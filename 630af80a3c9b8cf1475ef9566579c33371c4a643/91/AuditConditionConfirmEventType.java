package org.eclipse.milo.opcua.sdk.client.model.types.objects;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.model.types.variables.PropertyType;
import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;

public interface AuditConditionConfirmEventType extends AuditConditionEventType {
    QualifiedProperty<ByteString> CONDITION_EVENT_ID = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "ConditionEventId",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=15"),
        ValueRanks.Scalar,
        ByteString.class
    );

    QualifiedProperty<LocalizedText> COMMENT = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "Comment",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=21"),
        ValueRanks.Scalar,
        LocalizedText.class
    );

    /**
     * Get the local value of the ConditionEventId Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the ConditionEventId Node.
     * @throws UaException if an error occurs creating or getting the ConditionEventId Node.
     */
    ByteString getConditionEventId() throws UaException;

    /**
     * Set the local value of the ConditionEventId Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param conditionEventId the local value to set for the ConditionEventId Node.
     * @throws UaException if an error occurs creating or getting the ConditionEventId Node.
     */
    void setConditionEventId(ByteString conditionEventId) throws UaException;

    /**
     * Read the value of the ConditionEventId Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link ByteString} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    ByteString readConditionEventId() throws UaException;

    /**
     * Write a new value for the ConditionEventId Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param conditionEventId the {@link ByteString} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeConditionEventId(ByteString conditionEventId) throws UaException;

    /**
     * An asynchronous implementation of {@link #readConditionEventId()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends ByteString> readConditionEventIdAsync();

    /**
     * An asynchronous implementation of {@link #writeConditionEventId(ByteString)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeConditionEventIdAsync(ByteString conditionEventId);

    /**
     * Get the ConditionEventId {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the ConditionEventId {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getConditionEventIdNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getConditionEventIdNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getConditionEventIdNodeAsync();

    /**
     * Get the local value of the Comment Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the Comment Node.
     * @throws UaException if an error occurs creating or getting the Comment Node.
     */
    LocalizedText getComment() throws UaException;

    /**
     * Set the local value of the Comment Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param comment the local value to set for the Comment Node.
     * @throws UaException if an error occurs creating or getting the Comment Node.
     */
    void setComment(LocalizedText comment) throws UaException;

    /**
     * Read the value of the Comment Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link LocalizedText} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    LocalizedText readComment() throws UaException;

    /**
     * Write a new value for the Comment Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param comment the {@link LocalizedText} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeComment(LocalizedText comment) throws UaException;

    /**
     * An asynchronous implementation of {@link #readComment()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends LocalizedText> readCommentAsync();

    /**
     * An asynchronous implementation of {@link #writeComment(LocalizedText)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeCommentAsync(LocalizedText comment);

    /**
     * Get the Comment {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the Comment {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getCommentNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getCommentNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getCommentNodeAsync();
}
