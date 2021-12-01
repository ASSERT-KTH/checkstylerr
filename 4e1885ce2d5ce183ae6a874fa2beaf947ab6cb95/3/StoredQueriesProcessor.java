package com.bakdata.conquery.apiv1;

import static com.bakdata.conquery.models.auth.AuthorizationHelper.buildDatasetAbilityMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;

import com.bakdata.conquery.io.storage.MetaStorage;
import com.bakdata.conquery.models.auth.AuthorizationHelper;
import com.bakdata.conquery.models.auth.entities.User;
import com.bakdata.conquery.models.auth.permissions.Ability;
import com.bakdata.conquery.models.config.ConqueryConfig;
import com.bakdata.conquery.models.datasets.Dataset;
import com.bakdata.conquery.models.exceptions.JSONException;
import com.bakdata.conquery.models.execution.ExecutionState;
import com.bakdata.conquery.models.execution.ExecutionStatus;
import com.bakdata.conquery.models.execution.FullExecutionStatus;
import com.bakdata.conquery.models.execution.ManagedExecution;
import com.bakdata.conquery.models.identifiable.ids.specific.DatasetId;
import com.bakdata.conquery.models.identifiable.ids.specific.ManagedExecutionId;
import com.bakdata.conquery.models.query.ExecutionManager;
import com.bakdata.conquery.models.query.IQuery;
import com.bakdata.conquery.models.query.ManagedQuery;
import com.bakdata.conquery.models.query.concept.CQElement;
import com.bakdata.conquery.models.query.concept.ConceptQuery;
import com.bakdata.conquery.models.query.concept.SecondaryIdQuery;
import com.bakdata.conquery.models.query.concept.specific.CQAnd;
import com.bakdata.conquery.models.query.concept.specific.CQExternal;
import com.bakdata.conquery.models.worker.DatasetRegistry;
import com.bakdata.conquery.models.worker.Namespace;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class StoredQueriesProcessor {

	@Getter
	private final DatasetRegistry datasetRegistry;
	private final MetaStorage storage;
	private final ConqueryConfig config;

	public Stream<ExecutionStatus> getAllQueries(Namespace namespace, HttpServletRequest req, User user) {
		Collection<ManagedExecution<?>> allQueries = storage.getAllExecutions();

		return getQueriesFiltered(namespace.getDataset(), RequestAwareUriBuilder.fromRequest(req), user, allQueries);
	}

	public Stream<ExecutionStatus> getQueriesFiltered(Dataset datasetId, UriBuilder uriBuilder, User user, Collection<ManagedExecution<?>> allQueries) {
		Map<DatasetId, Set<Ability>> datasetAbilities = buildDatasetAbilityMap(user, datasetRegistry);

		return allQueries.stream()
						 // to exclude subtypes from somewhere else
						 .filter(StoredQueriesProcessor::canFrontendRender)
						 // The following only checks the dataset, under which the query was submitted, but a query can target more that
						 // one dataset.
						 .filter(q -> q.getDataset().equals(datasetId))
						 .filter(q -> q.getState().equals(ExecutionState.DONE) || q.getState().equals(ExecutionState.NEW))
						 // We decide, that if a user owns an execution it is permitted to see it, which saves us a lot of permissions
						 // However, for other executions we check because those are probably shared.
						 .filter(q -> user.isPermitted(q, Ability.READ))
						 .flatMap(mq -> {
							 try {
								 return Stream.of(
										 mq.buildStatusOverview(
												 uriBuilder.clone(),
												 user,
												 datasetAbilities
										 ));
							 }
							 catch (Exception e) {
								 log.warn("Could not build status of " + mq, e);
								 return Stream.empty();
							 }
						 });
	}

	private static boolean canFrontendRender(ManagedExecution<?> q) {
		if (!(q instanceof ManagedQuery)) {
			return false;
		}

		final IQuery query = ((ManagedQuery) q).getQuery();

		if (query instanceof ConceptQuery) {
			return isFrontendStructure(((ConceptQuery) query).getRoot());
		}

		if (query instanceof SecondaryIdQuery) {
			return isFrontendStructure(((SecondaryIdQuery) query).getRoot());
		}

		return false;
	}

	/**
	 * Frontend can only render very specific formats properly.
	 *
	 * @implNote We filter for just the bare minimum, as the structure of the frontend is very specific and hard to fix in java code.
	 */
	public static boolean isFrontendStructure(CQElement root) {
		return root instanceof CQAnd || root instanceof CQExternal;
	}


  public void deleteQuery(ManagedExecution<?> execution, User user) {

		user.authorize(execution, Ability.DELETE);

		storage.removeExecution(execution.getId());
	}

	public FullExecutionStatus getQueryFullStatus(ManagedExecution query, User user, UriBuilder url) {

		user.authorize(query, Ability.READ);

		query.initExecutable(datasetRegistry, config);

		Map<DatasetId, Set<Ability>> datasetAbilities = buildDatasetAbilityMap(user, datasetRegistry);
		return query.buildStatusFull(storage, url, user, datasetRegistry, datasetAbilities);
	}

	public void patchQuery(User user, ManagedExecution execution, MetaDataPatch patch) throws JSONException {

		user.authorize(execution, Ability.MODIFY);

		log.trace("Patching {} ({}) with patch: {}", execution.getClass().getSimpleName(), execution, patch);
		patch.applyTo(execution, storage, user);
		storage.updateExecution(execution);

		// Patch this query in other datasets
		List<Dataset> remainingDatasets = datasetRegistry.getAllDatasets(() -> new ArrayList<>());
		remainingDatasets.remove(execution.getDataset());

		for (Dataset dataset : remainingDatasets) {
			ManagedExecutionId id = new ManagedExecutionId(dataset.getId(), execution.getQueryId());
			final ManagedExecution<?> otherExecution = storage.getExecution(id);
			if (otherExecution == null) {
				continue;
			}
			log.trace("Patching {} ({}) with patch: {}", execution.getClass().getSimpleName(), id, patch);
			patch.applyTo(otherExecution, storage, user);
			storage.updateExecution(execution);
		}
	}

	public FullExecutionStatus reexecute(User user, ManagedExecution<?> query, UriBuilder responseBuilder) {
		if(!query.getState().equals(ExecutionState.RUNNING)) {
			ExecutionManager.execute(getDatasetRegistry(), query, config);
		}

		final Map<DatasetId, Set<Ability>> datasetAbilities = AuthorizationHelper.buildDatasetAbilityMap(user, getDatasetRegistry());
		return query.buildStatusFull(storage, responseBuilder, user, getDatasetRegistry(), datasetAbilities);
	}

}
