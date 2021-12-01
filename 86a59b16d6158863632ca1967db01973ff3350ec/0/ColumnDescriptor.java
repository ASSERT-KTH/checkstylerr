package com.bakdata.conquery.models.query;

import com.bakdata.conquery.models.concepts.select.Select;
import com.bakdata.conquery.models.externalservice.ResultType;
import com.bakdata.conquery.models.identifiable.ids.specific.SelectId;
import com.bakdata.conquery.models.query.concept.specific.CQConcept;
import lombok.Builder;
import lombok.Getter;

/**
 * Container class for the query API provide meta data for reach column in the
 * csv result. This can be used for the result preview in the frontend.
 */
@Getter
@Builder
public class ColumnDescriptor {

	/**
	 * The name of the column.
	 */
	private String label;
	/**
	 * If this descriptor originates from a {@link Select} which is a child of {@link CQConcept}, it is the label of the corresponding {@link CQConcept}, otherwise <code>null</code>.
	 */
	private String userConceptLabel;
	/**
	 * The datatype that corresponds to this column.
	 */
	private String type;
	/**
	 * Similar to {@link ColumnDescriptor.userConceptLabel} this holds the submitted {@link SelectId}.
	 */
	private SelectId selectId;

}
