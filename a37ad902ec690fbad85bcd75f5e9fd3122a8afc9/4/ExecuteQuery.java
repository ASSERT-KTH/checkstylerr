package com.bakdata.conquery.models.messages.namespaces.specific;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.error.ConqueryError;
import com.bakdata.conquery.models.error.ConqueryException;
import com.bakdata.conquery.models.execution.ManagedExecution;
import com.bakdata.conquery.models.identifiable.ids.specific.ManagedExecutionId;
import com.bakdata.conquery.models.messages.namespaces.NamespacedMessage;
import com.bakdata.conquery.models.messages.namespaces.WorkerMessage;
import com.bakdata.conquery.models.query.QueryExecutionContext;
import com.bakdata.conquery.models.query.QueryPlanContext;
import com.bakdata.conquery.models.query.queryplan.QueryPlan;
import com.bakdata.conquery.models.query.results.ShardResult;
import com.bakdata.conquery.models.worker.Worker;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Send message to worker to execute {@code query} on the workers associated entities.
 */
@Slf4j
@CPSType(id="EXECUTE_QUERY", base=NamespacedMessage.class)
@AllArgsConstructor @NoArgsConstructor @Getter @Setter @ToString(callSuper=true)
public class ExecuteQuery extends WorkerMessage {

	private ManagedExecution<?> execution;

	@Override
	public void react(Worker context) throws Exception {
		log.info("Started {} {}", execution.getClass().getSimpleName(), execution.getId());
		Set<Entry<ManagedExecutionId, QueryPlan>> plans = null;
		// Generate query plans for this execution. For ManagedQueries this is only one plan.
		// For ManagedForms there might be multiple plans, which originate from ManagedQueries.
		// The results are send directly to these ManagesQueries
		try {
			plans = execution.createQueryPlans(new QueryPlanContext(context)).entrySet();		
		} catch (ConqueryException e) {	
			log.warn("Failed to create query plans for " + execution.getId(), e );
			ShardResult result = execution.getInitializedShardResult(null);
			sendFailureToMaster(result, execution, context, e.getCtx());
			return;
		} catch (Exception e) {
			log.error("Failed to create query plans for " + execution.getId(), e );
			// If one of the plans can not be created (maybe due to a Id that references a non existing concept) fail the whole job.
			ShardResult result = execution.getInitializedShardResult(null);
			sendFailureToMaster(result, execution, context, new ConqueryError.UnknownError(e));
			return;
		}
		// Execute all plans.
		for(Entry<ManagedExecutionId, QueryPlan> entry : plans) {
			ShardResult result = execution.getInitializedShardResult(entry);
			try {
				context.getQueryExecutor().execute(result, new QueryExecutionContext(context.getStorage()), entry);
				result.getFuture().addListener(()->result.send(context), MoreExecutors.directExecutor());
			} catch(ConqueryException e) {
				log.warn("Error while executing {} (with subquery: {})", execution.getId(), entry.getKey(), e );
				sendFailureToMaster(result, execution, context, e.getCtx());
			} catch (Exception e) {
				log.error("Error while executing {} (with subquery: {})", execution.getId(), entry.getKey(), e );
				sendFailureToMaster(result, execution, context, new ConqueryError.UnknownError(e));
			}
		}
	}

	private static void sendFailureToMaster(ShardResult result, ManagedExecution<?> execution, Worker context, ConqueryError error) {
		result.setError(Optional.of(error));
		result.finish();
		context.send(new CollectQueryResult(result));
	}
}
