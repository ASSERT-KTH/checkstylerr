/*
 * DynamicReports - Free Java reporting library for creating reports dynamically
 *
 * Copyright (C) 2010 - 2018 Ricardo Mariaca and the Dynamic Reports Contributors
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
package net.sf.dynamicreports.report.base.crosstab;

import net.sf.dynamicreports.report.constant.Constants;
import net.sf.dynamicreports.report.definition.crosstab.DRICrosstabColumnGroup;

/**
 * <p>DRCrosstabColumnGroup class.</p>
 *
 * @author Ricardo Mariaca
 * 
 */
public class DRCrosstabColumnGroup<T> extends DRCrosstabGroup<T> implements DRICrosstabColumnGroup<T> {
    private static final long serialVersionUID = Constants.SERIAL_VERSION_UID;

    private Integer headerHeight;
    private Integer totalHeaderWidth;

    /** {@inheritDoc} */
    @Override
    public Integer getHeaderHeight() {
        return headerHeight;
    }

    /**
     * <p>Setter for the field <code>headerHeight</code>.</p>
     *
     * @param headerHeight a {@link java.lang.Integer} object.
     */
    public void setHeaderHeight(Integer headerHeight) {
        this.headerHeight = headerHeight;
    }

    /** {@inheritDoc} */
    @Override
    public Integer getTotalHeaderWidth() {
        return totalHeaderWidth;
    }

    /**
     * <p>Setter for the field <code>totalHeaderWidth</code>.</p>
     *
     * @param totalHeaderWidth a {@link java.lang.Integer} object.
     */
    public void setTotalHeaderWidth(Integer totalHeaderWidth) {
        this.totalHeaderWidth = totalHeaderWidth;
    }
}
