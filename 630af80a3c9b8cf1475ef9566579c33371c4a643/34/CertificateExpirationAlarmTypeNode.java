package org.eclipse.milo.opcua.sdk.client.model.nodes.objects;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.model.nodes.variables.PropertyTypeNode;
import org.eclipse.milo.opcua.sdk.client.model.types.objects.CertificateExpirationAlarmType;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;

public class CertificateExpirationAlarmTypeNode extends SystemOffNormalAlarmTypeNode implements CertificateExpirationAlarmType {
    public CertificateExpirationAlarmTypeNode(OpcUaClient client, NodeId nodeId, NodeClass nodeClass,
                                              QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                              UInteger writeMask, UInteger userWriteMask, UByte eventNotifier) {
        super(client, nodeId, nodeClass, browseName, displayName, description, writeMask, userWriteMask, eventNotifier);
    }

    @Override
    public DateTime getExpirationDate() throws UaException {
        PropertyTypeNode node = getExpirationDateNode();
        return (DateTime) node.getValue().getValue().getValue();
    }

    @Override
    public void setExpirationDate(DateTime expirationDate) throws UaException {
        PropertyTypeNode node = getExpirationDateNode();
        node.setValue(new Variant(expirationDate));
    }

    @Override
    public DateTime readExpirationDate() throws UaException {
        try {
            return readExpirationDateAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public void writeExpirationDate(DateTime expirationDate) throws UaException {
        try {
            writeExpirationDateAsync(expirationDate).get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends DateTime> readExpirationDateAsync() {
        return getExpirationDateNodeAsync().thenCompose(node -> node.readAttributeAsync(AttributeId.Value)).thenApply(v -> (DateTime) v.getValue().getValue());
    }

    @Override
    public CompletableFuture<StatusCode> writeExpirationDateAsync(DateTime expirationDate) {
        DataValue value = DataValue.valueOnly(new Variant(expirationDate));
        return getExpirationDateNodeAsync()
            .thenCompose(node -> node.writeAttributeAsync(AttributeId.Value, value));
    }

    @Override
    public PropertyTypeNode getExpirationDateNode() throws UaException {
        try {
            return getExpirationDateNodeAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends PropertyTypeNode> getExpirationDateNodeAsync() {
        CompletableFuture<UaNode> future = getMemberNodeAsync("http://opcfoundation.org/UA/", "ExpirationDate", ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=46"), false);
        return future.thenApply(node -> (PropertyTypeNode) node);
    }

    @Override
    public Double getExpirationLimit() throws UaException {
        PropertyTypeNode node = getExpirationLimitNode();
        return (Double) node.getValue().getValue().getValue();
    }

    @Override
    public void setExpirationLimit(Double expirationLimit) throws UaException {
        PropertyTypeNode node = getExpirationLimitNode();
        node.setValue(new Variant(expirationLimit));
    }

    @Override
    public Double readExpirationLimit() throws UaException {
        try {
            return readExpirationLimitAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public void writeExpirationLimit(Double expirationLimit) throws UaException {
        try {
            writeExpirationLimitAsync(expirationLimit).get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends Double> readExpirationLimitAsync() {
        return getExpirationLimitNodeAsync().thenCompose(node -> node.readAttributeAsync(AttributeId.Value)).thenApply(v -> (Double) v.getValue().getValue());
    }

    @Override
    public CompletableFuture<StatusCode> writeExpirationLimitAsync(Double expirationLimit) {
        DataValue value = DataValue.valueOnly(new Variant(expirationLimit));
        return getExpirationLimitNodeAsync()
            .thenCompose(node -> node.writeAttributeAsync(AttributeId.Value, value));
    }

    @Override
    public PropertyTypeNode getExpirationLimitNode() throws UaException {
        try {
            return getExpirationLimitNodeAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends PropertyTypeNode> getExpirationLimitNodeAsync() {
        CompletableFuture<UaNode> future = getMemberNodeAsync("http://opcfoundation.org/UA/", "ExpirationLimit", ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=46"), false);
        return future.thenApply(node -> (PropertyTypeNode) node);
    }

    @Override
    public NodeId getCertificateType() throws UaException {
        PropertyTypeNode node = getCertificateTypeNode();
        return (NodeId) node.getValue().getValue().getValue();
    }

    @Override
    public void setCertificateType(NodeId certificateType) throws UaException {
        PropertyTypeNode node = getCertificateTypeNode();
        node.setValue(new Variant(certificateType));
    }

    @Override
    public NodeId readCertificateType() throws UaException {
        try {
            return readCertificateTypeAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public void writeCertificateType(NodeId certificateType) throws UaException {
        try {
            writeCertificateTypeAsync(certificateType).get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends NodeId> readCertificateTypeAsync() {
        return getCertificateTypeNodeAsync().thenCompose(node -> node.readAttributeAsync(AttributeId.Value)).thenApply(v -> (NodeId) v.getValue().getValue());
    }

    @Override
    public CompletableFuture<StatusCode> writeCertificateTypeAsync(NodeId certificateType) {
        DataValue value = DataValue.valueOnly(new Variant(certificateType));
        return getCertificateTypeNodeAsync()
            .thenCompose(node -> node.writeAttributeAsync(AttributeId.Value, value));
    }

    @Override
    public PropertyTypeNode getCertificateTypeNode() throws UaException {
        try {
            return getCertificateTypeNodeAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends PropertyTypeNode> getCertificateTypeNodeAsync() {
        CompletableFuture<UaNode> future = getMemberNodeAsync("http://opcfoundation.org/UA/", "CertificateType", ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=46"), false);
        return future.thenApply(node -> (PropertyTypeNode) node);
    }

    @Override
    public ByteString getCertificate() throws UaException {
        PropertyTypeNode node = getCertificateNode();
        return (ByteString) node.getValue().getValue().getValue();
    }

    @Override
    public void setCertificate(ByteString certificate) throws UaException {
        PropertyTypeNode node = getCertificateNode();
        node.setValue(new Variant(certificate));
    }

    @Override
    public ByteString readCertificate() throws UaException {
        try {
            return readCertificateAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public void writeCertificate(ByteString certificate) throws UaException {
        try {
            writeCertificateAsync(certificate).get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends ByteString> readCertificateAsync() {
        return getCertificateNodeAsync().thenCompose(node -> node.readAttributeAsync(AttributeId.Value)).thenApply(v -> (ByteString) v.getValue().getValue());
    }

    @Override
    public CompletableFuture<StatusCode> writeCertificateAsync(ByteString certificate) {
        DataValue value = DataValue.valueOnly(new Variant(certificate));
        return getCertificateNodeAsync()
            .thenCompose(node -> node.writeAttributeAsync(AttributeId.Value, value));
    }

    @Override
    public PropertyTypeNode getCertificateNode() throws UaException {
        try {
            return getCertificateNodeAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends PropertyTypeNode> getCertificateNodeAsync() {
        CompletableFuture<UaNode> future = getMemberNodeAsync("http://opcfoundation.org/UA/", "Certificate", ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=46"), false);
        return future.thenApply(node -> (PropertyTypeNode) node);
    }
}
