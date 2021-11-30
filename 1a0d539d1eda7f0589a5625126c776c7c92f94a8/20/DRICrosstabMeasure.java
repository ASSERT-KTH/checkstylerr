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
package net.sf.dynamicreports.report.definition.crosstab;

import java.io.Serializable;
import java.util.List;

import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.TextAdjust;
import net.sf.dynamicreports.report.definition.DRIHyperLink;
import net.sf.dynamicreports.report.definition.datatype.DRIDataType;
import net.sf.dynamicreports.report.definition.expression.DRIExpression;
import net.sf.dynamicreports.report.definition.expression.DRIPropertyExpression;
import net.sf.dynamicreports.report.definition.expression.DRIValueFormatter;
import net.sf.dynamicreports.report.definition.style.DRIReportStyle;

/**
 * <p>DRICrosstabMeasure interface.</p>
 *
 * @author Ricardo Mariaca, Jan Moxter
 * 
 */
public interface DRICrosstabMeasure<T> extends Serializable {

    /**
     * <p>getName.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getName();

    /**
     * <p>getDataType.</p>
     *
     * @return a {@link net.sf.dynamicreports.report.definition.datatype.DRIDataType} object.
     */
    DRIDataType<? super T, T> getDataType();

    /**
     * <p>getExpression.</p>
     *
     * @return a {@link net.sf.dynamicreports.report.definition.expression.DRIExpression} object.
     */
    DRIExpression<?> getExpression();

    /**
     * <p>getPattern.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getPattern();

    /**
     * <p>getHorizontalTextAlignment.</p>
     *
     * @return a {@link net.sf.dynamicreports.report.constant.HorizontalTextAlignment} object.
     */
    HorizontalTextAlignment getHorizontalTextAlignment();

    /**
     * <p>getValueFormatter.</p>
     *
     * @return a {@link net.sf.dynamicreports.report.definition.expression.DRIValueFormatter} object.
     */
    DRIValueFormatter<?, ? super T> getValueFormatter();

    /**
     * <p>getStretchWithOverflow.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     * @deprecated replaced by {@link #getTextAdjust()}
     */
    @Deprecated Boolean getStretchWithOverflow();

    /**
     * <p>getTextAdjust.</p>
     *
     * @return a {@link net.sf.dynamicreports.report.constant.TextAdjust} object.
     */
    TextAdjust getTextAdjust();

    /**
     * <p>getHyperLink.</p>
     *
     * @return a {@link net.sf.dynamicreports.report.definition.DRIHyperLink} object.
     */
    DRIHyperLink getHyperLink();

    /**
     * <p>getPropertyExpressions.</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<DRIPropertyExpression> getPropertyExpressions();

    /**
     * <p>getStyles.</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<DRICrosstabCellStyle> getStyles();

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
}
