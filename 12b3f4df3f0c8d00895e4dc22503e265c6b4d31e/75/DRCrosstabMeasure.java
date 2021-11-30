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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;

import net.sf.dynamicreports.report.ReportUtils;
import net.sf.dynamicreports.report.base.DRHyperLink;
import net.sf.dynamicreports.report.constant.Constants;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.TextAdjust;
import net.sf.dynamicreports.report.definition.crosstab.DRICrosstabCellStyle;
import net.sf.dynamicreports.report.definition.crosstab.DRICrosstabMeasure;
import net.sf.dynamicreports.report.definition.datatype.DRIDataType;
import net.sf.dynamicreports.report.definition.expression.DRIExpression;
import net.sf.dynamicreports.report.definition.expression.DRIPropertyExpression;
import net.sf.dynamicreports.report.definition.expression.DRIValueFormatter;
import net.sf.dynamicreports.report.definition.style.DRIReportStyle;

/**
 * <p>DRCrosstabMeasure class.</p>
 *
 * @author Ricardo Mariaca, Jan Moxter
 * 
 */
public class DRCrosstabMeasure<T> implements DRICrosstabMeasure<T> {
    private static final long serialVersionUID = Constants.SERIAL_VERSION_UID;

    private final String name;
    private final DRIExpression<?> expression;
    private DRIDataType<? super T, T> dataType;
    private String pattern;
    private HorizontalTextAlignment horizontalTextAlignment;
    private DRIValueFormatter<?, ? super T> valueFormatter;
    private Boolean stretchWithOverflow;
    private TextAdjust textAdjust;
    private DRHyperLink hyperLink;
    private List<DRIPropertyExpression> propertyExpressions;
    private List<DRICrosstabCellStyle> styles;
    private DRIExpression<?> titleExpression;
    private DRIReportStyle titleStyle;

    /**
     * <p>Constructor for DRCrosstabMeasure.</p>
     *
     * @param expression a {@link net.sf.dynamicreports.report.definition.expression.DRIExpression} object.
     */
    public DRCrosstabMeasure(final DRIExpression<?> expression) {
        Validate.notNull(expression, "expression must not be null");
        this.expression = expression;
        this.name = ReportUtils.generateUniqueName("crosstabMeasure");
        this.styles = new ArrayList<>();
        propertyExpressions = new ArrayList<>();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public DRIExpression<?> getExpression() {
        return expression;
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
    public void setDataType(final DRIDataType<? super T, T> dataType) {
        this.dataType = dataType;
    }

    /** {@inheritDoc} */
    @Override
    public String getPattern() {
        return pattern;
    }

    /**
     * <p>Setter for the field <code>pattern</code>.</p>
     *
     * @param pattern a {@link java.lang.String} object.
     */
    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

    /** {@inheritDoc} */
    @Override
    public HorizontalTextAlignment getHorizontalTextAlignment() {
        return horizontalTextAlignment;
    }

    /**
     * <p>Setter for the field <code>horizontalTextAlignment</code>.</p>
     *
     * @param horizontalTextAlignment a {@link net.sf.dynamicreports.report.constant.HorizontalTextAlignment} object.
     */
    public void setHorizontalTextAlignment(final HorizontalTextAlignment horizontalTextAlignment) {
        this.horizontalTextAlignment = horizontalTextAlignment;
    }

    /** {@inheritDoc} */
    @Override
    public DRIValueFormatter<?, ? super T> getValueFormatter() {
        return valueFormatter;
    }

    /**
     * <p>Setter for the field <code>valueFormatter</code>.</p>
     *
     * @param valueFormatter a {@link net.sf.dynamicreports.report.definition.expression.DRIValueFormatter} object.
     */
    public void setValueFormatter(final DRIValueFormatter<?, ? super T> valueFormatter) {
        this.valueFormatter = valueFormatter;
    }

    /** {@inheritDoc} */
    @Override
    public Boolean getStretchWithOverflow() {
        return stretchWithOverflow;
    }

    /**
     * <p>Setter for the field <code>stretchWithOverflow</code>.</p>
     *
     * @param stretchWithOverflow a {@link java.lang.Boolean} object.
     * @deprecated replaced by {@link #setTextAdjust(TextAdjust)}
     */
    @Deprecated
    public void setStretchWithOverflow(final Boolean stretchWithOverflow) {
        this.stretchWithOverflow = stretchWithOverflow;
    }

    /** {@inheritDoc} */
    @Override
    public TextAdjust getTextAdjust() {
        return this.textAdjust;
    }

    /**
     * <p>Setter for the field <code>textAdjust</code>.</p>
     *
     * @param textAdjust a {@link net.sf.dynamicreports.report.constant.TextAdjust} object.
     */
    public void setTextAdjust(final TextAdjust textAdjust) {
        this.textAdjust = textAdjust;
    }

    /** {@inheritDoc} */
    @Override
    public DRHyperLink getHyperLink() {
        return hyperLink;
    }

    /**
     * <p>Setter for the field <code>hyperLink</code>.</p>
     *
     * @param hyperLink a {@link net.sf.dynamicreports.report.base.DRHyperLink} object.
     */
    public void setHyperLink(final DRHyperLink hyperLink) {
        this.hyperLink = hyperLink;
    }

    /** {@inheritDoc} */
    @Override
    public List<DRIPropertyExpression> getPropertyExpressions() {
        return propertyExpressions;
    }

    /**
     * <p>Setter for the field <code>propertyExpressions</code>.</p>
     *
     * @param propertyExpressions a {@link java.util.List} object.
     */
    public void setPropertyExpressions(final List<DRIPropertyExpression> propertyExpressions) {
        this.propertyExpressions = propertyExpressions;
    }

    /**
     * <p>addPropertyExpression.</p>
     *
     * @param propertyExpression a {@link net.sf.dynamicreports.report.definition.expression.DRIPropertyExpression} object.
     */
    public void addPropertyExpression(final DRIPropertyExpression propertyExpression) {
        Validate.notNull(propertyExpression, "propertyExpression must not be null");
        this.propertyExpressions.add(propertyExpression);
    }

    /** {@inheritDoc} */
    @Override
    public List<DRICrosstabCellStyle> getStyles() {
        return styles;
    }

    /**
     * <p>setStyle.</p>
     *
     * @param styles a {@link java.util.List} object.
     */
    public void setStyle(final List<DRICrosstabCellStyle> styles) {
        this.styles = styles;
    }

    /** {@inheritDoc} */
    @Override
    public DRIExpression<?> getTitleExpression() {
        return titleExpression;
    }

    /**
     * <p>Setter for the field <code>titleExpression</code>.</p>
     *
     * @param titleExpression a {@link net.sf.dynamicreports.report.definition.expression.DRIExpression} object.
     */
    public void setTitleExpression(final DRIExpression<?> titleExpression) {
        this.titleExpression = titleExpression;
    }

    /** {@inheritDoc} */
    @Override
    public DRIReportStyle getTitleStyle() {
        return titleStyle;
    }

    /**
     * <p>Setter for the field <code>titleStyle</code>.</p>
     *
     * @param titleStyle a {@link net.sf.dynamicreports.report.definition.style.DRIReportStyle} object.
     */
    public void setTitleStyle(final DRIReportStyle titleStyle) {
        this.titleStyle = titleStyle;
    }
}
