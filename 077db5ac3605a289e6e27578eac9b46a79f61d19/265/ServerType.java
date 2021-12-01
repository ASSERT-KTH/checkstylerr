package org.eclipse.milo.opcua.sdk.client.model.types.objects;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.client.model.types.variables.PropertyType;
import org.eclipse.milo.opcua.sdk.client.model.types.variables.ServerStatusType;
import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.structured.ServerStatusDataType;
import org.eclipse.milo.opcua.stack.core.util.Unit;

public interface ServerType extends BaseObjectType {
    QualifiedProperty<String[]> SERVER_ARRAY = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "ServerArray",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=12"),
        ValueRanks.OneDimension,
        String[].class
    );

    QualifiedProperty<String[]> NAMESPACE_ARRAY = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "NamespaceArray",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=12"),
        ValueRanks.OneDimension,
        String[].class
    );

    QualifiedProperty<UByte> SERVICE_LEVEL = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "ServiceLevel",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=3"),
        ValueRanks.Scalar,
        UByte.class
    );

    QualifiedProperty<Boolean> AUDITING = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "Auditing",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=1"),
        ValueRanks.Scalar,
        Boolean.class
    );

    QualifiedProperty<DateTime> ESTIMATED_RETURN_TIME = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "EstimatedReturnTime",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=13"),
        ValueRanks.Scalar,
        DateTime.class
    );

    /**
     * Get the local value of the ServerArray Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the ServerArray Node.
     * @throws UaException if an error occurs creating or getting the ServerArray Node.
     */
    String[] getServerArray() throws UaException;

    /**
     * Set the local value of the ServerArray Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param serverArray the local value to set for the ServerArray Node.
     * @throws UaException if an error occurs creating or getting the ServerArray Node.
     */
    void setServerArray(String[] serverArray) throws UaException;

    /**
     * Read the value of the ServerArray Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link String[]} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    String[] readServerArray() throws UaException;

    /**
     * Write a new value for the ServerArray Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param serverArray the {@link String[]} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeServerArray(String[] serverArray) throws UaException;

    /**
     * An asynchronous implementation of {@link #readServerArray()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends String[]> readServerArrayAsync();

    /**
     * An asynchronous implementation of {@link #writeServerArray(String[])}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<Unit> writeServerArrayAsync(String[] serverArray);

    /**
     * Get the ServerArray {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the ServerArray {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getServerArrayNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getServerArrayNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getServerArrayNodeAsync();

    /**
     * Get the local value of the NamespaceArray Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the NamespaceArray Node.
     * @throws UaException if an error occurs creating or getting the NamespaceArray Node.
     */
    String[] getNamespaceArray() throws UaException;

    /**
     * Set the local value of the NamespaceArray Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param namespaceArray the local value to set for the NamespaceArray Node.
     * @throws UaException if an error occurs creating or getting the NamespaceArray Node.
     */
    void setNamespaceArray(String[] namespaceArray) throws UaException;

    /**
     * Read the value of the NamespaceArray Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link String[]} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    String[] readNamespaceArray() throws UaException;

    /**
     * Write a new value for the NamespaceArray Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param namespaceArray the {@link String[]} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeNamespaceArray(String[] namespaceArray) throws UaException;

    /**
     * An asynchronous implementation of {@link #readNamespaceArray()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends String[]> readNamespaceArrayAsync();

    /**
     * An asynchronous implementation of {@link #writeNamespaceArray(String[])}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<Unit> writeNamespaceArrayAsync(String[] namespaceArray);

    /**
     * Get the NamespaceArray {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the NamespaceArray {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getNamespaceArrayNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getNamespaceArrayNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getNamespaceArrayNodeAsync();

    /**
     * Get the local value of the ServiceLevel Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the ServiceLevel Node.
     * @throws UaException if an error occurs creating or getting the ServiceLevel Node.
     */
    UByte getServiceLevel() throws UaException;

    /**
     * Set the local value of the ServiceLevel Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param serviceLevel the local value to set for the ServiceLevel Node.
     * @throws UaException if an error occurs creating or getting the ServiceLevel Node.
     */
    void setServiceLevel(UByte serviceLevel) throws UaException;

    /**
     * Read the value of the ServiceLevel Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link UByte} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    UByte readServiceLevel() throws UaException;

    /**
     * Write a new value for the ServiceLevel Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param serviceLevel the {@link UByte} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeServiceLevel(UByte serviceLevel) throws UaException;

    /**
     * An asynchronous implementation of {@link #readServiceLevel()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends UByte> readServiceLevelAsync();

    /**
     * An asynchronous implementation of {@link #writeServiceLevel(UByte)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<Unit> writeServiceLevelAsync(UByte serviceLevel);

    /**
     * Get the ServiceLevel {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the ServiceLevel {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getServiceLevelNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getServiceLevelNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getServiceLevelNodeAsync();

    /**
     * Get the local value of the Auditing Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the Auditing Node.
     * @throws UaException if an error occurs creating or getting the Auditing Node.
     */
    Boolean getAuditing() throws UaException;

    /**
     * Set the local value of the Auditing Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param auditing the local value to set for the Auditing Node.
     * @throws UaException if an error occurs creating or getting the Auditing Node.
     */
    void setAuditing(Boolean auditing) throws UaException;

    /**
     * Read the value of the Auditing Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link Boolean} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    Boolean readAuditing() throws UaException;

    /**
     * Write a new value for the Auditing Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param auditing the {@link Boolean} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeAuditing(Boolean auditing) throws UaException;

    /**
     * An asynchronous implementation of {@link #readAuditing()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends Boolean> readAuditingAsync();

    /**
     * An asynchronous implementation of {@link #writeAuditing(Boolean)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<Unit> writeAuditingAsync(Boolean auditing);

    /**
     * Get the Auditing {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the Auditing {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getAuditingNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getAuditingNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getAuditingNodeAsync();

    /**
     * Get the local value of the EstimatedReturnTime Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the EstimatedReturnTime Node.
     * @throws UaException if an error occurs creating or getting the EstimatedReturnTime Node.
     */
    DateTime getEstimatedReturnTime() throws UaException;

    /**
     * Set the local value of the EstimatedReturnTime Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param estimatedReturnTime the local value to set for the EstimatedReturnTime Node.
     * @throws UaException if an error occurs creating or getting the EstimatedReturnTime Node.
     */
    void setEstimatedReturnTime(DateTime estimatedReturnTime) throws UaException;

    /**
     * Read the value of the EstimatedReturnTime Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link DateTime} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    DateTime readEstimatedReturnTime() throws UaException;

    /**
     * Write a new value for the EstimatedReturnTime Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param estimatedReturnTime the {@link DateTime} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeEstimatedReturnTime(DateTime estimatedReturnTime) throws UaException;

    /**
     * An asynchronous implementation of {@link #readEstimatedReturnTime()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends DateTime> readEstimatedReturnTimeAsync();

    /**
     * An asynchronous implementation of {@link #writeEstimatedReturnTime(DateTime)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<Unit> writeEstimatedReturnTimeAsync(DateTime estimatedReturnTime);

    /**
     * Get the EstimatedReturnTime {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the EstimatedReturnTime {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getEstimatedReturnTimeNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getEstimatedReturnTimeNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getEstimatedReturnTimeNodeAsync();

    /**
     * Get the local value of the ServerStatus Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the ServerStatus Node.
     * @throws UaException if an error occurs creating or getting the ServerStatus Node.
     */
    ServerStatusDataType getServerStatus() throws UaException;

    /**
     * Set the local value of the ServerStatus Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param serverStatus the local value to set for the ServerStatus Node.
     * @throws UaException if an error occurs creating or getting the ServerStatus Node.
     */
    void setServerStatus(ServerStatusDataType serverStatus) throws UaException;

    /**
     * Read the value of the ServerStatus Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link ServerStatusDataType} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    ServerStatusDataType readServerStatus() throws UaException;

    /**
     * Write a new value for the ServerStatus Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param serverStatus the {@link ServerStatusDataType} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeServerStatus(ServerStatusDataType serverStatus) throws UaException;

    /**
     * An asynchronous implementation of {@link #readServerStatus()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends ServerStatusDataType> readServerStatusAsync();

    /**
     * An asynchronous implementation of {@link #writeServerStatus(ServerStatusDataType)}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<Unit> writeServerStatusAsync(ServerStatusDataType serverStatus);

    /**
     * Get the ServerStatus {@link ServerStatusType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the ServerStatus {@link ServerStatusType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    ServerStatusType getServerStatusNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getServerStatusNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends ServerStatusType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends ServerStatusType> getServerStatusNodeAsync();

    /**
     * Get the ServerCapabilities {@link ServerCapabilitiesType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the ServerCapabilities {@link ServerCapabilitiesType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    ServerCapabilitiesType getServerCapabilitiesNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getServerCapabilitiesNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ServerCapabilitiesType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends ServerCapabilitiesType> getServerCapabilitiesNodeAsync();

    /**
     * Get the ServerDiagnostics {@link ServerDiagnosticsType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the ServerDiagnostics {@link ServerDiagnosticsType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    ServerDiagnosticsType getServerDiagnosticsNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getServerDiagnosticsNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ServerDiagnosticsType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends ServerDiagnosticsType> getServerDiagnosticsNodeAsync();

    /**
     * Get the VendorServerInfo {@link VendorServerInfoType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the VendorServerInfo {@link VendorServerInfoType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    VendorServerInfoType getVendorServerInfoNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getVendorServerInfoNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * VendorServerInfoType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends VendorServerInfoType> getVendorServerInfoNodeAsync();

    /**
     * Get the ServerRedundancy {@link ServerRedundancyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the ServerRedundancy {@link ServerRedundancyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    ServerRedundancyType getServerRedundancyNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getServerRedundancyNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ServerRedundancyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends ServerRedundancyType> getServerRedundancyNodeAsync();

    /**
     * Get the Namespaces {@link NamespacesType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the Namespaces {@link NamespacesType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    NamespacesType getNamespacesNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getNamespacesNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * NamespacesType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends NamespacesType> getNamespacesNodeAsync();
}
