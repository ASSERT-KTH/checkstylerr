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
import org.eclipse.milo.opcua.sdk.server.model.types.objects.AuditOpenSecureChannelEventType;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.SecurityTokenRequestType;

public class AuditOpenSecureChannelEventTypeNode extends AuditChannelEventTypeNode implements AuditOpenSecureChannelEventType {
    public AuditOpenSecureChannelEventTypeNode(UaNodeContext context, NodeId nodeId,
                                               QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                               UInteger writeMask, UInteger userWriteMask) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask);
    }

    public AuditOpenSecureChannelEventTypeNode(UaNodeContext context, NodeId nodeId,
                                               QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                               UInteger writeMask, UInteger userWriteMask, UByte eventNotifier) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, eventNotifier);
    }

    @Override
    public PropertyTypeNode getClientCertificateNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditOpenSecureChannelEventType.CLIENT_CERTIFICATE);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public ByteString getClientCertificate() {
        Optional<ByteString> propertyValue = getProperty(AuditOpenSecureChannelEventType.CLIENT_CERTIFICATE);
        return propertyValue.orElse(null);
    }

    @Override
    public void setClientCertificate(ByteString value) {
        setProperty(AuditOpenSecureChannelEventType.CLIENT_CERTIFICATE, value);
    }

    @Override
    public PropertyTypeNode getClientCertificateThumbprintNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditOpenSecureChannelEventType.CLIENT_CERTIFICATE_THUMBPRINT);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public String getClientCertificateThumbprint() {
        Optional<String> propertyValue = getProperty(AuditOpenSecureChannelEventType.CLIENT_CERTIFICATE_THUMBPRINT);
        return propertyValue.orElse(null);
    }

    @Override
    public void setClientCertificateThumbprint(String value) {
        setProperty(AuditOpenSecureChannelEventType.CLIENT_CERTIFICATE_THUMBPRINT, value);
    }

    @Override
    public PropertyTypeNode getRequestTypeNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditOpenSecureChannelEventType.REQUEST_TYPE);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public SecurityTokenRequestType getRequestType() {
        Optional<SecurityTokenRequestType> propertyValue = getProperty(AuditOpenSecureChannelEventType.REQUEST_TYPE);
        return propertyValue.orElse(null);
    }

    @Override
    public void setRequestType(SecurityTokenRequestType value) {
        setProperty(AuditOpenSecureChannelEventType.REQUEST_TYPE, value);
    }

    @Override
    public PropertyTypeNode getSecurityPolicyUriNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditOpenSecureChannelEventType.SECURITY_POLICY_URI);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public String getSecurityPolicyUri() {
        Optional<String> propertyValue = getProperty(AuditOpenSecureChannelEventType.SECURITY_POLICY_URI);
        return propertyValue.orElse(null);
    }

    @Override
    public void setSecurityPolicyUri(String value) {
        setProperty(AuditOpenSecureChannelEventType.SECURITY_POLICY_URI, value);
    }

    @Override
    public PropertyTypeNode getSecurityModeNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditOpenSecureChannelEventType.SECURITY_MODE);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public MessageSecurityMode getSecurityMode() {
        Optional<MessageSecurityMode> propertyValue = getProperty(AuditOpenSecureChannelEventType.SECURITY_MODE);
        return propertyValue.orElse(null);
    }

    @Override
    public void setSecurityMode(MessageSecurityMode value) {
        setProperty(AuditOpenSecureChannelEventType.SECURITY_MODE, value);
    }

    @Override
    public PropertyTypeNode getRequestedLifetimeNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditOpenSecureChannelEventType.REQUESTED_LIFETIME);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public Double getRequestedLifetime() {
        Optional<Double> propertyValue = getProperty(AuditOpenSecureChannelEventType.REQUESTED_LIFETIME);
        return propertyValue.orElse(null);
    }

    @Override
    public void setRequestedLifetime(Double value) {
        setProperty(AuditOpenSecureChannelEventType.REQUESTED_LIFETIME, value);
    }
}
