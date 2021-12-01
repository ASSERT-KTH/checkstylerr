package org.eclipse.milo.opcua.sdk.client.model.types.objects;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.model.types.variables.PropertyType;
import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;

public interface ProgressEventType extends BaseEventType {
    QualifiedProperty<Object> CONTEXT = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "Context",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=24"),
        ValueRanks.Scalar,
        Object.class
    );

    QualifiedProperty<UShort> PROGRESS = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "Progress",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=5"),
        ValueRanks.Scalar,
        UShort.class
    );

    /**
     * Get the local value of the Context Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the Context Node.
     * @throws UaException if an error occurs creating or getting the Context Node.
     */
    Object getContext() throws UaException;

    /**
     * Set the local value of the Context Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param context the local value to set for the Context Node.
     * @throws UaException if an error occurs creating or getting the Context Node.
     */
    void setContext(Object context) throws UaException;

    /**
     * Read the value of the Context Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link Object} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    Object readContext() throws UaException;

    /**
     * Write a new value for the Context Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param context the {@link Object} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeContext(Object context) throws UaException;

    /**
     * An asynchronous implementation of {@link #readContext()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<?> readContextAsync();

    /**
     * An asynchronous implementation of {@link #writeContext(Object)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeContextAsync(Object context);

    /**
     * Get the Context {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the Context {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getContextNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getContextNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getContextNodeAsync();

    /**
     * Get the local value of the Progress Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the Progress Node.
     * @throws UaException if an error occurs creating or getting the Progress Node.
     */
    UShort getProgress() throws UaException;

    /**
     * Set the local value of the Progress Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param progress the local value to set for the Progress Node.
     * @throws UaException if an error occurs creating or getting the Progress Node.
     */
    void setProgress(UShort progress) throws UaException;

    /**
     * Read the value of the Progress Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link UShort} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    UShort readProgress() throws UaException;

    /**
     * Write a new value for the Progress Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param progress the {@link UShort} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeProgress(UShort progress) throws UaException;

    /**
     * An asynchronous implementation of {@link #readProgress()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends UShort> readProgressAsync();

    /**
     * An asynchronous implementation of {@link #writeProgress(UShort)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeProgressAsync(UShort progress);

    /**
     * Get the Progress {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the Progress {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getProgressNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getProgressNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getProgressNodeAsync();
}
