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
import org.eclipse.milo.opcua.sdk.server.model.types.objects.AuditAddReferencesEventType;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.AddReferencesItem;

public class AuditAddReferencesEventTypeNode extends AuditNodeManagementEventTypeNode implements AuditAddReferencesEventType {
    public AuditAddReferencesEventTypeNode(UaNodeContext context, NodeId nodeId,
                                           QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                           UInteger writeMask, UInteger userWriteMask) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask);
    }

    public AuditAddReferencesEventTypeNode(UaNodeContext context, NodeId nodeId,
                                           QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                           UInteger writeMask, UInteger userWriteMask, UByte eventNotifier) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, eventNotifier);
    }

    @Override
    public PropertyTypeNode getReferencesToAddNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditAddReferencesEventType.REFERENCES_TO_ADD);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public AddReferencesItem[] getReferencesToAdd() {
        Optional<AddReferencesItem[]> propertyValue = getProperty(AuditAddReferencesEventType.REFERENCES_TO_ADD);
        return propertyValue.orElse(null);
    }

    @Override
    public void setReferencesToAdd(AddReferencesItem[] value) {
        setProperty(AuditAddReferencesEventType.REFERENCES_TO_ADD, value);
    }
}
