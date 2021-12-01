package com.bakdata.conquery.models.execution;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.bakdata.conquery.apiv1.QueryDescription;
import com.bakdata.conquery.io.jackson.serializer.NsIdRefCollection;
import com.bakdata.conquery.models.datasets.SecondaryIdDescription;
import com.bakdata.conquery.models.error.ConqueryErrorInfo;
import com.bakdata.conquery.models.identifiable.ids.specific.GroupId;
import com.bakdata.conquery.models.query.ColumnDescriptor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

/**
 * This status holds extensive information about the query description and meta data that is computational heavy
 * and can produce a larger payload to requests.
 * It should only be rendered, when a client asks for a specific execution, not if a list of executions is requested.
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
public class FullExecutionStatus extends ExecutionStatus {

	/**
	 * Holds a description for each column, present in the result.
	 */
	private List<ColumnDescriptor> columnDescriptions;

	/**
	 * Indicates if the concepts that are included in the query description can be accessed by the user.
	 */
	private boolean canExpand;

	/**
	 * Is set to the query description if the user can expand all included concepts.
	 */
	private QueryDescription query;

	/**
	 * Is set when the QueryFailed
	 */
	private ConqueryErrorInfo error;

	/**
	 * The groups this execution is shared with.
	 */
	private Collection<GroupId> groups;

	/**
	 * Possible {@link SecondaryIdDescription}s available, of {@link com.bakdata.conquery.models.concepts.Concept}s used in this Query.
	 */
	@NsIdRefCollection
	private Set<SecondaryIdDescription> availableSecondaryIds;
}
