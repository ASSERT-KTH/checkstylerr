package com.piksel.sequoia.clientsdk.criteria;

/*-
 * #%L
 * Sequoia Java Client SDK
 * %%
 * Copyright (C) 2018 Piksel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import com.google.common.collect.Lists;
import com.piksel.sequoia.annotations.Internal;

/**
 * Creates new query strings from a {@link Criteria}.
 */
@Internal
@SuppressWarnings({"rawtypes", "unchecked"}) 
public final class QueryStringFactory {

    /**
     * Construct and return a {@link QueryString} that satisfies the criteria provided.
     */
    public QueryString createQueryString(DefaultCriteria<?> criteria) {
        checkNotNull(criteria);
        QueryString qs = new QueryString();
        if (criteria.isEmpty()) {
            return qs;
        }
        createCriterionList(criteria, qs);
        addFieldsEntries(qs, criteria);
        addOrderEntries(qs, criteria);
        createInclusions(criteria, qs);

        addIntersectFieldCriterias(criteria, qs);

        return qs;
    }


    private void createInclusions(DefaultCriteria<?> criteria, QueryString qs) {
        addInclusionEntries(qs, criteria);
        addInclusionFieldEntries(qs, criteria);
    }


    private void createCriterionList(DefaultCriteria<?> criteria, QueryString qs) {
        addPerPage(criteria, qs);
        addPage(criteria, qs);
        addCount(criteria, qs);
        addFacetCount(criteria, qs);
        addSkipResources(criteria, qs);
        addLang(criteria, qs);
        addActive(criteria, qs);
        addAvailable(criteria, qs);
        addCriterionEntries(criteria, qs);
    }

    private void addLang(DefaultCriteria criteria, QueryString qs) {
        Optional.ofNullable(criteria.getLang()).ifPresent(lang -> {
            if (!Objects.isNull(lang.getLang())) {
                qs.put(criteria.getDocumentName().get().concat("lang"), lang.getLang());
            }
        });
    }

    private void addSkipResources(DefaultCriteria criteria, QueryString qs) {
        Optional.ofNullable(criteria.resourceSettings()).ifPresent(skipResources -> {
            if (skipResources.isSkipResources()) {
                qs.put(criteria.getDocumentName().get().concat("resources"), "false");
            }
        });
    }

    private void addCount(DefaultCriteria criteria, QueryString qs) {
        Optional.ofNullable(criteria.getCount()).ifPresent(count -> {
            if (count.isCount()) {
                qs.put(criteria.getDocumentName().get().concat("count"), "true");
            }
        });
    }

    private void addFacetCount(DefaultCriteria criteria, QueryString qs) {
        Optional.ofNullable(criteria.getFacetCount()).ifPresent(facetCount -> {
            if (!Objects.isNull(facetCount.getFields())) {
                qs.put(criteria.getDocumentName().get().concat("count"), facetCount.getFields());
            }
        });
    }

    private void addPerPage(DefaultCriteria criteria, QueryString qs) {
        Optional.ofNullable(criteria.getPerPage()).ifPresent(perPage ->
            qs.put(criteria.getDocumentName().get().concat("perPage"), Integer.toString(perPage.getPerPage()))
        );
    }

    private void addPage(DefaultCriteria criteria, QueryString qs) {
        Optional.ofNullable(criteria.getPage()).ifPresent(page ->
                qs.put(criteria.getDocumentName().get().concat("page"), Integer.toString(page.getPage()))
        );
    }

    private void addCriterionEntries(DefaultCriteria criteria, QueryString qs) {
        List<Criterion> entries = criteria.getCriterionEntries();
        for (Criterion c : entries) {
            c.applyExpression(criteria.getDocumentName(), qs);
        }
    }

    private void addActive(Criteria criteria, QueryString qs) {
        criteria.getActive().ifPresent(active -> qs.put(criteria.getDocumentName().get().concat("active"), String.valueOf(active)));
    }

    private void addAvailable(Criteria criteria, QueryString qs) {
        criteria.getAvailable().ifPresent(available -> qs.put(criteria.getDocumentName().get().concat("available"), String.valueOf(available)));
    }

    private void addOrderEntries(QueryString qs, DefaultCriteria<?> criteria) {
        List<String> orderCriteriaFields = Lists.newArrayList();
        criteria.getOrderEntries().forEach(order -> orderCriteriaFields.add(order.toString()));
        if (!orderCriteriaFields.isEmpty()) {
            qs.put(criteria.getDocumentName().get().concat("sort"), String.join(",", orderCriteriaFields));
        }
    }

    private void addInclusionEntries(QueryString qs, DefaultCriteria criteria) {
        List<Inclusion> entries = criteria.getInclusionEntries();
        List<DefaultCriteria> intersectedDocs = criteria.getIntersectCriterias();
        String inclusions = Stream.concat(
                    entries.stream().map(Inclusion::toString),
                    intersectedDocs.stream().map(dc -> dc.getDocumentName().getName().concat("<intersect>")))
            .collect(Collectors.joining(","));
        if (!StringUtils.isBlank(inclusions)) {
            qs.put(criteria.getDocumentName().get().concat("include"), inclusions);
        }
    }

    private void addFieldsEntries(QueryString qs, DefaultCriteria<?> criteria) {
        List<String> fieldSelectorCriteriaFields = Lists.newArrayList();
        criteria.getFieldsEntries().forEach(field -> fieldSelectorCriteriaFields.add(field.toString()));
        if (!fieldSelectorCriteriaFields.isEmpty()) {
            qs.put(criteria.getDocumentName().get().concat("fields"), String.join(",", fieldSelectorCriteriaFields));
        }
    }

    private void addInclusionFieldEntries(QueryString qs, DefaultCriteria<?> criteria) {
        for (Inclusion inclusion : criteria.getInclusionEntries()) {
            addFields(qs, inclusion, criteria.getDocumentName().get());
            addOrders(qs, inclusion, criteria.getDocumentName().get());
        }
    }

    private void addOrders(QueryString qs, Inclusion inclusion, String documentName) {
        if (!inclusion.getOrderEntries().isEmpty()) {
            qs.put(documentName + Objects.toString(inclusion) + ".sort", String.join(",", inclusion.getOrderEntries()));
        }
    }

    private void addFields(QueryString qs, Inclusion inclusion, String documentName) {
        if (!inclusion.getFieldEntries().isEmpty()) {
            qs.put(documentName + Objects.toString(inclusion) + ".fields", String.join(",", inclusion.getFieldEntries()));
        }
    }

    private void addIntersectFieldCriterias (DefaultCriteria criteria, QueryString qs) {
        ((List<DefaultCriteria>)criteria.getIntersectCriterias()).stream()
                .map(this::createQueryString)
                .reduce(qs, (qs1, qs2) -> {
                    qs1.putAll(qs2);
                    return qs1;
                });
    }

}
