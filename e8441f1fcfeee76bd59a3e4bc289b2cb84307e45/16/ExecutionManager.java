package com.bakdata.conquery.models.query;

import java.util.Objects;
import java.util.UUID;

import com.bakdata.conquery.apiv1.SubmittedQuery;
import com.bakdata.conquery.io.xodus.MasterMetaStorage;
import com.bakdata.conquery.models.exceptions.JSONException;
import com.bakdata.conquery.models.execution.ManagedExecution;
import com.bakdata.conquery.models.identifiable.ids.specific.DatasetId;
import com.bakdata.conquery.models.identifiable.ids.specific.ManagedExecutionId;
import com.bakdata.conquery.models.identifiable.ids.specific.UserId;
import com.bakdata.conquery.models.messages.namespaces.specific.ExecuteQuery;
import com.bakdata.conquery.models.query.results.ShardResult;
import com.bakdata.conquery.models.worker.Namespace;
import com.bakdata.conquery.models.worker.Namespaces;
import com.bakdata.conquery.models.worker.WorkerInformation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExecutionManager {

	@NonNull
	private final Namespace namespace;

	public ManagedExecution<?> runQuery(SubmittedQuery query, UserId userId) throws JSONException {
		return runQuery(query, UUID.randomUUID(), userId);
	}

	public ManagedExecution<?> runQuery(SubmittedQuery query, UUID queryId, UserId userId) throws JSONException {
		return executeQuery(createQuery(namespace.getStorage().getMetaStorage(), namespace.getNamespaces(), query, queryId, userId, namespace.getDataset().getId()));
	}

	public static ManagedExecution<?> createQuery(MasterMetaStorage storage, Namespaces namespaces, SubmittedQuery query, UUID queryId, UserId userId, DatasetId submittedDataset) throws JSONException {
		// Transform the submitted query into an initialized execution
		ManagedExecution<?> managed = query.toManagedExecution(storage, namespaces, userId, submittedDataset);


//		ManagedQuery managed = new ManagedQuery(query, namespace, user.getId());
		managed.setExecutionId(queryId);
		storage.addExecution(managed);

		return managed;
	}

	/**
	 * Send message for query execution to all workers.
	 *
	 * @param query
	 * @return
	 */
	public ManagedExecution<?> executeQuery(ManagedExecution<?> query) {

		query.start();

		for(WorkerInformation worker : namespace.getWorkers()) {
			worker.send(new ExecuteQuery(query));
		}
		return query;
	}

	/**
	 * Receive part of query result and store into query.
	 * @param result
	 */
	public <R extends ShardResult, E extends ManagedExecution<R>> void addQueryResult(R result) {
		((E)getQuery(result.getQueryId())).addResult(result);
	}

	public ManagedExecution<?> getQuery(@NonNull ManagedExecutionId id) {
		return Objects.requireNonNull(namespace.getStorage().getMetaStorage().getExecution(id),"Unable to find query " + id.toString());
	}

}
