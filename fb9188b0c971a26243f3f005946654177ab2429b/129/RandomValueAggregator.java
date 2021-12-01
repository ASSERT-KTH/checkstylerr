package com.bakdata.conquery.models.query.queryplan.aggregators.specific.value;

import java.util.Random;

import com.bakdata.conquery.models.datasets.Column;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.externalservice.ResultType;
import com.bakdata.conquery.models.query.queryplan.aggregators.SingleColumnAggregator;
import com.bakdata.conquery.models.query.queryplan.clone.CloneContext;

/**
 * Aggregator, returning a random value of a column.
 * @param <VALUE> Value type of the column/return value
 */
public class RandomValueAggregator<VALUE> extends SingleColumnAggregator<VALUE> {

	private Object value;
	private int nValues = 0;
	private Bucket bucket;

	private final Random random = new Random();

	public RandomValueAggregator(Column column) {
		super(column);
	}

	/**
	 * length of sequence = m, but not known at event of sampling
	 *
	 * P(switching n-th value) = 1/n
	 *
	 * P(n-th value = output) 	= P(switching n-th value) * P(not switching values > n)
	 * = 1/n * n/m = 1/m
	 *
	 * @param bucket
	 * @param event
	 */
	@Override
	public void acceptEvent(Bucket bucket, int event) {
		if (!bucket.has(event, getColumn())) {
			return;
		}

		// Count how many values we have seen, so a draw is always evenly distributed
		nValues++;

		if (random.nextInt(nValues) == 0) {
			value = bucket.getRaw(event, getColumn());
			this.bucket = bucket;
		}
	}

	@Override
	public VALUE getAggregationResult() {
		if (bucket == null) {
			return null;
		}

		return (VALUE) getColumn().getTypeFor(bucket).createPrintValue(value);
	}

	@Override
	public RandomValueAggregator doClone(CloneContext ctx) {
		return new RandomValueAggregator(getColumn());
	}
	
	@Override
	public ResultType getResultType() {
		return ResultType.resolveResultType(getColumn().getType());
	}
}
