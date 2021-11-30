/*
 * DynamicReports - Free Java reporting library for creating reports dynamically
 *
 * Copyright (C) 2010 - 2018 Ricardo Mariaca and the Dynamic Reports Contributors
 * http://www.dynamicreports.org
 *
 * This file is part of DynamicReports.
 *
 * DynamicReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DynamicReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with DynamicReports. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.dynamicreports.report.builder.condition;

import net.sf.dynamicreports.report.constant.Constants;
import net.sf.dynamicreports.report.definition.DRIValue;

/**
 * <p>GreaterOrEqualsValueExpression class.</p>
 *
 * @author Ricardo Mariaca (r.mariaca@dynamicreports.org)
 * 
 */
public class GreaterOrEqualsValueExpression<T extends Number> extends AbstractValueExpression<T> {
    private static final long serialVersionUID = Constants.SERIAL_VERSION_UID;

    /**
     * <p>Constructor for GreaterOrEqualsValueExpression.</p>
     *
     * @param value  a {@link net.sf.dynamicreports.report.definition.DRIValue} object.
     * @param number a {@link java.lang.Number} object.
     */
    public GreaterOrEqualsValueExpression(DRIValue<T> value, Number number) {
        super(value, number);
    }

    /** {@inheritDoc} */
    @Override
    protected Boolean compare(Number actualValue, Number number) {
        return actualValue.doubleValue() >= number.doubleValue();
    }
}
