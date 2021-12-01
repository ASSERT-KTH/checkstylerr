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

import com.cronutils.mapper.WeekDay;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraintsBuilder;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.parser.CronParserField;
import com.cronutils.utils.Preconditions;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

/**
 * This class generates the actual days of month matching the "days of week" specification if the.
 * specification is a range like SUN-TUE or MON-FRI. Only a range is supported. It accomplishes this
 * by creating an instance of the OnDayOfWeekValuesGenerator for each day of week needed and then
 * aggregating the values.
 *
 * <p>The methods:
 * <ul>
 * <li>generateNextValue()
 * <li>generatePreviousValue()
 * </ul>
 * are not implemented and WILL FAIL logically when called.
 *
 * @author phethmon
 */
class BetweenDayOfWeekValueGenerator extends FieldValueGenerator {
    private final int year;
    private final int month;
    private final WeekDay mondayDoWValue;
    private final Set<Integer> dowValidValues;

    public BetweenDayOfWeekValueGenerator(final CronField cronField, final int year, final int month, final WeekDay mondayDoWValue) {
        super(cronField);
        Preconditions.checkArgument(CronFieldName.DAY_OF_WEEK.equals(cronField.getField()), "CronField does not belong to day of week");
        this.year = year;
        this.month = month;
        this.mondayDoWValue = mondayDoWValue;
        dowValidValues = new HashSet<>();
        final Between between = (Between) cronField.getExpression();
        int from = (Integer) between.getFrom().getValue();
        final int to = (Integer) between.getTo().getValue();
        if (to >= from) {
            IntStream.rangeClosed(from, to).forEach(dowValidValues::add);
        } else {
            // Handle a rollover situation for #340, ex. FRI-WED should be handled as FRI-SAT,SUN-WED
            IntStream.rangeClosed(from, cronField.getConstraints().getEndRange()).forEach(dowValidValues::add);
            IntStream.rangeClosed(cronField.getConstraints().getStartRange(), to).forEach(dowValidValues::add);
        }
    }

    @Override
    protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(final int start, final int end) {
        final List<Integer> values = new ArrayList<>();
        for (Integer dayOfWeek: dowValidValues) {
            // Build a CronField representing a single day of the week
            final FieldConstraintsBuilder fcb = FieldConstraintsBuilder.instance().forField(CronFieldName.DAY_OF_WEEK);
            final CronParserField parser = new CronParserField(CronFieldName.DAY_OF_WEEK, fcb.createConstraintsInstance());
            final CronField cronField = parser.parse(dayOfWeek.toString());

            // now a generator for matching days
            final OnDayOfWeekValueGenerator odow = new OnDayOfWeekValueGenerator(cronField, year, month, mondayDoWValue);

            // get the list of matching days
            final List<Integer> candidatesList = odow.generateCandidates(start, end);

            // add them to the master list
            if (candidatesList != null) {
                values.addAll(candidatesList);
            }
        }
        Collections.sort(values);
        return values;
    }

    @Override
    protected boolean matchesFieldExpressionClass(final FieldExpression fieldExpression) {
        return fieldExpression instanceof Between;
    }

    @Override
    public int generateNextValue(final int reference) throws NoSuchValueException {
        // This method does not logically work.
        return 0;
    }

    @Override
    public int generatePreviousValue(final int reference) throws NoSuchValueException {
        // This method does not logically work.
        return 0;
    }

    @Override
    public boolean isMatch(final int value) {
        // DayOfWeek getValue returns 1 (Monday) - 7 (Sunday),
        // so we should factor in the monday DoW used to generate
        // the valid DoW values
        final int localDateDoW = LocalDate.of(year, month, value).getDayOfWeek().getValue();

        // Sunday's value is mondayDoWValue-1 when generating the valid values
        // Ex.
        // cron4j 0(Sun)-6(Sat), mondayDoW = 1
        // quartz 1(Sun)-7(Sat), mondayDoW = 2

        // modulo 7 to convert Sunday from 7 to 0 and adjust to match the mondayDoWValue
        final int cronDoW = localDateDoW % 7 + (mondayDoWValue.getMondayDoWValue() - 1);

        return dowValidValues.contains(cronDoW);
    }
}
