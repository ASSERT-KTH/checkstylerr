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
package net.sf.dynamicreports.report.base;

import net.sf.dynamicreports.report.constant.Constants;
import net.sf.dynamicreports.report.definition.DRIField;
import net.sf.dynamicreports.report.definition.datatype.DRIDataType;
import org.apache.commons.lang3.Validate;

/**
 * <p>DRField class.</p>
 *
 * @author Ricardo Mariaca
 * 
 */
public class DRField<T> implements DRIField<T> {
    private static final long serialVersionUID = Constants.SERIAL_VERSION_UID;

    private String name;
    private Class<? super T> valueClass;
    private DRIDataType<? super T, T> dataType;
    private String description;

    /**
     * <p>Constructor for DRField.</p>
     *
     * @param name       a {@link java.lang.String} object.
     * @param valueClass a {@link java.lang.Class} object.
     */
    public DRField(String name, Class<? super T> valueClass) {
        Validate.notEmpty(name, "name must not be empty");
        Validate.notNull(valueClass, "valueClass must not be null");
        this.name = name;
        this.valueClass = valueClass;
    }

    /** {@inheritDoc} */
    @Override
    public DRIDataType<? super T, T> getDataType() {
        return dataType;
    }

    /**
     * <p>Setter for the field <code>dataType</code>.</p>
     *
     * @param dataType a {@link net.sf.dynamicreports.report.definition.datatype.DRIDataType} object.
     */
    public void setDataType(DRIDataType<? super T, T> dataType) {
        this.dataType = dataType;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? super T> getValueClass() {
        return valueClass;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * <p>Setter for the field <code>description</code>.</p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
