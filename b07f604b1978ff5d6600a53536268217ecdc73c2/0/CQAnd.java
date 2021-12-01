package com.bakdata.conquery.models.query.concept.specific;

import java.util.*;
import java.util.function.Consumer;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import c10n.C10N;
import com.bakdata.conquery.apiv1.forms.export_form.ExportForm;
import com.bakdata.conquery.internationalization.CQElementC10n;
import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.io.jackson.InternalOnly;
import com.bakdata.conquery.models.execution.ManagedExecution;
import com.bakdata.conquery.models.externalservice.ResultType;
import com.bakdata.conquery.models.query.QueryPlanContext;
import com.bakdata.conquery.models.query.QueryResolveContext;
import com.bakdata.conquery.models.query.Visitable;
import com.bakdata.conquery.models.query.concept.CQElement;
import com.bakdata.conquery.models.query.queryplan.ConceptQueryPlan;
import com.bakdata.conquery.models.query.queryplan.DateAggregationAction;
import com.bakdata.conquery.models.query.queryplan.QPNode;
import com.bakdata.conquery.models.query.queryplan.aggregators.specific.ExistsAggregator;
import com.bakdata.conquery.models.query.queryplan.specific.AndNode;
import com.bakdata.conquery.models.query.resultinfo.LocalizedDefaultResultInfo;
import com.bakdata.conquery.models.query.resultinfo.ResultInfoCollector;
import com.bakdata.conquery.util.QueryUtils;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;

@CPSType(id = "AND", base = CQElement.class)
public class CQAnd extends CQElement implements ExportForm.DefaultSelectSettable {

	@Getter
	@Setter
	@NotEmpty
	@Valid
	private List<CQElement> children;

	@Getter @Setter
	private Optional<Boolean> createExists = Optional.empty();

	@InternalOnly
	@Getter @Setter
	private DateAggregationAction dateAction;

	@Override
	public void setDefaultExists() {
		if (createExists.isEmpty()){
			createExists = Optional.of(true);
		}
	}

	@Override
	public QPNode createQueryPlan(QueryPlanContext context, ConceptQueryPlan plan) {
		Preconditions.checkNotNull(dateAction);

		QPNode[] nodes = new QPNode[children.size()];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = children.get(i).createQueryPlan(context, plan);
		}

		final QPNode node = AndNode.of(Arrays.asList(nodes), dateAction);

		if (createExists.orElse(false)) {
			final ExistsAggregator existsAggregator = new ExistsAggregator(node.collectRequiredTables());
			existsAggregator.setReference(node);
			plan.addAggregator(existsAggregator);
		}

		return node;
	}

	@Override
	public void collectRequiredQueries(Set<ManagedExecution> requiredQueries) {
		for (CQElement c : children) {
			c.collectRequiredQueries(requiredQueries);
		}
	}

	@Override
	public void resolve(QueryResolveContext context) {
		Preconditions.checkNotNull(context.getDateAggregationMode());

		dateAction = determineDateAction(context);
		children.forEach(c->c.resolve(context));
	}

	private DateAggregationAction determineDateAction(QueryResolveContext context) {
		switch(context.getDateAggregationMode()) {
			case NONE:
				return DateAggregationAction.BLOCK;
			case MERGE:
				return DateAggregationAction.MERGE;
			case LOGICAL:
			case INTERSECT:
				return DateAggregationAction.INTERSECT;
			default:
				throw new IllegalStateException("Cannot handle mode " + context.getDateAggregationMode());
		}
	}

	@Override
	public void collectResultInfos(ResultInfoCollector collector) {
		for (CQElement c : children) {
			c.collectResultInfos(collector);
		}

		if(createExists.orElse(false)){
			collector.add(new LocalizedDefaultResultInfo(this::getUserOrDefaultLabel, this::defaultLabel, ResultType.BooleanT.INSTANCE));
		}
	}

	@Override
	public String getUserOrDefaultLabel(Locale locale) {
		// Prefer the user label
		if (getLabel() != null){
			return getLabel();
		}
		return QueryUtils.createDefaultMultiLabel(children, " " + C10N.get(CQElementC10n.class, locale).and() + " ", locale);
	}

	@Override
	public String defaultLabel(Locale locale) {
		// This forces the default label on children even if there was a user label
		return QueryUtils.createTotalDefaultMultiLabel(children, " " + C10N.get(CQElementC10n.class, locale).and() + " ", locale);
	}

	@Override
	public void visit(Consumer<Visitable> visitor) {
		super.visit(visitor);
		for (CQElement c : children) {
			c.visit(visitor);
		}
	}
}
