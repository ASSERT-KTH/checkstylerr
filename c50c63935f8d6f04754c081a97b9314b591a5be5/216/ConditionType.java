package org.eclipse.milo.opcua.sdk.client.model.types.objects;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.model.types.variables.ConditionVariableType;
import org.eclipse.milo.opcua.sdk.client.model.types.variables.PropertyType;
import org.eclipse.milo.opcua.sdk.client.model.types.variables.TwoStateVariableType;
import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.util.Unit;

public interface ConditionType extends BaseEventType {
    QualifiedProperty<NodeId> CONDITION_CLASS_ID = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "ConditionClassId",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=17"),
        ValueRanks.Scalar,
        NodeId.class
    );

    QualifiedProperty<LocalizedText> CONDITION_CLASS_NAME = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "ConditionClassName",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=21"),
        ValueRanks.Scalar,
        LocalizedText.class
    );

    QualifiedProperty<String> CONDITION_NAME = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "ConditionName",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=12"),
        ValueRanks.Scalar,
        String.class
    );

    QualifiedProperty<NodeId> BRANCH_ID = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "BranchId",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=17"),
        ValueRanks.Scalar,
        NodeId.class
    );

    QualifiedProperty<Boolean> RETAIN = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "Retain",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=1"),
        ValueRanks.Scalar,
        Boolean.class
    );

    QualifiedProperty<String> CLIENT_USER_ID = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "ClientUserId",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=12"),
        ValueRanks.Scalar,
        String.class
    );

    /**
     * Get the local value of the ConditionClassId Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the ConditionClassId Node.
     * @throws UaException if an error occurs creating or getting the ConditionClassId Node.
     */
    NodeId getConditionClassId() throws UaException;

    /**
     * Set the local value of the ConditionClassId Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param conditionClassId the local value to set for the ConditionClassId Node.
     * @throws UaException if an error occurs creating or getting the ConditionClassId Node.
     */
    void setConditionClassId(NodeId conditionClassId) throws UaException;

    /**
     * Read the value of the ConditionClassId Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link NodeId} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    NodeId readConditionClassId() throws UaException;

    /**
     * Write a new value for the ConditionClassId Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param conditionClassId the {@link NodeId} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeConditionClassId(NodeId conditionClassId) throws UaException;

    /**
     * An asynchronous implementation of {@link #readConditionClassId()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends NodeId> readConditionClassIdAsync();

    /**
     * An asynchronous implementation of {@link #writeConditionClassId(NodeId)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<Unit> writeConditionClassIdAsync(NodeId conditionClassId);

    /**
     * Get the ConditionClassId {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the ConditionClassId {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getConditionClassIdNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getConditionClassIdNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getConditionClassIdNodeAsync();

    /**
     * Get the local value of the ConditionClassName Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the ConditionClassName Node.
     * @throws UaException if an error occurs creating or getting the ConditionClassName Node.
     */
    LocalizedText getConditionClassName() throws UaException;

    /**
     * Set the local value of the ConditionClassName Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param conditionClassName the local value to set for the ConditionClassName Node.
     * @throws UaException if an error occurs creating or getting the ConditionClassName Node.
     */
    void setConditionClassName(LocalizedText conditionClassName) throws UaException;

    /**
     * Read the value of the ConditionClassName Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link LocalizedText} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    LocalizedText readConditionClassName() throws UaException;

    /**
     * Write a new value for the ConditionClassName Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param conditionClassName the {@link LocalizedText} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeConditionClassName(LocalizedText conditionClassName) throws UaException;

    /**
     * An asynchronous implementation of {@link #readConditionClassName()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends LocalizedText> readConditionClassNameAsync();

    /**
     * An asynchronous implementation of {@link #writeConditionClassName(LocalizedText)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<Unit> writeConditionClassNameAsync(LocalizedText conditionClassName);

    /**
     * Get the ConditionClassName {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the ConditionClassName {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getConditionClassNameNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getConditionClassNameNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getConditionClassNameNodeAsync();

    /**
     * Get the local value of the ConditionName Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the ConditionName Node.
     * @throws UaException if an error occurs creating or getting the ConditionName Node.
     */
    String getConditionName() throws UaException;

    /**
     * Set the local value of the ConditionName Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param conditionName the local value to set for the ConditionName Node.
     * @throws UaException if an error occurs creating or getting the ConditionName Node.
     */
    void setConditionName(String conditionName) throws UaException;

    /**
     * Read the value of the ConditionName Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link String} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    String readConditionName() throws UaException;

    /**
     * Write a new value for the ConditionName Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param conditionName the {@link String} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeConditionName(String conditionName) throws UaException;

    /**
     * An asynchronous implementation of {@link #readConditionName()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends String> readConditionNameAsync();

    /**
     * An asynchronous implementation of {@link #writeConditionName(String)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<Unit> writeConditionNameAsync(String conditionName);

    /**
     * Get the ConditionName {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the ConditionName {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getConditionNameNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getConditionNameNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getConditionNameNodeAsync();

    /**
     * Get the local value of the BranchId Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the BranchId Node.
     * @throws UaException if an error occurs creating or getting the BranchId Node.
     */
    NodeId getBranchId() throws UaException;

    /**
     * Set the local value of the BranchId Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param branchId the local value to set for the BranchId Node.
     * @throws UaException if an error occurs creating or getting the BranchId Node.
     */
    void setBranchId(NodeId branchId) throws UaException;

    /**
     * Read the value of the BranchId Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link NodeId} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    NodeId readBranchId() throws UaException;

    /**
     * Write a new value for the BranchId Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param branchId the {@link NodeId} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeBranchId(NodeId branchId) throws UaException;

    /**
     * An asynchronous implementation of {@link #readBranchId()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends NodeId> readBranchIdAsync();

    /**
     * An asynchronous implementation of {@link #writeBranchId(NodeId)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<Unit> writeBranchIdAsync(NodeId branchId);

    /**
     * Get the BranchId {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the BranchId {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getBranchIdNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getBranchIdNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getBranchIdNodeAsync();

    /**
     * Get the local value of the Retain Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the Retain Node.
     * @throws UaException if an error occurs creating or getting the Retain Node.
     */
    Boolean getRetain() throws UaException;

    /**
     * Set the local value of the Retain Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param retain the local value to set for the Retain Node.
     * @throws UaException if an error occurs creating or getting the Retain Node.
     */
    void setRetain(Boolean retain) throws UaException;

    /**
     * Read the value of the Retain Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link Boolean} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    Boolean readRetain() throws UaException;

    /**
     * Write a new value for the Retain Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param retain the {@link Boolean} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeRetain(Boolean retain) throws UaException;

    /**
     * An asynchronous implementation of {@link #readRetain()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends Boolean> readRetainAsync();

    /**
     * An asynchronous implementation of {@link #writeRetain(Boolean)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<Unit> writeRetainAsync(Boolean retain);

    /**
     * Get the Retain {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the Retain {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getRetainNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getRetainNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getRetainNodeAsync();

    /**
     * Get the local value of the ClientUserId Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the ClientUserId Node.
     * @throws UaException if an error occurs creating or getting the ClientUserId Node.
     */
    String getClientUserId() throws UaException;

    /**
     * Set the local value of the ClientUserId Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param clientUserId the local value to set for the ClientUserId Node.
     * @throws UaException if an error occurs creating or getting the ClientUserId Node.
     */
    void setClientUserId(String clientUserId) throws UaException;

    /**
     * Read the value of the ClientUserId Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link String} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    String readClientUserId() throws UaException;

    /**
     * Write a new value for the ClientUserId Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param clientUserId the {@link String} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeClientUserId(String clientUserId) throws UaException;

    /**
     * An asynchronous implementation of {@link #readClientUserId()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends String> readClientUserIdAsync();

    /**
     * An asynchronous implementation of {@link #writeClientUserId(String)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<Unit> writeClientUserIdAsync(String clientUserId);

    /**
     * Get the ClientUserId {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the ClientUserId {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getClientUserIdNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getClientUserIdNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getClientUserIdNodeAsync();

    /**
     * Get the local value of the EnabledState Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the EnabledState Node.
     * @throws UaException if an error occurs creating or getting the EnabledState Node.
     */
    LocalizedText getEnabledState() throws UaException;

    /**
     * Set the local value of the EnabledState Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param enabledState the local value to set for the EnabledState Node.
     * @throws UaException if an error occurs creating or getting the EnabledState Node.
     */
    void setEnabledState(LocalizedText enabledState) throws UaException;

    /**
     * Read the value of the EnabledState Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link LocalizedText} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    LocalizedText readEnabledState() throws UaException;

    /**
     * Write a new value for the EnabledState Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param enabledState the {@link LocalizedText} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeEnabledState(LocalizedText enabledState) throws UaException;

    /**
     * An asynchronous implementation of {@link #readEnabledState()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends LocalizedText> readEnabledStateAsync();

    /**
     * An asynchronous implementation of {@link #writeEnabledState(LocalizedText)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<Unit> writeEnabledStateAsync(LocalizedText enabledState);

    /**
     * Get the EnabledState {@link TwoStateVariableType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the EnabledState {@link TwoStateVariableType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    TwoStateVariableType getEnabledStateNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getEnabledStateNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends TwoStateVariableType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends TwoStateVariableType> getEnabledStateNodeAsync();

    /**
     * Get the local value of the Quality Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the Quality Node.
     * @throws UaException if an error occurs creating or getting the Quality Node.
     */
    StatusCode getQuality() throws UaException;

    /**
     * Set the local value of the Quality Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param quality the local value to set for the Quality Node.
     * @throws UaException if an error occurs creating or getting the Quality Node.
     */
    void setQuality(StatusCode quality) throws UaException;

    /**
     * Read the value of the Quality Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link StatusCode} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    StatusCode readQuality() throws UaException;

    /**
     * Write a new value for the Quality Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param quality the {@link StatusCode} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeQuality(StatusCode quality) throws UaException;

    /**
     * An asynchronous implementation of {@link #readQuality()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends StatusCode> readQualityAsync();

    /**
     * An asynchronous implementation of {@link #writeQuality(StatusCode)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<Unit> writeQualityAsync(StatusCode quality);

    /**
     * Get the Quality {@link ConditionVariableType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the Quality {@link ConditionVariableType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    ConditionVariableType getQualityNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getQualityNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends ConditionVariableType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends ConditionVariableType> getQualityNodeAsync();

    /**
     * Get the local value of the LastSeverity Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the LastSeverity Node.
     * @throws UaException if an error occurs creating or getting the LastSeverity Node.
     */
    UShort getLastSeverity() throws UaException;

    /**
     * Set the local value of the LastSeverity Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param lastSeverity the local value to set for the LastSeverity Node.
     * @throws UaException if an error occurs creating or getting the LastSeverity Node.
     */
    void setLastSeverity(UShort lastSeverity) throws UaException;

    /**
     * Read the value of the LastSeverity Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link UShort} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    UShort readLastSeverity() throws UaException;

    /**
     * Write a new value for the LastSeverity Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param lastSeverity the {@link UShort} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeLastSeverity(UShort lastSeverity) throws UaException;

    /**
     * An asynchronous implementation of {@link #readLastSeverity()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends UShort> readLastSeverityAsync();

    /**
     * An asynchronous implementation of {@link #writeLastSeverity(UShort)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<Unit> writeLastSeverityAsync(UShort lastSeverity);

    /**
     * Get the LastSeverity {@link ConditionVariableType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the LastSeverity {@link ConditionVariableType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    ConditionVariableType getLastSeverityNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getLastSeverityNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends ConditionVariableType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends ConditionVariableType> getLastSeverityNodeAsync();

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
     * completes exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<Unit> writeCommentAsync(LocalizedText comment);

    /**
     * Get the Comment {@link ConditionVariableType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the Comment {@link ConditionVariableType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    ConditionVariableType getCommentNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getCommentNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends ConditionVariableType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends ConditionVariableType> getCommentNodeAsync();
}
