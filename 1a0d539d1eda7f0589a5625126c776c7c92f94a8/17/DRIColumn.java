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
package net.sf.dynamicreports.report.definition.column;

import java.util.List;

import net.sf.dynamicreports.report.constant.ComponentDimensionType;
import net.sf.dynamicreports.report.constant.TextAdjust;
import net.sf.dynamicreports.report.definition.component.DRIComponent;
import net.sf.dynamicreports.report.definition.expression.DRIExpression;
import net.sf.dynamicreports.report.definition.expression.DRIPropertyExpression;
import net.sf.dynamicreports.report.definition.grid.DRIColumnGridComponent;
import net.sf.dynamicreports.report.definition.style.DRIReportStyle;

/**
 * <p>DRIColumn interface.</p>
 *
 * @author Ricardo Mariaca, Jan Moxter
 * 
 */
public interface DRIColumn<T extends DRIComponent> extends DRIColumnGridComponent {

    /**
     * <p>getComponent.</p>
     *
     * @return a T object.
     */
    T getComponent();

    /**
     * <p>getTitleExpression.</p>
     *
     * @return a {@link net.sf.dynamicreports.report.definition.expression.DRIExpression} object.
     */
    DRIExpression<?> getTitleExpression();

    /**
     * <p>getTitleStyle.</p>
     *
     * @return a {@link net.sf.dynamicreports.report.definition.style.DRIReportStyle} object.
     */
    DRIReportStyle getTitleStyle();

    /**
     * <p>getTitleHeight.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    Integer getTitleHeight();

    /**
     * <p>getTitleHeightType.</p>
     *
     * @return a {@link net.sf.dynamicreports.report.constant.ComponentDimensionType} object.
     */
    ComponentDimensionType getTitleHeightType();

    /**
     * <p>getTitleRows.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    Integer getTitleRows();

    /**
     * <p>getTitleStretchWithOverflow.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     * @deprecated replaced by {@link #getTitleTextAdjust()}
     */
    @Deprecated
    Boolean getTitleStretchWithOverflow();

    /**
     * <p>getTitlePropertyExpressions.</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<DRIPropertyExpression> getTitlePropertyExpressions();

    /**
     * <p>getName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getName();

    /**
     * <p>getTitleTextAdjust.</p>
     *
     * @return a {@link net.sf.dynamicreports.report.constant.TextAdjust} object.
     */
    TextAdjust getTitleTextAdjust();
}
