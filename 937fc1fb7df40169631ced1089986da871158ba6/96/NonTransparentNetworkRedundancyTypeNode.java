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

import org.eclipse.milo.opcua.sdk.server.api.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.server.model.nodes.variables.PropertyTypeNode;
import org.eclipse.milo.opcua.sdk.server.model.types.objects.NonTransparentNetworkRedundancyType;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.NetworkGroupDataType;

public class NonTransparentNetworkRedundancyTypeNode extends NonTransparentRedundancyTypeNode implements NonTransparentNetworkRedundancyType {
    public NonTransparentNetworkRedundancyTypeNode(UaNodeContext context, NodeId nodeId,
                                                   QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                                   UInteger writeMask, UInteger userWriteMask) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask);
    }

    public NonTransparentNetworkRedundancyTypeNode(UaNodeContext context, NodeId nodeId,
                                                   QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                                   UInteger writeMask, UInteger userWriteMask, UByte eventNotifier) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, eventNotifier);
    }

    @Override
    public PropertyTypeNode getServerNetworkGroupsNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(NonTransparentNetworkRedundancyType.SERVER_NETWORK_GROUPS);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public NetworkGroupDataType[] getServerNetworkGroups() {
        Optional<NetworkGroupDataType[]> propertyValue = getProperty(NonTransparentNetworkRedundancyType.SERVER_NETWORK_GROUPS);
        return propertyValue.orElse(null);
    }

    @Override
    public void setServerNetworkGroups(NetworkGroupDataType[] value) {
        setProperty(NonTransparentNetworkRedundancyType.SERVER_NETWORK_GROUPS, value);
    }
}
