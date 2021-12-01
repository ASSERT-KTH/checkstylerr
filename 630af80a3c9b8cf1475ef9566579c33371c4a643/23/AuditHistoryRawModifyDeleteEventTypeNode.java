package org.eclipse.milo.opcua.sdk.client.model.nodes.objects;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.model.nodes.variables.PropertyTypeNode;
import org.eclipse.milo.opcua.sdk.client.model.types.objects.AuditHistoryRawModifyDeleteEventType;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
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

public class AuditHistoryRawModifyDeleteEventTypeNode extends AuditHistoryDeleteEventTypeNode implements AuditHistoryRawModifyDeleteEventType {
    public AuditHistoryRawModifyDeleteEventTypeNode(OpcUaClient client, NodeId nodeId,
                                                    NodeClass nodeClass, QualifiedName browseName, LocalizedText displayName,
                                                    LocalizedText description, UInteger writeMask, UInteger userWriteMask, UByte eventNotifier) {
        super(client, nodeId, nodeClass, browseName, displayName, description, writeMask, userWriteMask, eventNotifier);
    }

    @Override
    public Boolean getIsDeleteModified() throws UaException {
        PropertyTypeNode node = getIsDeleteModifiedNode();
        return (Boolean) node.getValue().getValue().getValue();
    }

    @Override
    public void setIsDeleteModified(Boolean isDeleteModified) throws UaException {
        PropertyTypeNode node = getIsDeleteModifiedNode();
        node.setValue(new Variant(isDeleteModified));
    }

    @Override
    public Boolean readIsDeleteModified() throws UaException {
        try {
            return readIsDeleteModifiedAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public void writeIsDeleteModified(Boolean isDeleteModified) throws UaException {
        try {
            writeIsDeleteModifiedAsync(isDeleteModified).get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends Boolean> readIsDeleteModifiedAsync() {
        return getIsDeleteModifiedNodeAsync().thenCompose(node -> node.readAttributeAsync(AttributeId.Value)).thenApply(v -> (Boolean) v.getValue().getValue());
    }

    @Override
    public CompletableFuture<StatusCode> writeIsDeleteModifiedAsync(Boolean isDeleteModified) {
        DataValue value = DataValue.valueOnly(new Variant(isDeleteModified));
        return getIsDeleteModifiedNodeAsync()
            .thenCompose(node -> node.writeAttributeAsync(AttributeId.Value, value));
    }

    @Override
    public PropertyTypeNode getIsDeleteModifiedNode() throws UaException {
        try {
            return getIsDeleteModifiedNodeAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends PropertyTypeNode> getIsDeleteModifiedNodeAsync() {
        CompletableFuture<UaNode> future = getMemberNodeAsync("http://opcfoundation.org/UA/", "IsDeleteModified", ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=46"), false);
        return future.thenApply(node -> (PropertyTypeNode) node);
    }

    @Override
    public DateTime getStartTime() throws UaException {
        PropertyTypeNode node = getStartTimeNode();
        return (DateTime) node.getValue().getValue().getValue();
    }

    @Override
    public void setStartTime(DateTime startTime) throws UaException {
        PropertyTypeNode node = getStartTimeNode();
        node.setValue(new Variant(startTime));
    }

    @Override
    public DateTime readStartTime() throws UaException {
        try {
            return readStartTimeAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public void writeStartTime(DateTime startTime) throws UaException {
        try {
            writeStartTimeAsync(startTime).get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends DateTime> readStartTimeAsync() {
        return getStartTimeNodeAsync().thenCompose(node -> node.readAttributeAsync(AttributeId.Value)).thenApply(v -> (DateTime) v.getValue().getValue());
    }

    @Override
    public CompletableFuture<StatusCode> writeStartTimeAsync(DateTime startTime) {
        DataValue value = DataValue.valueOnly(new Variant(startTime));
        return getStartTimeNodeAsync()
            .thenCompose(node -> node.writeAttributeAsync(AttributeId.Value, value));
    }

    @Override
    public PropertyTypeNode getStartTimeNode() throws UaException {
        try {
            return getStartTimeNodeAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends PropertyTypeNode> getStartTimeNodeAsync() {
        CompletableFuture<UaNode> future = getMemberNodeAsync("http://opcfoundation.org/UA/", "StartTime", ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=46"), false);
        return future.thenApply(node -> (PropertyTypeNode) node);
    }

    @Override
    public DateTime getEndTime() throws UaException {
        PropertyTypeNode node = getEndTimeNode();
        return (DateTime) node.getValue().getValue().getValue();
    }

    @Override
    public void setEndTime(DateTime endTime) throws UaException {
        PropertyTypeNode node = getEndTimeNode();
        node.setValue(new Variant(endTime));
    }

    @Override
    public DateTime readEndTime() throws UaException {
        try {
            return readEndTimeAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public void writeEndTime(DateTime endTime) throws UaException {
        try {
            writeEndTimeAsync(endTime).get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends DateTime> readEndTimeAsync() {
        return getEndTimeNodeAsync().thenCompose(node -> node.readAttributeAsync(AttributeId.Value)).thenApply(v -> (DateTime) v.getValue().getValue());
    }

    @Override
    public CompletableFuture<StatusCode> writeEndTimeAsync(DateTime endTime) {
        DataValue value = DataValue.valueOnly(new Variant(endTime));
        return getEndTimeNodeAsync()
            .thenCompose(node -> node.writeAttributeAsync(AttributeId.Value, value));
    }

    @Override
    public PropertyTypeNode getEndTimeNode() throws UaException {
        try {
            return getEndTimeNodeAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends PropertyTypeNode> getEndTimeNodeAsync() {
        CompletableFuture<UaNode> future = getMemberNodeAsync("http://opcfoundation.org/UA/", "EndTime", ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=46"), false);
        return future.thenApply(node -> (PropertyTypeNode) node);
    }

    @Override
    public DataValue[] getOldValues() throws UaException {
        PropertyTypeNode node = getOldValuesNode();
        return (DataValue[]) node.getValue().getValue().getValue();
    }

    @Override
    public void setOldValues(DataValue[] oldValues) throws UaException {
        PropertyTypeNode node = getOldValuesNode();
        node.setValue(new Variant(oldValues));
    }

    @Override
    public DataValue[] readOldValues() throws UaException {
        try {
            return readOldValuesAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public void writeOldValues(DataValue[] oldValues) throws UaException {
        try {
            writeOldValuesAsync(oldValues).get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends DataValue[]> readOldValuesAsync() {
        return getOldValuesNodeAsync().thenCompose(node -> node.readAttributeAsync(AttributeId.Value)).thenApply(v -> (DataValue[]) v.getValue().getValue());
    }

    @Override
    public CompletableFuture<StatusCode> writeOldValuesAsync(DataValue[] oldValues) {
        DataValue value = DataValue.valueOnly(new Variant(oldValues));
        return getOldValuesNodeAsync()
            .thenCompose(node -> node.writeAttributeAsync(AttributeId.Value, value));
    }

    @Override
    public PropertyTypeNode getOldValuesNode() throws UaException {
        try {
            return getOldValuesNodeAsync().get();
        } catch (ExecutionException | InterruptedException e) {
            throw UaException.extract(e).orElse(new UaException(StatusCodes.Bad_UnexpectedError, e));
        }
    }

    @Override
    public CompletableFuture<? extends PropertyTypeNode> getOldValuesNodeAsync() {
        CompletableFuture<UaNode> future = getMemberNodeAsync("http://opcfoundation.org/UA/", "OldValues", ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=46"), false);
        return future.thenApply(node -> (PropertyTypeNode) node);
    }
}
