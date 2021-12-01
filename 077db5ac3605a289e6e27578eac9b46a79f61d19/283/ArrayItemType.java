package org.eclipse.milo.opcua.sdk.client.model.types.variables;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.AxisScaleEnumeration;
import org.eclipse.milo.opcua.stack.core.types.structured.EUInformation;
import org.eclipse.milo.opcua.stack.core.types.structured.Range;

public interface ArrayItemType extends DataItemType {
    QualifiedProperty<Range> INSTRUMENT_RANGE = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "InstrumentRange",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=884"),
        ValueRanks.Scalar,
        Range.class
    );

    QualifiedProperty<Range> EU_RANGE = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "EURange",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=884"),
        ValueRanks.Scalar,
        Range.class
    );

    QualifiedProperty<EUInformation> ENGINEERING_UNITS = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "EngineeringUnits",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=887"),
        ValueRanks.Scalar,
        EUInformation.class
    );

    QualifiedProperty<LocalizedText> TITLE = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "Title",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=21"),
        ValueRanks.Scalar,
        LocalizedText.class
    );

    QualifiedProperty<AxisScaleEnumeration> AXIS_SCALE_TYPE = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "AxisScaleType",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=12077"),
        ValueRanks.Scalar,
        AxisScaleEnumeration.class
    );

    /**
     * Get the local value of the InstrumentRange Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the InstrumentRange Node.
     * @throws UaException if an error occurs creating or getting the InstrumentRange Node.
     */
    Range getInstrumentRange() throws UaException;

    /**
     * Set the local value of the InstrumentRange Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param instrumentRange the local value to set for the InstrumentRange Node.
     * @throws UaException if an error occurs creating or getting the InstrumentRange Node.
     */
    void setInstrumentRange(Range instrumentRange) throws UaException;

    /**
     * Read the value of the InstrumentRange Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link Range} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    Range readInstrumentRange() throws UaException;

    /**
     * Write a new value for the InstrumentRange Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param instrumentRange the {@link Range} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeInstrumentRange(Range instrumentRange) throws UaException;

    /**
     * An asynchronous implementation of {@link #readInstrumentRange()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends Range> readInstrumentRangeAsync();

    /**
     * An asynchronous implementation of {@link #writeInstrumentRange(Range)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeInstrumentRangeAsync(Range instrumentRange);

    /**
     * Get the InstrumentRange {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the InstrumentRange {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getInstrumentRangeNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getInstrumentRangeNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getInstrumentRangeNodeAsync();

    /**
     * Get the local value of the EURange Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the EURange Node.
     * @throws UaException if an error occurs creating or getting the EURange Node.
     */
    Range getEuRange() throws UaException;

    /**
     * Set the local value of the EURange Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param euRange the local value to set for the EURange Node.
     * @throws UaException if an error occurs creating or getting the EURange Node.
     */
    void setEuRange(Range euRange) throws UaException;

    /**
     * Read the value of the EURange Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link Range} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    Range readEuRange() throws UaException;

    /**
     * Write a new value for the EURange Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param euRange the {@link Range} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeEuRange(Range euRange) throws UaException;

    /**
     * An asynchronous implementation of {@link #readEuRange()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends Range> readEuRangeAsync();

    /**
     * An asynchronous implementation of {@link #writeEuRange(Range)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeEuRangeAsync(Range euRange);

    /**
     * Get the EURange {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the EURange {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getEuRangeNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getEuRangeNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getEuRangeNodeAsync();

    /**
     * Get the local value of the EngineeringUnits Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the EngineeringUnits Node.
     * @throws UaException if an error occurs creating or getting the EngineeringUnits Node.
     */
    EUInformation getEngineeringUnits() throws UaException;

    /**
     * Set the local value of the EngineeringUnits Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param engineeringUnits the local value to set for the EngineeringUnits Node.
     * @throws UaException if an error occurs creating or getting the EngineeringUnits Node.
     */
    void setEngineeringUnits(EUInformation engineeringUnits) throws UaException;

    /**
     * Read the value of the EngineeringUnits Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link EUInformation} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    EUInformation readEngineeringUnits() throws UaException;

    /**
     * Write a new value for the EngineeringUnits Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param engineeringUnits the {@link EUInformation} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeEngineeringUnits(EUInformation engineeringUnits) throws UaException;

    /**
     * An asynchronous implementation of {@link #readEngineeringUnits()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends EUInformation> readEngineeringUnitsAsync();

    /**
     * An asynchronous implementation of {@link #writeEngineeringUnits(EUInformation)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeEngineeringUnitsAsync(EUInformation engineeringUnits);

    /**
     * Get the EngineeringUnits {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the EngineeringUnits {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getEngineeringUnitsNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getEngineeringUnitsNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getEngineeringUnitsNodeAsync();

    /**
     * Get the local value of the Title Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the Title Node.
     * @throws UaException if an error occurs creating or getting the Title Node.
     */
    LocalizedText getTitle() throws UaException;

    /**
     * Set the local value of the Title Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param title the local value to set for the Title Node.
     * @throws UaException if an error occurs creating or getting the Title Node.
     */
    void setTitle(LocalizedText title) throws UaException;

    /**
     * Read the value of the Title Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link LocalizedText} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    LocalizedText readTitle() throws UaException;

    /**
     * Write a new value for the Title Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param title the {@link LocalizedText} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeTitle(LocalizedText title) throws UaException;

    /**
     * An asynchronous implementation of {@link #readTitle()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends LocalizedText> readTitleAsync();

    /**
     * An asynchronous implementation of {@link #writeTitle(LocalizedText)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeTitleAsync(LocalizedText title);

    /**
     * Get the Title {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the Title {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getTitleNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getTitleNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getTitleNodeAsync();

    /**
     * Get the local value of the AxisScaleType Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the AxisScaleType Node.
     * @throws UaException if an error occurs creating or getting the AxisScaleType Node.
     */
    AxisScaleEnumeration getAxisScaleType() throws UaException;

    /**
     * Set the local value of the AxisScaleType Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param axisScaleType the local value to set for the AxisScaleType Node.
     * @throws UaException if an error occurs creating or getting the AxisScaleType Node.
     */
    void setAxisScaleType(AxisScaleEnumeration axisScaleType) throws UaException;

    /**
     * Read the value of the AxisScaleType Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link AxisScaleEnumeration} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    AxisScaleEnumeration readAxisScaleType() throws UaException;

    /**
     * Write a new value for the AxisScaleType Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param axisScaleType the {@link AxisScaleEnumeration} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeAxisScaleType(AxisScaleEnumeration axisScaleType) throws UaException;

    /**
     * An asynchronous implementation of {@link #readAxisScaleType()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends AxisScaleEnumeration> readAxisScaleTypeAsync();

    /**
     * An asynchronous implementation of {@link #writeAxisScaleType(AxisScaleEnumeration)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeAxisScaleTypeAsync(AxisScaleEnumeration axisScaleType);

    /**
     * Get the AxisScaleType {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the AxisScaleType {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getAxisScaleTypeNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getAxisScaleTypeNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getAxisScaleTypeNodeAsync();
}
