package com.bakdata.conquery.models.query.concept;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.CheckForNull;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.bakdata.conquery.apiv1.QueryDescription;
import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.datasets.Table;
import com.bakdata.conquery.models.externalservice.ResultType;
import com.bakdata.conquery.models.identifiable.ids.specific.ColumnId;
import com.bakdata.conquery.models.identifiable.ids.specific.ManagedExecutionId;
import com.bakdata.conquery.models.identifiable.ids.specific.SecondaryIdId;
import com.bakdata.conquery.models.identifiable.ids.specific.TableId;
import com.bakdata.conquery.models.query.IQuery;
import com.bakdata.conquery.models.query.QueryPlanContext;
import com.bakdata.conquery.models.query.QueryResolveContext;
import com.bakdata.conquery.models.query.Visitable;
import com.bakdata.conquery.models.query.concept.filter.CQTable;
import com.bakdata.conquery.models.query.concept.specific.CQConcept;
import com.bakdata.conquery.models.query.queryplan.ConceptQueryPlan;
import com.bakdata.conquery.models.query.queryplan.SecondaryIdQueryPlan;
import com.bakdata.conquery.models.query.resultinfo.ResultInfoCollector;
import com.bakdata.conquery.models.query.resultinfo.SimpleResultInfo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

@Getter
@CPSType(id = "SECONDARY_ID_QUERY", base = QueryDescription.class)
public class SecondaryIdQuery extends IQuery {

	@Valid
	@NotNull
	private final CQElement root;
	/**
	 * @apiNote not using {@link ConceptQuery} directly in the API-spec simplifies the API.
	 */
	@JsonIgnore
	private final ConceptQuery query;
	@NotNull
	private final SecondaryIdId secondaryId;

	@JsonCreator
	public SecondaryIdQuery(@Valid @NotNull CQElement root, @NotNull SecondaryIdId secondaryId) {
		this.root = root;
		this.secondaryId = secondaryId;
		this.query = new ConceptQuery(root);
	}

	@Override
	public SecondaryIdQueryPlan createQueryPlan(QueryPlanContext context) {

		context = context.withSelectedSecondaryId(getSecondaryId());

		final ConceptQueryPlan queryPlan = query.createQueryPlan(context);

		Set<ColumnId> withSecondaryId = new HashSet<>();
		Set<TableId> withoutSecondaryId = new HashSet<>();

		// partition tables by their holding of the requested SecondaryId.
		// This assumes that from the root, only ConceptNodes hold TableIds we are interested in.
		query.visit(queryElement -> {
			if (!(queryElement instanceof CQConcept)) {
				return;
			}

			final CQConcept concept = (CQConcept) queryElement;

			for (CQTable connector : concept.getTables()) {
				final Table table = connector.getResolvedConnector().getTable();
				final Column secondaryIdColumn = findSecondaryIdColumn(table);

				if (secondaryIdColumn != null && !concept.isExcludeFromSecondaryIdQuery()) {
					withSecondaryId.add(secondaryIdColumn.getId());
				}
				else {
					withoutSecondaryId.add(table.getId());
				}
			}
		});

		// If there are no tables with the secondaryId, we fail as that is user error.
		if (withSecondaryId.isEmpty()) {
			throw new IllegalArgumentException("No SecondaryIds found.");
		}

		return new SecondaryIdQueryPlan(queryPlan, secondaryId, withSecondaryId, withoutSecondaryId);
	}

	/**
	 * selects the right column for the given secondaryId from a table
	 */
	@CheckForNull
	private Column findSecondaryIdColumn(Table table) {

		for (Column col : table.getColumns()) {
			if (!secondaryId.equals(col.getSecondaryId())) {
				continue;
			}

			return col;
		}

		return null;
	}

	@Override
	public void collectRequiredQueries(Set<ManagedExecutionId> requiredQueries) {
		query.collectRequiredQueries(requiredQueries);
	}

	@Override
	public void resolve(QueryResolveContext context) {
		query.resolve(context);
	}

	@Override
	public void collectResultInfos(ResultInfoCollector collector) {
		collector.add(new SimpleResultInfo(secondaryId.getName(), ResultType.STRING));
		query.collectResultInfos(collector);
	}

	@Override
	public void visit(Consumer<Visitable> visitor) {
		visitor.accept(this);
		root.visit(visitor);
	}

	public static enum QueryPlanPhase {
		None,
		WithId,
		WithoutId
	}
}