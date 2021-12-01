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

import org.eclipse.milo.opcua.sdk.core.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.server.model.nodes.variables.PropertyTypeNode;
import org.eclipse.milo.opcua.sdk.server.model.types.objects.AuditHistoryEventUpdateEventType;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.PerformUpdateType;
import org.eclipse.milo.opcua.stack.core.types.structured.EventFilter;
import org.eclipse.milo.opcua.stack.core.types.structured.HistoryEventFieldList;

public class AuditHistoryEventUpdateEventTypeNode extends AuditHistoryUpdateEventTypeNode implements AuditHistoryEventUpdateEventType {
    public AuditHistoryEventUpdateEventTypeNode(UaNodeContext context, NodeId nodeId,
                                                QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                                UInteger writeMask, UInteger userWriteMask) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask);
    }

    public AuditHistoryEventUpdateEventTypeNode(UaNodeContext context, NodeId nodeId,
                                                QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                                UInteger writeMask, UInteger userWriteMask, UByte eventNotifier) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, eventNotifier);
    }

    @Override
    public PropertyTypeNode getUpdatedNodeNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditHistoryEventUpdateEventType.UPDATED_NODE);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public NodeId getUpdatedNode() {
        Optional<NodeId> propertyValue = getProperty(AuditHistoryEventUpdateEventType.UPDATED_NODE);
        return propertyValue.orElse(null);
    }

    @Override
    public void setUpdatedNode(NodeId value) {
        setProperty(AuditHistoryEventUpdateEventType.UPDATED_NODE, value);
    }

    @Override
    public PropertyTypeNode getPerformInsertReplaceNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditHistoryEventUpdateEventType.PERFORM_INSERT_REPLACE);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public PerformUpdateType getPerformInsertReplace() {
        Optional<PerformUpdateType> propertyValue = getProperty(AuditHistoryEventUpdateEventType.PERFORM_INSERT_REPLACE);
        return propertyValue.orElse(null);
    }

    @Override
    public void setPerformInsertReplace(PerformUpdateType value) {
        setProperty(AuditHistoryEventUpdateEventType.PERFORM_INSERT_REPLACE, value);
    }

    @Override
    public PropertyTypeNode getFilterNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditHistoryEventUpdateEventType.FILTER);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public EventFilter getFilter() {
        Optional<EventFilter> propertyValue = getProperty(AuditHistoryEventUpdateEventType.FILTER);
        return propertyValue.orElse(null);
    }

    @Override
    public void setFilter(EventFilter value) {
        setProperty(AuditHistoryEventUpdateEventType.FILTER, value);
    }

    @Override
    public PropertyTypeNode getNewValuesNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditHistoryEventUpdateEventType.NEW_VALUES);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public HistoryEventFieldList[] getNewValues() {
        Optional<HistoryEventFieldList[]> propertyValue = getProperty(AuditHistoryEventUpdateEventType.NEW_VALUES);
        return propertyValue.orElse(null);
    }

    @Override
    public void setNewValues(HistoryEventFieldList[] value) {
        setProperty(AuditHistoryEventUpdateEventType.NEW_VALUES, value);
    }

    @Override
    public PropertyTypeNode getOldValuesNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditHistoryEventUpdateEventType.OLD_VALUES);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public HistoryEventFieldList[] getOldValues() {
        Optional<HistoryEventFieldList[]> propertyValue = getProperty(AuditHistoryEventUpdateEventType.OLD_VALUES);
        return propertyValue.orElse(null);
    }

    @Override
    public void setOldValues(HistoryEventFieldList[] value) {
        setProperty(AuditHistoryEventUpdateEventType.OLD_VALUES, value);
    }
}
