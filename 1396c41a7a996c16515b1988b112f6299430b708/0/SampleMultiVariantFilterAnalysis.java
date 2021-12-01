package org.opencb.opencga.analysis.variant.samples;

import org.apache.commons.lang3.time.StopWatch;
import org.opencb.biodata.models.variant.StudyEntry;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.opencga.analysis.tools.OpenCgaToolScopeStudy;
import org.opencb.opencga.catalog.db.api.IndividualDBAdaptor;
import org.opencb.opencga.catalog.db.api.SampleDBAdaptor;
import org.opencb.opencga.catalog.exceptions.CatalogException;
import org.opencb.opencga.core.common.TimeUtils;
import org.opencb.opencga.core.models.common.Enums;
import org.opencb.opencga.core.models.individual.Individual;
import org.opencb.opencga.core.models.sample.Sample;
import org.opencb.opencga.core.tools.annotations.Tool;
import org.opencb.opencga.storage.core.exceptions.StorageEngineException;
import org.opencb.opencga.storage.core.variant.adaptors.GenotypeClass;
import org.opencb.opencga.storage.core.variant.adaptors.VariantField;
import org.opencb.opencga.storage.core.variant.adaptors.VariantQueryParam;
import org.opencb.opencga.storage.core.variant.adaptors.VariantQueryUtils;
import org.opencb.opencga.storage.core.variant.adaptors.iterators.VariantDBIterator;
import org.opencb.opencga.storage.core.variant.query.VariantQueryParser;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

@Tool(id="sample-multi-query", resource = Enums.Resource.VARIANT)
public class SampleMultiVariantFilterAnalysis extends OpenCgaToolScopeStudy {

    private SampleMultiVariantFilterAnalysisParams analysisParams = new SampleMultiVariantFilterAnalysisParams();
    private TreeQuery treeQuery;
//    private LinkedList<String> steps;

    private final static Comparator<TreeQuery.Node> COMPARATOR = Comparator.comparing(SampleMultiVariantFilterAnalysis::toQueryValue);
    private String studyFqn;

    @Override
    protected void check() throws Exception {
        super.check();
        analysisParams.updateParams(params);
        studyFqn = getStudyFqn();

        treeQuery = new TreeQuery(analysisParams.getQuery());
        addAttribute("query", treeQuery.getRoot().toString());

        VariantQueryOptimizer.optimize(treeQuery);
        treeQuery.log();

//        steps = new LinkedList<>();
//        if (multiQuery.getTree().getType().equals(MultiQuery.Node.Type.QUERY)) {
//            int i = 0;
//            for (MultiQuery.Node node : multiQuery.getTree().getNodes()) {
//                steps.add("node-" + node.getType() + "-" + i);
//                i++;
//            }
//        }
//        steps.add("join-results");
    }

//    @Override
//    protected List<String> getSteps() {
//        return steps;
//    }

    @Override
    protected void run() throws Exception {
        List<String> inputSamples = new ArrayList<>(getVariantStorageManager().getIndexedSamples(studyFqn, getToken()));
        Query baseQuery = new Query();
        baseQuery.put(VariantQueryParam.STUDY.key(), studyFqn);

        List<String> samplesResult = resolveNode(treeQuery.getRoot(), baseQuery, inputSamples);

        addAttribute("numSamples", samplesResult.size());
        logger.info("Found {} samples", samplesResult.size());

        try (PrintStream out = new PrintStream(getOutDir().resolve("samples.tsv").toFile())) {
            out.println("##num_samples=" + samplesResult.size());
            out.println("#SAMPLE");
            for (String s : samplesResult) {
                out.println(s);
            }
        }
    }

    // Return a value that will depend on the likely of the node to return a large or small number of samples
    private static Integer toQueryValue(TreeQuery.Node node) {
        switch (node.getType()) {
            case QUERY:
                int v = 1000;
                Query query = node.getQuery();
                if (VariantQueryUtils.isValidParam(query, VariantQueryParam.ANNOT_CONSEQUENCE_TYPE)) {
                    List<String> cts = VariantQueryUtils
                            .parseConsequenceTypes(query.getAsStringList(VariantQueryParam.ANNOT_CONSEQUENCE_TYPE.key()));
                    if (VariantQueryUtils.LOF_SET.containsAll(cts)) {
                        v -= 500;
                    } else if (VariantQueryUtils.LOF_EXTENDED_SET.containsAll(cts)) {
                        v -= 250;
                    }
                }
                VariantQueryParser.VariantQueryXref xrefs = VariantQueryParser.parseXrefs(query);
                int fromXref = 0;
//                if (!xrefs.getGenes().isEmpty()) {
//
//                }
//                if (!xrefs.getOtherXrefs().isEmpty()) {
//
//                }
//
//                if (VariantQueryUtils.isValidParam(query, VariantQueryParam.ANNOT_CONSEQUENCE_TYPE)) {
//
//                }
//                if (VariantQueryUtils.isValidParam(query, VariantQueryParam.ANNOT_CONSEQUENCE_TYPE)) {
//
//                }
                v -= fromXref;
                return v;
            case COMPLEMENT:
                return 1000 - toQueryValue(node.getNodes().get(0));
            case UNION:
                return node.getNodes().stream().mapToInt(SampleMultiVariantFilterAnalysis::toQueryValue).max().orElse(0);
            case INTERSECTION:
                return node.getNodes().stream().mapToInt(SampleMultiVariantFilterAnalysis::toQueryValue).min().orElse(0);
            default:
                throw new IllegalArgumentException("Unknown node type " + node.getType());
        }

    }

    private List<String> resolveNode(TreeQuery.Node node, Query baseQuery, List<String> includeSamples)
            throws CatalogException, StorageEngineException, IOException {
        switch (node.getType()) {
            case QUERY:
                return resolveQuery(((TreeQuery.QueryNode) node), baseQuery, includeSamples);
            case COMPLEMENT:
                return resolveComplementQuery(((TreeQuery.ComplementNode) node), baseQuery, includeSamples);
            case INTERSECTION:
                return resolveIntersectNode(((TreeQuery.IntersectionNode) node), baseQuery, includeSamples);
            case UNION:
                return resolveUnionNode(((TreeQuery.UnionNode) node), baseQuery, includeSamples);
            default:
                throw new IllegalArgumentException("Unknown node type " + node.getType());
        }
    }

    private List<String> resolveUnionNode(TreeQuery.UnionNode node, Query baseQuery, List<String> includeSamples)
            throws CatalogException, StorageEngineException, IOException {

        logger.info("Execute union-node with {} children for {} samples",
                node.getNodes().size(), includeSamples.size());

        includeSamples = new ArrayList<>(includeSamples);
        Set<String> result = new HashSet<>();
        node.getNodes().sort(COMPARATOR.reversed());
        for (TreeQuery.Node subNode : node.getNodes()) {
            if (includeSamples.isEmpty()) {
                logger.info("Skip node '{}'. All samples found", subNode);
            } else {
                List<String> thisNodeResult = resolveNode(subNode, baseQuery, includeSamples);
                includeSamples.removeAll(thisNodeResult);
                result.addAll(thisNodeResult);
            }
        }

        return new ArrayList<>(result);
    }

    private List<String> resolveIntersectNode(TreeQuery.IntersectionNode node, Query baseQuery, List<String> includeSamples)
            throws CatalogException, StorageEngineException, IOException {

        logger.info("Execute intersect-node with {} children at for {} samples",
                node.getNodes().size(), includeSamples.size());

        node.getNodes().sort(COMPARATOR.reversed());
        for (TreeQuery.Node subNode : node.getNodes()) {
            if (includeSamples.isEmpty()) {
                logger.info("Skip node '{}'", subNode);
            } else {
                includeSamples = resolveNode(subNode, baseQuery, includeSamples);
            }
        }

        return includeSamples;
    }

    private List<String> resolveComplementQuery(TreeQuery.ComplementNode node, Query baseQuery, List<String> includeSamples)
            throws CatalogException, IOException, StorageEngineException {
        logger.info("Execute complement-node for {} samples", includeSamples.size());
        List<String> subSamples = resolveNode(node.getNodes().get(0), baseQuery, includeSamples);
        logger.info("Discard {} of {} samples", subSamples.size(), includeSamples.size());

        includeSamples = new LinkedList<>(includeSamples);
        includeSamples.removeAll(subSamples);
        return includeSamples;
    }

    private List<String> resolveQuery(TreeQuery.QueryNode node, Query baseQuery, List<String> includeSamples)
            throws CatalogException, StorageEngineException, IOException {
        logger.info("Execute leaf-node '{}' for {} samples", node, includeSamples.size());

        Query variantsQuery = node.getQuery();
        Query sampleQuery = new Query();
        Query individualQuery = new Query();
        for (String key : new HashSet<>(variantsQuery.keySet())) {
            if (key.startsWith("sample.")) {
                sampleQuery.put(key.replaceFirst("sample\\.", ""), variantsQuery.getString(key));
                variantsQuery.remove(key);
            }
            if (key.startsWith("individual.")) {
                sampleQuery.put(key.replaceFirst("individual\\.", ""), variantsQuery.getString(key));
                variantsQuery.remove(key);
            }
        }
        if (!sampleQuery.isEmpty()) {
            int inputSampleSize = includeSamples.size();
            if (sampleQuery.containsKey(SampleDBAdaptor.QueryParams.ID.key())) {
                // Remove samples not in the query
                Set<String> samplesFromQuery = new HashSet<>(sampleQuery.getAsStringList(SampleDBAdaptor.QueryParams.ID.key()));
                includeSamples = new LinkedList<>(includeSamples);
                includeSamples.removeIf(s -> !samplesFromQuery.contains(s));
            }
            if (!includeSamples.isEmpty()) {
                sampleQuery.put(SampleDBAdaptor.QueryParams.ID.key(), includeSamples);
                includeSamples = getCatalogManager().getSampleManager()
                        .search(studyFqn, sampleQuery, new QueryOptions(QueryOptions.INCLUDE, "id"), getToken())
                        .getResults()
                        .stream()
                        .map(Sample::getId)
                        .collect(Collectors.toList());
            }

            logger.info("Filter samples with catalog samples metadata. Found {} samples out of {}",
                    includeSamples.size(), inputSampleSize);
            if (includeSamples.isEmpty()) {
                logger.info("Skip query leaf no sample passed the catalog sample filter.");
                return Collections.emptyList();
            }
        }

        if (!individualQuery.isEmpty()) {
            int inputSampleSize = includeSamples.size();
            individualQuery.put(IndividualDBAdaptor.QueryParams.SAMPLES.key(), includeSamples);
            includeSamples = getCatalogManager().getIndividualManager()
                    .search(studyFqn, individualQuery, new QueryOptions(QueryOptions.INCLUDE, "id"), getToken())
                    .getResults()
                    .stream()
                    .map(Individual::getSamples)
                    .flatMap(Collection::stream)
                    .map(Sample::getId)
                    .filter(new HashSet<>(includeSamples)::contains)
                    .collect(Collectors.toList());

            logger.info("Filter samples with catalog individuals metadata. Found {} samples out of {}",
                    includeSamples.size(), inputSampleSize);
            if (includeSamples.isEmpty()) {
                logger.info("Skip query leaf no sample passed the catalog individual filter.");
                return Collections.emptyList();
            }
        }

        Set<String> samples;
        if (params.getBoolean("direct")) {
            samples = resolveQueryDirect(node, baseQuery, includeSamples);
        } else {
            samples = resolveQuerySamplesData(node, baseQuery, includeSamples);
        }

        logger.info("Found {} sample in leaf '{}'", samples.size(), node);
        return new ArrayList<>(samples);
    }

    private Set<String> resolveQuerySamplesData(TreeQuery.QueryNode node, Query baseQuery, List<String> includeSamples)
            throws CatalogException, StorageEngineException, IOException {
        Query query = new Query(baseQuery);
        query.putAll(node.getQuery());
        Set<String> samples = new HashSet<>();
        includeSamples = new LinkedList<>(includeSamples);

        List<String> thisVariantSamples = new ArrayList<>(includeSamples.size());
        VariantDBIterator iterator = getVariantStorageManager().iterator(query, new QueryOptions(VariantField.SUMMARY, true), getToken());
        while (iterator.hasNext()) {
            Variant next = iterator.next();
            StopWatch stopWatch = StopWatch.createStarted();
            logger.debug("[{}] start processing", next);
            includeSamples.removeAll(samples);
            if (includeSamples.isEmpty()) {
                logger.info("Shortcut at node '{}' after finding {} samples", node, samples.size());
                break;
            }

            int limit = 1000;
            int skip = 0;
            int numSamples;

            do {
                QueryOptions queryOptions = new QueryOptions();
                queryOptions.put(VariantQueryParam.INCLUDE_SAMPLE.key(), includeSamples);
                queryOptions.put(QueryOptions.LIMIT, limit);
                queryOptions.put(QueryOptions.SKIP, skip);

                Variant variant = getVariantStorageManager()
                        .getSampleData(next.toString(), studyFqn, queryOptions, getToken()).first();

                StudyEntry studyEntry = variant.getStudies().get(0);
                numSamples = studyEntry.getSamplesData().size();
                skip += numSamples;

                int sampleIdPos = studyEntry.getFormatPositions().get(VariantQueryParser.SAMPLE_ID);
                for (List<String> samplesDatum : studyEntry.getSamplesData()) {
                    if (GenotypeClass.MAIN_ALT.test(samplesDatum.get(0))) {
                        String sampleId = samplesDatum.get(sampleIdPos);
                        samples.add(sampleId);
                        thisVariantSamples.add(sampleId);
                    }
                }
            } while (numSamples == limit);

            logger.debug("[{}] found {} samples in {}", next, thisVariantSamples.size(), TimeUtils.durationToString(stopWatch));
            thisVariantSamples.clear();
        }
        return samples;
    }

    private Set<String> resolveQueryDirect(TreeQuery.QueryNode node, Query baseQuery, List<String> includeSamples)
            throws CatalogException, StorageEngineException {
        Query query = new Query(baseQuery);
        query.putAll(node.getQuery());
        query.put(VariantQueryParam.INCLUDE_SAMPLE.key(), includeSamples);
        query.put(VariantQueryParam.INCLUDE_FORMAT.key(), "GT," + VariantQueryParser.SAMPLE_ID);
        Set<String> samples = new HashSet<>();

        VariantDBIterator iterator = getVariantStorageManager().iterator(query, new QueryOptions(), getToken());
        while (iterator.hasNext()) {
            Variant next = iterator.next();
            for (List<String> samplesDatum : next.getStudies().get(0).getSamplesData()) {
                if (GenotypeClass.MAIN_ALT.test(samplesDatum.get(0))) {
                    samples.add(samplesDatum.get(1));
                }
            }
        }
        return samples;
    }

}
