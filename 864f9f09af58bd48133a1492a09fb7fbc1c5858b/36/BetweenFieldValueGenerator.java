/*
 * Copyright 2015 jmrozanec
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cronutils.model.time.generator;

import com.cronutils.model.field.CronField;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;

import java.util.ArrayList;
import java.util.List;

class BetweenFieldValueGenerator extends FieldValueGenerator {

    public BetweenFieldValueGenerator(final CronField cronField) {
        super(cronField);
    }

    @Override
    public int generateNextValue(final int reference) throws NoSuchValueException {
        final Between between = (Between) cronField.getExpression();
        //TODO validate from/to logic
        int candidate = reference;
        do {
            ++candidate;
        } while (candidate < map(between.getFrom()));

        if (candidate > map(between.getTo())) {
            throw new NoSuchValueException();
        }
        return candidate;
    }

    @Override
    public int generatePreviousValue(final int reference) throws NoSuchValueException {
        final Between between = (Between) cronField.getExpression();
        //TODO deal with from/to logic, to ensure correct values are assumed
        int candidate = reference;
        do {
            --candidate;
        } while (candidate > map(between.getTo()));

        if (candidate < map(between.getFrom())) {
            throw new NoSuchValueException();
        }
        return candidate;
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(final int start, final int end) {
        final List<Integer> values = new ArrayList<>();
        //check overlapping ranges: x1 <= y2 && y1 <= x2
        final Between between = (Between) cronField.getExpression();
        final int expressionStart = map(between.getFrom());
        final int expressionEnd = map(between.getTo());
        int rangestart = start;
        int rangeend = end;
        if (start <= expressionEnd && expressionStart <= end) { //ranges overlap
            if (expressionEnd < end) {
                rangeend = expressionEnd;
            }
            if (map(between.getFrom()) > start) {
                rangestart = expressionStart;
            }
            try {
                if (rangestart != start) {
                    values.add(rangestart);
                }
                int reference = generateNextValue(rangestart);
                while (reference < rangeend) {
                    values.add(reference);
                    reference = generateNextValue(reference);
                }
                if (rangeend != end) {
                    values.add(reference);
                }
            } catch (final NoSuchValueException e) {
                // TODO: Explain why this exception is ignored
            }
        }
        return values;
    }

    @Override
    public boolean isMatch(final int value) {
        final Between between = (Between) cronField.getExpression();
        return value >= map(between.getFrom()) && value <= map(between.getTo());
    }

    @Override
    protected boolean matchesFieldExpressionClass(final FieldExpression fieldExpression) {
        return fieldExpression instanceof Between;
    }

    static int map(final FieldValue<?> fieldValue) {
        if (fieldValue instanceof IntegerFieldValue) {
            return ((IntegerFieldValue) fieldValue).getValue();
        }
        throw new IllegalArgumentException("Non integer values at intervals are not fully supported yet.");
    }
}
