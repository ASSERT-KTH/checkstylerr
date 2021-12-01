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
import org.eclipse.milo.opcua.sdk.server.model.types.objects.AuditHistoryEventDeleteEventType;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.HistoryEventFieldList;

public class AuditHistoryEventDeleteEventTypeNode extends AuditHistoryDeleteEventTypeNode implements AuditHistoryEventDeleteEventType {
    public AuditHistoryEventDeleteEventTypeNode(UaNodeContext context, NodeId nodeId,
                                                QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                                UInteger writeMask, UInteger userWriteMask) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask);
    }

    public AuditHistoryEventDeleteEventTypeNode(UaNodeContext context, NodeId nodeId,
                                                QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                                UInteger writeMask, UInteger userWriteMask, UByte eventNotifier) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, eventNotifier);
    }

    @Override
    public PropertyTypeNode getEventIdsNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditHistoryEventDeleteEventType.EVENT_IDS);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public ByteString[] getEventIds() {
        Optional<ByteString[]> propertyValue = getProperty(AuditHistoryEventDeleteEventType.EVENT_IDS);
        return propertyValue.orElse(null);
    }

    @Override
    public void setEventIds(ByteString[] value) {
        setProperty(AuditHistoryEventDeleteEventType.EVENT_IDS, value);
    }

    @Override
    public PropertyTypeNode getOldValuesNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditHistoryEventDeleteEventType.OLD_VALUES);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public HistoryEventFieldList getOldValues() {
        Optional<HistoryEventFieldList> propertyValue = getProperty(AuditHistoryEventDeleteEventType.OLD_VALUES);
        return propertyValue.orElse(null);
    }

    @Override
    public void setOldValues(HistoryEventFieldList value) {
        setProperty(AuditHistoryEventDeleteEventType.OLD_VALUES, value);
    }
}
