package com.bakdata.conquery.models.concepts;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import com.bakdata.conquery.io.cps.CPSBase;
import com.bakdata.conquery.models.concepts.select.Select;
import com.bakdata.conquery.models.exceptions.ConfigurationException;
import com.bakdata.conquery.models.exceptions.JSONException;
import com.bakdata.conquery.models.identifiable.ids.specific.ConceptId;
import com.bakdata.conquery.models.identifiable.ids.specific.DatasetId;
import com.bakdata.conquery.models.query.QueryPlanContext;
import com.bakdata.conquery.models.query.queryplan.QPNode;
import com.bakdata.conquery.models.query.queryplan.aggregators.Aggregator;
import com.bakdata.conquery.models.query.queryplan.filter.FilterNode;
import com.bakdata.conquery.models.query.queryplan.specific.FiltersNode;
import com.bakdata.conquery.models.query.queryplan.specific.Leaf;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This is a single node or concept in a concept tree.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "type")
@CPSBase
@ToString(of = {"name", "connectors"})
public abstract class Concept<CONNECTOR extends Connector> extends ConceptElement<ConceptId> {

	@Getter
	@Setter
	private boolean hidden = false;
	@JsonManagedReference
	@Valid
	@Getter
	@Setter
	private List<CONNECTOR> connectors = Collections.emptyList();
	@NotNull
	@Getter
	@Setter
	private DatasetId dataset;


	public CONNECTOR getConnectorByName(String connector) {
		return connectors
					   .stream()
					   .filter(n -> n.getName().equals(connector))
					   .findAny()
					   .orElseThrow(() -> new NoSuchElementException("Connector not found: " + connector));
	}

	public abstract List<? extends Select> getSelects();

	public void initElements(Validator validator) throws ConfigurationException, JSONException {
	}

	@Override
	@JsonIgnore
	public Concept<?> getConcept() {
		return this;
	}

	@Override
	public ConceptId createId() {
		return new ConceptId(Objects.requireNonNull(dataset), getName());
	}

	public int countElements() {
		return 1;
	}

	@Override
	public long calculateBitMask() {
		return 0L;
	}

	/**
	 * Allows concepts to create their own altered FiltersNode if necessary.
	 */
	public QPNode createConceptQuery(QueryPlanContext context, List<FilterNode<?>> filters, List<Aggregator<?>> aggregators) {
		if (filters.isEmpty() && aggregators.isEmpty()) {
			return new Leaf();
		}
		return FiltersNode.create(filters, aggregators);
	}
}
