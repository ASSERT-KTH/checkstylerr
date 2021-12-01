package com.bakdata.conquery.models.events;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.bakdata.conquery.io.jackson.serializer.CBlockDeserializer;
import com.bakdata.conquery.models.common.daterange.CDateRange;
import com.bakdata.conquery.models.concepts.Concept;
import com.bakdata.conquery.models.concepts.ConceptElement;
import com.bakdata.conquery.models.concepts.tree.ConceptTreeChild;
import com.bakdata.conquery.models.concepts.tree.ConceptTreeNode;
import com.bakdata.conquery.models.concepts.tree.TreeConcept;
import com.bakdata.conquery.models.identifiable.IdentifiableImpl;
import com.bakdata.conquery.models.identifiable.ids.specific.BucketId;
import com.bakdata.conquery.models.identifiable.ids.specific.CBlockId;
import com.bakdata.conquery.models.identifiable.ids.specific.ConnectorId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Metadata for connection of {@link Bucket} and {@link Concept}
 * <p>
 * Pre-computed assignment of {@link TreeConcept}.
 */
// TODO move to Bucket
@Getter
@Setter
@NoArgsConstructor
@JsonDeserialize(using = CBlockDeserializer.class)
public class CBlock extends IdentifiableImpl<CBlockId> {

	/**
	 * Estimate the memory usage of CBlocks.
	 * @param depthEstimate estimate of depth of mostSpecificChildren
	 */
	public static long estimateMemoryBytes(long entities, long entries, double depthEstimate){
		return Math.round(entities *
						  (
						  		Integer.BYTES + Long.BYTES // includedConcepts
								+ 2 * Integer.BYTES // minDate
								+ 2 * Integer.BYTES // maxDate
						  )
						  + entries * depthEstimate * Integer.BYTES // mostSpecificChildren (rough estimate, not resident on ManagerNode)
		);
	}

	private BucketId bucket;
	@NotNull
	private ConnectorId connector;

	/**
	 * Bloom filter per entity for the first 64 {@link ConceptTreeChild}.
	 */
	private Int2LongArrayMap includedConcepts = new Int2LongArrayMap();

	/**
	 * Statistic for fast lookup if entity is of interest.
	 * Int array for memory performance.
	 */
	//TODO wrap access in private methods and change to a more appropriate class
	private Map<Integer, Integer> minDate = new Int2IntArrayMap();
	private Map<Integer, Integer> maxDate = new Int2IntArrayMap();
	/**
	 * Represents the path in a {@link TreeConcept} to optimize lookup.
	 * Nodes in the tree are simply enumerated.
	 */
	// todo, can this be implemented using a store or at least with bytes only?
	private int[][] mostSpecificChildren;

	public CBlock(BucketId bucket, ConnectorId connector) {
		this.bucket = bucket;
		this.connector = connector;
	}

	public static long calculateBitMask(List<ConceptElement<?>> concepts) {
		long mask = 0;
		for (ConceptElement<?> concept : concepts) {
			mask |= concept.calculateBitMask();
		}
		return mask;
	}


	public int[] getEventMostSpecificChild(int event) {
		if(mostSpecificChildren == null){
			return null;
		}

		return mostSpecificChildren[event];
	}

	public CDateRange getEntityDateRange(int entity) {
		return CDateRange.of(getEntityMinDate(entity), getEntityMaxDate(entity));
	}

	public int getEntityMinDate(int entity) {
		return minDate.getOrDefault(entity, Integer.MIN_VALUE);
	}

	public int getEntityMaxDate(int entity) {
		return maxDate.getOrDefault(entity, Integer.MAX_VALUE);
	}

	@Override
	@JsonIgnore
	public CBlockId createId() {
		return new CBlockId(bucket, connector);
	}

	public void initIndizes(int bucketSize) {
		includedConcepts = new Int2LongArrayMap(bucketSize);
		includedConcepts.defaultReturnValue(0);

		minDate = new Int2IntArrayMap(bucketSize);
		maxDate = new Int2IntArrayMap(bucketSize);
	}

	public void addEntityDateRange(int entity, CDateRange range) {
		if (range.hasLowerBound()) {
			final int minValue = range.getMinValue();

			if (!minDate.containsKey(entity)) {
				minDate.put(entity, minValue);
			}
			else {
				int min = Math.min(minDate.get(entity), minValue);
				minDate.put(entity, min);
			}
		}

		if (range.hasUpperBound()) {
			final int maxValue = range.getMaxValue();

			if (!maxDate.containsKey(entity)) {
				maxDate.put(entity, maxValue);
			}
			else {
				int min = Math.max(maxDate.get(entity), maxValue);
				maxDate.put(entity, min);
			}
		}
	}

	public void addIncludedConcept(int entity, ConceptTreeNode<?> node) {
		final long mask = node.calculateBitMask();
		final long original = includedConcepts.getOrDefault(entity, 0);
		includedConcepts.put(entity, original | mask);
	}

	public boolean isConceptIncluded(int entity, long requiredBits) {
		if (requiredBits == 0L) {
			return true;
		}

		long bits = includedConcepts.get(entity);

		return (bits & requiredBits) != 0L;
	}
}
