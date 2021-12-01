package org.eclipse.milo.opcua.sdk.client.model.types.variables;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;

public interface SamplingIntervalDiagnosticsType extends BaseDataVariableType {
    /**
     * Get the local value of the SamplingInterval Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the SamplingInterval Node.
     * @throws UaException if an error occurs creating or getting the SamplingInterval Node.
     */
    Double getSamplingInterval() throws UaException;

    /**
     * Set the local value of the SamplingInterval Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param samplingInterval the local value to set for the SamplingInterval Node.
     * @throws UaException if an error occurs creating or getting the SamplingInterval Node.
     */
    void setSamplingInterval(Double samplingInterval) throws UaException;

    /**
     * Read the value of the SamplingInterval Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link Double} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    Double readSamplingInterval() throws UaException;

    /**
     * Write a new value for the SamplingInterval Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param samplingInterval the {@link Double} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeSamplingInterval(Double samplingInterval) throws UaException;

    /**
     * An asynchronous implementation of {@link #readSamplingInterval()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends Double> readSamplingIntervalAsync();

    /**
     * An asynchronous implementation of {@link #writeSamplingInterval(Double)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeSamplingIntervalAsync(Double samplingInterval);

    /**
     * Get the SamplingInterval {@link BaseDataVariableType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the SamplingInterval {@link BaseDataVariableType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    BaseDataVariableType getSamplingIntervalNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getSamplingIntervalNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends BaseDataVariableType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends BaseDataVariableType> getSamplingIntervalNodeAsync();

    /**
     * Get the local value of the SampledMonitoredItemsCount Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the SampledMonitoredItemsCount Node.
     * @throws UaException if an error occurs creating or getting the SampledMonitoredItemsCount Node.
     */
    UInteger getSampledMonitoredItemsCount() throws UaException;

    /**
     * Set the local value of the SampledMonitoredItemsCount Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param sampledMonitoredItemsCount the local value to set for the SampledMonitoredItemsCount Node.
     * @throws UaException if an error occurs creating or getting the SampledMonitoredItemsCount Node.
     */
    void setSampledMonitoredItemsCount(UInteger sampledMonitoredItemsCount) throws UaException;

    /**
     * Read the value of the SampledMonitoredItemsCount Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link UInteger} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    UInteger readSampledMonitoredItemsCount() throws UaException;

    /**
     * Write a new value for the SampledMonitoredItemsCount Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param sampledMonitoredItemsCount the {@link UInteger} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeSampledMonitoredItemsCount(UInteger sampledMonitoredItemsCount) throws UaException;

    /**
     * An asynchronous implementation of {@link #readSampledMonitoredItemsCount()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends UInteger> readSampledMonitoredItemsCountAsync();

    /**
     * An asynchronous implementation of {@link #writeSampledMonitoredItemsCount(UInteger)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeSampledMonitoredItemsCountAsync(
        UInteger sampledMonitoredItemsCount);

    /**
     * Get the SampledMonitoredItemsCount {@link BaseDataVariableType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the SampledMonitoredItemsCount {@link BaseDataVariableType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    BaseDataVariableType getSampledMonitoredItemsCountNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getSampledMonitoredItemsCountNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends BaseDataVariableType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends BaseDataVariableType> getSampledMonitoredItemsCountNodeAsync();

    /**
     * Get the local value of the MaxSampledMonitoredItemsCount Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the MaxSampledMonitoredItemsCount Node.
     * @throws UaException if an error occurs creating or getting the MaxSampledMonitoredItemsCount Node.
     */
    UInteger getMaxSampledMonitoredItemsCount() throws UaException;

    /**
     * Set the local value of the MaxSampledMonitoredItemsCount Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param maxSampledMonitoredItemsCount the local value to set for the MaxSampledMonitoredItemsCount Node.
     * @throws UaException if an error occurs creating or getting the MaxSampledMonitoredItemsCount Node.
     */
    void setMaxSampledMonitoredItemsCount(UInteger maxSampledMonitoredItemsCount) throws UaException;

    /**
     * Read the value of the MaxSampledMonitoredItemsCount Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link UInteger} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    UInteger readMaxSampledMonitoredItemsCount() throws UaException;

    /**
     * Write a new value for the MaxSampledMonitoredItemsCount Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param maxSampledMonitoredItemsCount the {@link UInteger} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeMaxSampledMonitoredItemsCount(UInteger maxSampledMonitoredItemsCount) throws
        UaException;

    /**
     * An asynchronous implementation of {@link #readMaxSampledMonitoredItemsCount()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends UInteger> readMaxSampledMonitoredItemsCountAsync();

    /**
     * An asynchronous implementation of {@link #writeMaxSampledMonitoredItemsCount(UInteger)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeMaxSampledMonitoredItemsCountAsync(
        UInteger maxSampledMonitoredItemsCount);

    /**
     * Get the MaxSampledMonitoredItemsCount {@link BaseDataVariableType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the MaxSampledMonitoredItemsCount {@link BaseDataVariableType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    BaseDataVariableType getMaxSampledMonitoredItemsCountNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getMaxSampledMonitoredItemsCountNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends BaseDataVariableType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends BaseDataVariableType> getMaxSampledMonitoredItemsCountNodeAsync();

    /**
     * Get the local value of the DisabledMonitoredItemsSamplingCount Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the DisabledMonitoredItemsSamplingCount Node.
     * @throws UaException if an error occurs creating or getting the DisabledMonitoredItemsSamplingCount Node.
     */
    UInteger getDisabledMonitoredItemsSamplingCount() throws UaException;

    /**
     * Set the local value of the DisabledMonitoredItemsSamplingCount Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param disabledMonitoredItemsSamplingCount the local value to set for the DisabledMonitoredItemsSamplingCount Node.
     * @throws UaException if an error occurs creating or getting the DisabledMonitoredItemsSamplingCount Node.
     */
    void setDisabledMonitoredItemsSamplingCount(UInteger disabledMonitoredItemsSamplingCount) throws
        UaException;

    /**
     * Read the value of the DisabledMonitoredItemsSamplingCount Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link UInteger} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    UInteger readDisabledMonitoredItemsSamplingCount() throws UaException;

    /**
     * Write a new value for the DisabledMonitoredItemsSamplingCount Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param disabledMonitoredItemsSamplingCount the {@link UInteger} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeDisabledMonitoredItemsSamplingCount(UInteger disabledMonitoredItemsSamplingCount) throws
        UaException;

    /**
     * An asynchronous implementation of {@link #readDisabledMonitoredItemsSamplingCount()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends UInteger> readDisabledMonitoredItemsSamplingCountAsync();

    /**
     * An asynchronous implementation of {@link #writeDisabledMonitoredItemsSamplingCount(UInteger)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeDisabledMonitoredItemsSamplingCountAsync(
        UInteger disabledMonitoredItemsSamplingCount);

    /**
     * Get the DisabledMonitoredItemsSamplingCount {@link BaseDataVariableType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the DisabledMonitoredItemsSamplingCount {@link BaseDataVariableType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    BaseDataVariableType getDisabledMonitoredItemsSamplingCountNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getDisabledMonitoredItemsSamplingCountNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends BaseDataVariableType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends BaseDataVariableType> getDisabledMonitoredItemsSamplingCountNodeAsync(
    );
}
