package com.bakdata.conquery.models.messages.namespaces.specific;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.concepts.Concept;
import com.bakdata.conquery.models.concepts.Connector;
import com.bakdata.conquery.models.concepts.MatchingStats;
import com.bakdata.conquery.models.concepts.tree.ConceptTreeNode;
import com.bakdata.conquery.models.concepts.tree.TreeConcept;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.events.CBlock;
import com.bakdata.conquery.models.identifiable.ids.specific.ConceptElementId;
import com.bakdata.conquery.models.messages.namespaces.NamespacedMessage;
import com.bakdata.conquery.models.messages.namespaces.WorkerMessage;
import com.bakdata.conquery.models.worker.Worker;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;

@CPSType(id = "UPDATE_MATCHING_STATS", base = NamespacedMessage.class)
@Slf4j
public class UpdateMatchingStatsMessage extends WorkerMessage.Slow {

	@Override
	public void react(Worker worker) throws Exception {
		if (worker.getStorage().getAllCBlocks().isEmpty()) {
			log.debug("Worker {} is empty, skipping.", worker);
			getProgressReporter().done();
			return;
		}

		final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(worker.getExecutorService());

		getProgressReporter().setMax(worker.getStorage().getAllConcepts().size());

		log.info("Starting to update Matching stats for {} Concepts", worker.getStorage().getAllConcepts().size());

		// SubJobs collect into this Map.
		final Map<ConceptElementId<?>, MatchingStats.Entry>
				messages =
				new HashMap<>(worker.getStorage().getAllConcepts().size()
							  * 5_000); // Just a guess-timate so we don't grow that often, this memory is very short lived so we can over commit.

		List<ListenableFuture<?>> subJobs =
				worker.getStorage()
					  .getAllConcepts()
					  .stream()
					  .map(concept -> executorService.submit(() -> calculateConceptMatches(concept, messages, worker)))
					  .collect(Collectors.toList());

		log.debug("All jobs submitted. Waiting for completion.");

		Futures.allAsList(subJobs).get();

		log.info("All threads are done.");

		if (!messages.isEmpty()) {
			worker.send(new UpdateElementMatchingStats(worker.getInfo().getId(), messages));
		}
		else {
			log.warn("Results were empty.");
		}

		getProgressReporter().done();
	}

	public void calculateConceptMatches(Concept<?> concept, Map<ConceptElementId<?>, MatchingStats.Entry> results, Worker worker) {

		Map<ConceptElementId<?>, MatchingStats.Entry> messages = new HashMap<>();

		for (CBlock cBlock : new ArrayList<>(worker.getStorage().getAllCBlocks())) {

			if (!cBlock.getConnector().getConcept().equals(concept)) {
				continue;
			}

			try {
				Bucket bucket = cBlock.getBucket();
				Table table = bucket.getTable();

				for (int event = 0; event < bucket.getNumberOfEvents(); event++) {

					final int[] localIds = cBlock.getEventMostSpecificChild(event);

					if (!(concept instanceof TreeConcept) || localIds == null) {

						messages.computeIfAbsent(concept.getId(), (x) -> new MatchingStats.Entry())
								.addEvent(table, bucket, event);

						continue;
					}

					if (Connector.isNotContained(localIds)) {
						continue;
					}

					ConceptTreeNode<?> e = ((TreeConcept) concept).getElementByLocalId(localIds);

					while (e != null) {
						messages.computeIfAbsent(e.getId(), (x) -> new MatchingStats.Entry())
								.addEvent(table, bucket, event);
						e = e.getParent();
					}
				}
			}
			catch (Exception e) {
				log.error("Failed to collect the matching stats for {}", cBlock, e);
			}
		}

		getProgressReporter().report(1);


		synchronized (results) {
			results.putAll(messages);
		}
	}
}
