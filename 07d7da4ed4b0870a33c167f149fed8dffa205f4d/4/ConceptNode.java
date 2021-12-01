package com.bakdata.conquery.models.query.queryplan.specific;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.bakdata.conquery.models.concepts.ConceptElement;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.identifiable.ids.specific.BucketId;
import com.bakdata.conquery.models.identifiable.ids.specific.TableId;
import com.bakdata.conquery.models.query.QueryExecutionContext;
import com.bakdata.conquery.models.query.concept.filter.CQTable;
import com.bakdata.conquery.models.query.entity.Entity;
import com.bakdata.conquery.models.query.entity.EntityRow;
import com.bakdata.conquery.models.query.queryplan.QPChainNode;
import com.bakdata.conquery.models.query.queryplan.QPNode;
import com.bakdata.conquery.models.query.queryplan.clone.CloneContext;

public class ConceptNode extends QPChainNode {

	private final ConceptElement<?>[] concepts;
	private final long requiredBits;
	private final CQTable table;
	private boolean tableActive = false;
	private Map<BucketId, EntityRow> preCurrentRow = null;
	private EntityRow currentRow = null;

	public ConceptNode(ConceptElement[] concepts, long requiredBits, CQTable table, QPNode child) {
		super(child);
		this.concepts = concepts;
		this.requiredBits = requiredBits;
		this.table = table;
	}

	@Override
	protected void init() {
		preCurrentRow = entity.getCBlockPreSelect(table.getResolvedConnector().getId());
	}

	@Override
	public void nextTable(QueryExecutionContext ctx, TableId currentTable) {
		tableActive = table.getResolvedConnector().getTable().getId().equals(currentTable);
		if(tableActive) {
			super.nextTable(ctx.withConnector(table.getResolvedConnector()), currentTable);
		}
	}

	@Override
	public void nextBlock(Bucket bucket) {
		if (tableActive) {
			currentRow = Objects.requireNonNull(preCurrentRow.get(bucket.getId()));
			super.nextBlock(bucket);
		}
	}


	@Override
	public boolean isOfInterest(Entity entity) {
		return entity.hasConnector(table.getId());
	}

	@Override
	public boolean isOfInterest(Bucket bucket) {
		if (!tableActive) {
			return false;
		}

		EntityRow row = Objects.requireNonNull(preCurrentRow.get(bucket.getId()));

		int localEntity = bucket.toLocal(entity.getId());
		long bits = row.getCBlock().getIncludedConcepts()[localEntity];

		if((bits & requiredBits) != 0L || requiredBits == 0L) {
			return super.isOfInterest(bucket);
		}
		return false;
	}

	@Override
	public void acceptEvent(Bucket bucket, int event) {
		if (!tableActive) {
			return;
		}

		//check concepts
		int[] mostSpecificChildren;
		if (currentRow.getCBlock().getMostSpecificChildren() != null
			&& ((mostSpecificChildren = currentRow.getCBlock().getMostSpecificChildren()[event]) != null)) {

			for (ConceptElement<?> ce : concepts) { //see #177  we could improve this by building a a prefix tree over concepts.prefix
				if (ce.matchesPrefix(mostSpecificChildren)) {
					getChild().acceptEvent(bucket, event);
				}
			}
		}
		else {
			for (ConceptElement ce : concepts) { //see #178  we could improve this by building a a prefix tree over concepts.prefix
				if (ce.getConcept() == ce) {
					getChild().acceptEvent(bucket, event);
				}
			}
		}
	}

	@Override
	public boolean isContained() {
		return getChild().isContained();
	}

	@Override
	public QPNode doClone(CloneContext ctx) {
		return new ConceptNode(concepts, requiredBits, table, ctx.clone(getChild()));
	}

	@Override
	public void collectRequiredTables(Set<TableId> requiredTables) {
		super.collectRequiredTables(requiredTables);
		requiredTables.add(table.getResolvedConnector().getTable().getId());
	}
}
