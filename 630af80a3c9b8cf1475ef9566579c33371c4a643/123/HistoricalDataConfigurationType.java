package org.eclipse.milo.opcua.sdk.client.model.types.objects;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.model.types.variables.PropertyType;
import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.ExceptionDeviationFormat;

public interface HistoricalDataConfigurationType extends BaseObjectType {
    QualifiedProperty<Boolean> STEPPED = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "Stepped",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=1"),
        ValueRanks.Scalar,
        Boolean.class
    );

    QualifiedProperty<String> DEFINITION = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "Definition",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=12"),
        ValueRanks.Scalar,
        String.class
    );

    QualifiedProperty<Double> MAX_TIME_INTERVAL = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "MaxTimeInterval",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=290"),
        ValueRanks.Scalar,
        Double.class
    );

    QualifiedProperty<Double> MIN_TIME_INTERVAL = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "MinTimeInterval",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=290"),
        ValueRanks.Scalar,
        Double.class
    );

    QualifiedProperty<Double> EXCEPTION_DEVIATION = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "ExceptionDeviation",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=11"),
        ValueRanks.Scalar,
        Double.class
    );

    QualifiedProperty<ExceptionDeviationFormat> EXCEPTION_DEVIATION_FORMAT = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "ExceptionDeviationFormat",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=890"),
        ValueRanks.Scalar,
        ExceptionDeviationFormat.class
    );

    QualifiedProperty<DateTime> START_OF_ARCHIVE = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "StartOfArchive",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=294"),
        ValueRanks.Scalar,
        DateTime.class
    );

    QualifiedProperty<DateTime> START_OF_ONLINE_ARCHIVE = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "StartOfOnlineArchive",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=294"),
        ValueRanks.Scalar,
        DateTime.class
    );

    /**
     * Get the local value of the Stepped Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the Stepped Node.
     * @throws UaException if an error occurs creating or getting the Stepped Node.
     */
    Boolean getStepped() throws UaException;

    /**
     * Set the local value of the Stepped Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param stepped the local value to set for the Stepped Node.
     * @throws UaException if an error occurs creating or getting the Stepped Node.
     */
    void setStepped(Boolean stepped) throws UaException;

    /**
     * Read the value of the Stepped Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link Boolean} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    Boolean readStepped() throws UaException;

    /**
     * Write a new value for the Stepped Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param stepped the {@link Boolean} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeStepped(Boolean stepped) throws UaException;

    /**
     * An asynchronous implementation of {@link #readStepped()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends Boolean> readSteppedAsync();

    /**
     * An asynchronous implementation of {@link #writeStepped(Boolean)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeSteppedAsync(Boolean stepped);

    /**
     * Get the Stepped {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the Stepped {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getSteppedNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getSteppedNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getSteppedNodeAsync();

    /**
     * Get the local value of the Definition Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the Definition Node.
     * @throws UaException if an error occurs creating or getting the Definition Node.
     */
    String getDefinition() throws UaException;

    /**
     * Set the local value of the Definition Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param definition the local value to set for the Definition Node.
     * @throws UaException if an error occurs creating or getting the Definition Node.
     */
    void setDefinition(String definition) throws UaException;

    /**
     * Read the value of the Definition Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link String} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    String readDefinition() throws UaException;

    /**
     * Write a new value for the Definition Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param definition the {@link String} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeDefinition(String definition) throws UaException;

    /**
     * An asynchronous implementation of {@link #readDefinition()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends String> readDefinitionAsync();

    /**
     * An asynchronous implementation of {@link #writeDefinition(String)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeDefinitionAsync(String definition);

    /**
     * Get the Definition {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the Definition {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getDefinitionNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getDefinitionNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getDefinitionNodeAsync();

    /**
     * Get the local value of the MaxTimeInterval Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the MaxTimeInterval Node.
     * @throws UaException if an error occurs creating or getting the MaxTimeInterval Node.
     */
    Double getMaxTimeInterval() throws UaException;

    /**
     * Set the local value of the MaxTimeInterval Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param maxTimeInterval the local value to set for the MaxTimeInterval Node.
     * @throws UaException if an error occurs creating or getting the MaxTimeInterval Node.
     */
    void setMaxTimeInterval(Double maxTimeInterval) throws UaException;

    /**
     * Read the value of the MaxTimeInterval Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link Double} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    Double readMaxTimeInterval() throws UaException;

    /**
     * Write a new value for the MaxTimeInterval Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param maxTimeInterval the {@link Double} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeMaxTimeInterval(Double maxTimeInterval) throws UaException;

    /**
     * An asynchronous implementation of {@link #readMaxTimeInterval()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends Double> readMaxTimeIntervalAsync();

    /**
     * An asynchronous implementation of {@link #writeMaxTimeInterval(Double)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeMaxTimeIntervalAsync(Double maxTimeInterval);

    /**
     * Get the MaxTimeInterval {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the MaxTimeInterval {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getMaxTimeIntervalNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getMaxTimeIntervalNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getMaxTimeIntervalNodeAsync();

    /**
     * Get the local value of the MinTimeInterval Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the MinTimeInterval Node.
     * @throws UaException if an error occurs creating or getting the MinTimeInterval Node.
     */
    Double getMinTimeInterval() throws UaException;

    /**
     * Set the local value of the MinTimeInterval Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param minTimeInterval the local value to set for the MinTimeInterval Node.
     * @throws UaException if an error occurs creating or getting the MinTimeInterval Node.
     */
    void setMinTimeInterval(Double minTimeInterval) throws UaException;

    /**
     * Read the value of the MinTimeInterval Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link Double} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    Double readMinTimeInterval() throws UaException;

    /**
     * Write a new value for the MinTimeInterval Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param minTimeInterval the {@link Double} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeMinTimeInterval(Double minTimeInterval) throws UaException;

    /**
     * An asynchronous implementation of {@link #readMinTimeInterval()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends Double> readMinTimeIntervalAsync();

    /**
     * An asynchronous implementation of {@link #writeMinTimeInterval(Double)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeMinTimeIntervalAsync(Double minTimeInterval);

    /**
     * Get the MinTimeInterval {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the MinTimeInterval {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getMinTimeIntervalNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getMinTimeIntervalNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getMinTimeIntervalNodeAsync();

    /**
     * Get the local value of the ExceptionDeviation Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the ExceptionDeviation Node.
     * @throws UaException if an error occurs creating or getting the ExceptionDeviation Node.
     */
    Double getExceptionDeviation() throws UaException;

    /**
     * Set the local value of the ExceptionDeviation Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param exceptionDeviation the local value to set for the ExceptionDeviation Node.
     * @throws UaException if an error occurs creating or getting the ExceptionDeviation Node.
     */
    void setExceptionDeviation(Double exceptionDeviation) throws UaException;

    /**
     * Read the value of the ExceptionDeviation Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link Double} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    Double readExceptionDeviation() throws UaException;

    /**
     * Write a new value for the ExceptionDeviation Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param exceptionDeviation the {@link Double} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeExceptionDeviation(Double exceptionDeviation) throws UaException;

    /**
     * An asynchronous implementation of {@link #readExceptionDeviation()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends Double> readExceptionDeviationAsync();

    /**
     * An asynchronous implementation of {@link #writeExceptionDeviation(Double)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeExceptionDeviationAsync(Double exceptionDeviation);

    /**
     * Get the ExceptionDeviation {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the ExceptionDeviation {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getExceptionDeviationNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getExceptionDeviationNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getExceptionDeviationNodeAsync();

    /**
     * Get the local value of the ExceptionDeviationFormat Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the ExceptionDeviationFormat Node.
     * @throws UaException if an error occurs creating or getting the ExceptionDeviationFormat Node.
     */
    ExceptionDeviationFormat getExceptionDeviationFormat() throws UaException;

    /**
     * Set the local value of the ExceptionDeviationFormat Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param exceptionDeviationFormat the local value to set for the ExceptionDeviationFormat Node.
     * @throws UaException if an error occurs creating or getting the ExceptionDeviationFormat Node.
     */
    void setExceptionDeviationFormat(ExceptionDeviationFormat exceptionDeviationFormat) throws
        UaException;

    /**
     * Read the value of the ExceptionDeviationFormat Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link ExceptionDeviationFormat} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    ExceptionDeviationFormat readExceptionDeviationFormat() throws UaException;

    /**
     * Write a new value for the ExceptionDeviationFormat Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param exceptionDeviationFormat the {@link ExceptionDeviationFormat} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeExceptionDeviationFormat(ExceptionDeviationFormat exceptionDeviationFormat) throws
        UaException;

    /**
     * An asynchronous implementation of {@link #readExceptionDeviationFormat()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends ExceptionDeviationFormat> readExceptionDeviationFormatAsync();

    /**
     * An asynchronous implementation of {@link #writeExceptionDeviationFormat(ExceptionDeviationFormat)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeExceptionDeviationFormatAsync(
        ExceptionDeviationFormat exceptionDeviationFormat);

    /**
     * Get the ExceptionDeviationFormat {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the ExceptionDeviationFormat {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getExceptionDeviationFormatNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getExceptionDeviationFormatNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getExceptionDeviationFormatNodeAsync();

    /**
     * Get the local value of the StartOfArchive Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the StartOfArchive Node.
     * @throws UaException if an error occurs creating or getting the StartOfArchive Node.
     */
    DateTime getStartOfArchive() throws UaException;

    /**
     * Set the local value of the StartOfArchive Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param startOfArchive the local value to set for the StartOfArchive Node.
     * @throws UaException if an error occurs creating or getting the StartOfArchive Node.
     */
    void setStartOfArchive(DateTime startOfArchive) throws UaException;

    /**
     * Read the value of the StartOfArchive Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link DateTime} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    DateTime readStartOfArchive() throws UaException;

    /**
     * Write a new value for the StartOfArchive Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param startOfArchive the {@link DateTime} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeStartOfArchive(DateTime startOfArchive) throws UaException;

    /**
     * An asynchronous implementation of {@link #readStartOfArchive()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends DateTime> readStartOfArchiveAsync();

    /**
     * An asynchronous implementation of {@link #writeStartOfArchive(DateTime)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeStartOfArchiveAsync(DateTime startOfArchive);

    /**
     * Get the StartOfArchive {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the StartOfArchive {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getStartOfArchiveNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getStartOfArchiveNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getStartOfArchiveNodeAsync();

    /**
     * Get the local value of the StartOfOnlineArchive Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the StartOfOnlineArchive Node.
     * @throws UaException if an error occurs creating or getting the StartOfOnlineArchive Node.
     */
    DateTime getStartOfOnlineArchive() throws UaException;

    /**
     * Set the local value of the StartOfOnlineArchive Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param startOfOnlineArchive the local value to set for the StartOfOnlineArchive Node.
     * @throws UaException if an error occurs creating or getting the StartOfOnlineArchive Node.
     */
    void setStartOfOnlineArchive(DateTime startOfOnlineArchive) throws UaException;

    /**
     * Read the value of the StartOfOnlineArchive Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link DateTime} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    DateTime readStartOfOnlineArchive() throws UaException;

    /**
     * Write a new value for the StartOfOnlineArchive Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param startOfOnlineArchive the {@link DateTime} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeStartOfOnlineArchive(DateTime startOfOnlineArchive) throws UaException;

    /**
     * An asynchronous implementation of {@link #readStartOfOnlineArchive()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends DateTime> readStartOfOnlineArchiveAsync();

    /**
     * An asynchronous implementation of {@link #writeStartOfOnlineArchive(DateTime)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeStartOfOnlineArchiveAsync(DateTime startOfOnlineArchive);

    /**
     * Get the StartOfOnlineArchive {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the StartOfOnlineArchive {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getStartOfOnlineArchiveNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getStartOfOnlineArchiveNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getStartOfOnlineArchiveNodeAsync();

    /**
     * Get the AggregateConfiguration {@link AggregateConfigurationType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the AggregateConfiguration {@link AggregateConfigurationType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    AggregateConfigurationType getAggregateConfigurationNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getAggregateConfigurationNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * AggregateConfigurationType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends AggregateConfigurationType> getAggregateConfigurationNodeAsync();

    /**
     * Get the AggregateFunctions {@link FolderType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the AggregateFunctions {@link FolderType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    FolderType getAggregateFunctionsNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getAggregateFunctionsNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * FolderType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends FolderType> getAggregateFunctionsNodeAsync();
}
