package org.eclipse.milo.opcua.sdk.client.model.types.variables;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;

public interface TwoStateVariableType extends StateVariableType {
    QualifiedProperty<Boolean> ID = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "Id",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=1"),
        ValueRanks.Scalar,
        Boolean.class
    );

    QualifiedProperty<DateTime> TRANSITION_TIME = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "TransitionTime",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=294"),
        ValueRanks.Scalar,
        DateTime.class
    );

    QualifiedProperty<DateTime> EFFECTIVE_TRANSITION_TIME = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "EffectiveTransitionTime",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=294"),
        ValueRanks.Scalar,
        DateTime.class
    );

    QualifiedProperty<LocalizedText> TRUE_STATE = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "TrueState",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=21"),
        ValueRanks.Scalar,
        LocalizedText.class
    );

    QualifiedProperty<LocalizedText> FALSE_STATE = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "FalseState",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=21"),
        ValueRanks.Scalar,
        LocalizedText.class
    );

    /**
     * Get the local value of the Id Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the Id Node.
     * @throws UaException if an error occurs creating or getting the Id Node.
     */
    Boolean getId() throws UaException;

    /**
     * Set the local value of the Id Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param id the local value to set for the Id Node.
     * @throws UaException if an error occurs creating or getting the Id Node.
     */
    void setId(Boolean id) throws UaException;

    /**
     * Read the value of the Id Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link Boolean} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    Boolean readId() throws UaException;

    /**
     * Write a new value for the Id Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param id the {@link Boolean} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeId(Boolean id) throws UaException;

    /**
     * An asynchronous implementation of {@link #readId()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends Boolean> readIdAsync();

    /**
     * An asynchronous implementation of {@link #writeId(Boolean)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeIdAsync(Boolean id);

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

    /**
     * Get the local value of the TransitionTime Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the TransitionTime Node.
     * @throws UaException if an error occurs creating or getting the TransitionTime Node.
     */
    DateTime getTransitionTime() throws UaException;

    /**
     * Set the local value of the TransitionTime Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param transitionTime the local value to set for the TransitionTime Node.
     * @throws UaException if an error occurs creating or getting the TransitionTime Node.
     */
    void setTransitionTime(DateTime transitionTime) throws UaException;

    /**
     * Read the value of the TransitionTime Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link DateTime} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    DateTime readTransitionTime() throws UaException;

    /**
     * Write a new value for the TransitionTime Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param transitionTime the {@link DateTime} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeTransitionTime(DateTime transitionTime) throws UaException;

    /**
     * An asynchronous implementation of {@link #readTransitionTime()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends DateTime> readTransitionTimeAsync();

    /**
     * An asynchronous implementation of {@link #writeTransitionTime(DateTime)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeTransitionTimeAsync(DateTime transitionTime);

    /**
     * Get the TransitionTime {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the TransitionTime {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getTransitionTimeNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getTransitionTimeNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getTransitionTimeNodeAsync();

    /**
     * Get the local value of the EffectiveTransitionTime Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the EffectiveTransitionTime Node.
     * @throws UaException if an error occurs creating or getting the EffectiveTransitionTime Node.
     */
    DateTime getEffectiveTransitionTime() throws UaException;

    /**
     * Set the local value of the EffectiveTransitionTime Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param effectiveTransitionTime the local value to set for the EffectiveTransitionTime Node.
     * @throws UaException if an error occurs creating or getting the EffectiveTransitionTime Node.
     */
    void setEffectiveTransitionTime(DateTime effectiveTransitionTime) throws UaException;

    /**
     * Read the value of the EffectiveTransitionTime Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link DateTime} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    DateTime readEffectiveTransitionTime() throws UaException;

    /**
     * Write a new value for the EffectiveTransitionTime Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param effectiveTransitionTime the {@link DateTime} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeEffectiveTransitionTime(DateTime effectiveTransitionTime) throws UaException;

    /**
     * An asynchronous implementation of {@link #readEffectiveTransitionTime()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends DateTime> readEffectiveTransitionTimeAsync();

    /**
     * An asynchronous implementation of {@link #writeEffectiveTransitionTime(DateTime)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeEffectiveTransitionTimeAsync(DateTime effectiveTransitionTime);

    /**
     * Get the EffectiveTransitionTime {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the EffectiveTransitionTime {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getEffectiveTransitionTimeNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getEffectiveTransitionTimeNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getEffectiveTransitionTimeNodeAsync();

    /**
     * Get the local value of the TrueState Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the TrueState Node.
     * @throws UaException if an error occurs creating or getting the TrueState Node.
     */
    LocalizedText getTrueState() throws UaException;

    /**
     * Set the local value of the TrueState Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param trueState the local value to set for the TrueState Node.
     * @throws UaException if an error occurs creating or getting the TrueState Node.
     */
    void setTrueState(LocalizedText trueState) throws UaException;

    /**
     * Read the value of the TrueState Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link LocalizedText} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    LocalizedText readTrueState() throws UaException;

    /**
     * Write a new value for the TrueState Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param trueState the {@link LocalizedText} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeTrueState(LocalizedText trueState) throws UaException;

    /**
     * An asynchronous implementation of {@link #readTrueState()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends LocalizedText> readTrueStateAsync();

    /**
     * An asynchronous implementation of {@link #writeTrueState(LocalizedText)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeTrueStateAsync(LocalizedText trueState);

    /**
     * Get the TrueState {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the TrueState {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getTrueStateNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getTrueStateNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getTrueStateNodeAsync();

    /**
     * Get the local value of the FalseState Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the FalseState Node.
     * @throws UaException if an error occurs creating or getting the FalseState Node.
     */
    LocalizedText getFalseState() throws UaException;

    /**
     * Set the local value of the FalseState Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param falseState the local value to set for the FalseState Node.
     * @throws UaException if an error occurs creating or getting the FalseState Node.
     */
    void setFalseState(LocalizedText falseState) throws UaException;

    /**
     * Read the value of the FalseState Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link LocalizedText} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    LocalizedText readFalseState() throws UaException;

    /**
     * Write a new value for the FalseState Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param falseState the {@link LocalizedText} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeFalseState(LocalizedText falseState) throws UaException;

    /**
     * An asynchronous implementation of {@link #readFalseState()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends LocalizedText> readFalseStateAsync();

    /**
     * An asynchronous implementation of {@link #writeFalseState(LocalizedText)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeFalseStateAsync(LocalizedText falseState);

    /**
     * Get the FalseState {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the FalseState {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getFalseStateNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getFalseStateNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getFalseStateNodeAsync();
}
