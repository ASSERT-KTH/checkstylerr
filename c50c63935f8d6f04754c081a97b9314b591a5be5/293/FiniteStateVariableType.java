package org.eclipse.milo.opcua.sdk.client.model.types.variables;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;

public interface FiniteStateVariableType extends StateVariableType {
    QualifiedProperty<NodeId> ID = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "Id",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=17"),
        ValueRanks.Scalar,
        NodeId.class
    );

    /**
     * Get the local value of the Id Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the Id Node.
     * @throws UaException if an error occurs creating or getting the Id Node.
     */
    NodeId getId() throws UaException;

    /**
     * Set the local value of the Id Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param id the local value to set for the Id Node.
     * @throws UaException if an error occurs creating or getting the Id Node.
     */
    void setId(NodeId id) throws UaException;

    /**
     * Read the value of the Id Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link NodeId} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    NodeId readId() throws UaException;

    /**
     * Write a new value for the Id Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param id the {@link NodeId} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeId(NodeId id) throws UaException;

    /**
     * An asynchronous implementation of {@link #readId()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends NodeId> readIdAsync();

    /**
     * An asynchronous implementation of {@link #writeId(NodeId)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeIdAsync(NodeId id);

    /**
     * Get the Id {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the Id {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getIdNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getIdNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getIdNodeAsync();
}
