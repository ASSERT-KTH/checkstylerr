/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.sdk.server.model.nodes.objects;

import java.util.Optional;

import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.api.nodes.ObjectNode;
import org.eclipse.milo.opcua.sdk.server.api.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.server.model.nodes.variables.PropertyTypeNode;
import org.eclipse.milo.opcua.sdk.server.model.nodes.variables.ServerStatusTypeNode;
import org.eclipse.milo.opcua.sdk.server.model.types.objects.ServerType;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.ServerStatusDataType;

public class ServerTypeNode extends BaseObjectTypeNode implements ServerType {
    public ServerTypeNode(UaNodeContext context, NodeId nodeId, QualifiedName browseName,
                          LocalizedText displayName, LocalizedText description, UInteger writeMask,
                          UInteger userWriteMask) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask);
    }

    public ServerTypeNode(UaNodeContext context, NodeId nodeId, QualifiedName browseName,
                          LocalizedText displayName, LocalizedText description, UInteger writeMask,
                          UInteger userWriteMask, UByte eventNotifier) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, eventNotifier);
    }

    @Override
    public PropertyTypeNode getServerArrayNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(ServerType.SERVER_ARRAY);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public String[] getServerArray() {
        Optional<String[]> propertyValue = getProperty(ServerType.SERVER_ARRAY);
        return propertyValue.orElse(null);
    }

    @Override
    public void setServerArray(String[] value) {
        setProperty(ServerType.SERVER_ARRAY, value);
    }

    @Override
    public PropertyTypeNode getNamespaceArrayNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(ServerType.NAMESPACE_ARRAY);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public String[] getNamespaceArray() {
        Optional<String[]> propertyValue = getProperty(ServerType.NAMESPACE_ARRAY);
        return propertyValue.orElse(null);
    }

    @Override
    public void setNamespaceArray(String[] value) {
        setProperty(ServerType.NAMESPACE_ARRAY, value);
    }

    @Override
    public PropertyTypeNode getServiceLevelNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(ServerType.SERVICE_LEVEL);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public UByte getServiceLevel() {
        Optional<UByte> propertyValue = getProperty(ServerType.SERVICE_LEVEL);
        return propertyValue.orElse(null);
    }

    @Override
    public void setServiceLevel(UByte value) {
        setProperty(ServerType.SERVICE_LEVEL, value);
    }

    @Override
    public PropertyTypeNode getAuditingNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(ServerType.AUDITING);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public Boolean getAuditing() {
        Optional<Boolean> propertyValue = getProperty(ServerType.AUDITING);
        return propertyValue.orElse(null);
    }

    @Override
    public void setAuditing(Boolean value) {
        setProperty(ServerType.AUDITING, value);
    }

    @Override
    public PropertyTypeNode getEstimatedReturnTimeNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(ServerType.ESTIMATED_RETURN_TIME);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public DateTime getEstimatedReturnTime() {
        Optional<DateTime> propertyValue = getProperty(ServerType.ESTIMATED_RETURN_TIME);
        return propertyValue.orElse(null);
    }

    @Override
    public void setEstimatedReturnTime(DateTime value) {
        setProperty(ServerType.ESTIMATED_RETURN_TIME, value);
    }

    @Override
    public ServerStatusTypeNode getServerStatusNode() {
        Optional<VariableNode> component = getVariableComponent("http://opcfoundation.org/UA/", "ServerStatus");
        return (ServerStatusTypeNode) component.orElse(null);
    }

    @Override
    public ServerStatusDataType getServerStatus() {
        Optional<VariableNode> component = getVariableComponent("http://opcfoundation.org/UA/", "ServerStatus");
        return component.map(node -> (ServerStatusDataType) node.getValue().getValue().getValue()).orElse(null);
    }

    @Override
    public void setServerStatus(ServerStatusDataType value) {
        getVariableComponent("http://opcfoundation.org/UA/", "ServerStatus").ifPresent(n -> n.setValue(new DataValue(new Variant(value))));
    }

    @Override
    public ServerCapabilitiesTypeNode getServerCapabilitiesNode() {
        Optional<ObjectNode> component = getObjectComponent("http://opcfoundation.org/UA/", "ServerCapabilities");
        return (ServerCapabilitiesTypeNode) component.orElse(null);
    }

    @Override
    public ServerDiagnosticsTypeNode getServerDiagnosticsNode() {
        Optional<ObjectNode> component = getObjectComponent("http://opcfoundation.org/UA/", "ServerDiagnostics");
        return (ServerDiagnosticsTypeNode) component.orElse(null);
    }

    @Override
    public VendorServerInfoTypeNode getVendorServerInfoNode() {
        Optional<ObjectNode> component = getObjectComponent("http://opcfoundation.org/UA/", "VendorServerInfo");
        return (VendorServerInfoTypeNode) component.orElse(null);
    }

    @Override
    public ServerRedundancyTypeNode getServerRedundancyNode() {
        Optional<ObjectNode> component = getObjectComponent("http://opcfoundation.org/UA/", "ServerRedundancy");
        return (ServerRedundancyTypeNode) component.orElse(null);
    }

    @Override
    public NamespacesTypeNode getNamespacesNode() {
        Optional<ObjectNode> component = getObjectComponent("http://opcfoundation.org/UA/", "Namespaces");
        return (NamespacesTypeNode) component.orElse(null);
    }

    @Override
    public UaMethodNode getGetMonitoredItemsMethodNode() {
        Optional<UaNode> methodNode = findNode("http://opcfoundation.org/UA/", "GetMonitoredItems", node -> node instanceof UaMethodNode, Reference.HAS_COMPONENT_PREDICATE);
        return (UaMethodNode) methodNode.orElse(null);
    }

    @Override
    public UaMethodNode getResendDataMethodNode() {
        Optional<UaNode> methodNode = findNode("http://opcfoundation.org/UA/", "ResendData", node -> node instanceof UaMethodNode, Reference.HAS_COMPONENT_PREDICATE);
        return (UaMethodNode) methodNode.orElse(null);
    }

    @Override
    public UaMethodNode getSetSubscriptionDurableMethodNode() {
        Optional<UaNode> methodNode = findNode("http://opcfoundation.org/UA/", "SetSubscriptionDurable", node -> node instanceof UaMethodNode, Reference.HAS_COMPONENT_PREDICATE);
        return (UaMethodNode) methodNode.orElse(null);
    }

    @Override
    public UaMethodNode getRequestServerStateChangeMethodNode() {
        Optional<UaNode> methodNode = findNode("http://opcfoundation.org/UA/", "RequestServerStateChange", node -> node instanceof UaMethodNode, Reference.HAS_COMPONENT_PREDICATE);
        return (UaMethodNode) methodNode.orElse(null);
    }
}
