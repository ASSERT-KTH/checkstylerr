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
package net.sf.dynamicreports.report.base.expression;

import net.sf.dynamicreports.report.ReportUtils;
import net.sf.dynamicreports.report.constant.Constants;
import net.sf.dynamicreports.report.definition.expression.DRISimpleExpression;
import org.apache.commons.lang3.Validate;

/**
 * <p>Abstract AbstractSimpleExpression class.</p>
 *
 * @author Ricardo Mariaca
 * 
 */
public abstract class AbstractSimpleExpression<T> implements DRISimpleExpression<T> {
    private static final long serialVersionUID = Constants.SERIAL_VERSION_UID;

    private String name;

    /**
     * <p>Constructor for AbstractSimpleExpression.</p>
     */
    protected AbstractSimpleExpression() {
        this.name = ReportUtils.generateUniqueName("simpleExpression");
    }

    /**
     * <p>Constructor for AbstractSimpleExpression.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    protected AbstractSimpleExpression(String name) {
        Validate.notEmpty(name, "name must not be empty");
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public Class<? super T> getValueClass() {
        return (Class<T>) ReportUtils.getGenericClass(this, 0);
    }
}
