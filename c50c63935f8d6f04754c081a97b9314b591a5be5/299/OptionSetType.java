package org.eclipse.milo.opcua.sdk.client.model.types.variables;

import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;

public interface OptionSetType extends BaseDataVariableType {
    QualifiedProperty<LocalizedText[]> OPTION_SET_VALUES = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "OptionSetValues",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=21"),
        ValueRanks.OneDimension,
        LocalizedText[].class
    );

    QualifiedProperty<Boolean[]> BIT_MASK = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "BitMask",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=1"),
        ValueRanks.OneDimension,
        Boolean[].class
    );

    /**
     * Get the local value of the OptionSetValues Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the OptionSetValues Node.
     * @throws UaException if an error occurs creating or getting the OptionSetValues Node.
     */
    LocalizedText[] getOptionSetValues() throws UaException;

    /**
     * Set the local value of the OptionSetValues Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param optionSetValues the local value to set for the OptionSetValues Node.
     * @throws UaException if an error occurs creating or getting the OptionSetValues Node.
     */
    void setOptionSetValues(LocalizedText[] optionSetValues) throws UaException;

    /**
     * Read the value of the OptionSetValues Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link LocalizedText[]} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    LocalizedText[] readOptionSetValues() throws UaException;

    /**
     * Write a new value for the OptionSetValues Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param optionSetValues the {@link LocalizedText[]} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeOptionSetValues(LocalizedText[] optionSetValues) throws UaException;

    /**
     * An asynchronous implementation of {@link #readOptionSetValues()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends LocalizedText[]> readOptionSetValuesAsync();

    /**
     * An asynchronous implementation of {@link #writeOptionSetValues(LocalizedText[])}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeOptionSetValuesAsync(LocalizedText[] optionSetValues);

    /**
     * Get the OptionSetValues {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the OptionSetValues {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getOptionSetValuesNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getOptionSetValuesNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getOptionSetValuesNodeAsync();

    /**
     * Get the local value of the BitMask Node.
     * <p>
     * The returned value is the last seen; it is not read live from the server.
     *
     * @return the local value of the BitMask Node.
     * @throws UaException if an error occurs creating or getting the BitMask Node.
     */
    Boolean[] getBitMask() throws UaException;

    /**
     * Set the local value of the BitMask Node.
     * <p>
     * The value is only updated locally; it is not written to the server.
     *
     * @param bitMask the local value to set for the BitMask Node.
     * @throws UaException if an error occurs creating or getting the BitMask Node.
     */
    void setBitMask(Boolean[] bitMask) throws UaException;

    /**
     * Read the value of the BitMask Node from the server and update the local value if the
     * operation succeeds.
     *
     * @return the {@link Boolean[]} value read from the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    Boolean[] readBitMask() throws UaException;

    /**
     * Write a new value for the BitMask Node to the server and update the local value if
     * the operation succeeds.
     *
     * @param bitMask the {@link Boolean[]} value to write to the server.
     * @throws UaException if a service- or operation-level error occurs.
     */
    void writeBitMask(Boolean[] bitMask) throws UaException;

    /**
     * An asynchronous implementation of {@link #readBitMask()}.
     *
     * @return a CompletableFuture that completes successfully with the property value or completes
     * exceptionally if an operation- or service-level error occurs.
     */
    CompletableFuture<? extends Boolean[]> readBitMaskAsync();

    /**
     * An asynchronous implementation of {@link #writeBitMask(Boolean[])}.
     *
     * @return a CompletableFuture that completes successfully with the operation result or
     * completes exceptionally if a service-level error occurs.
     */
    CompletableFuture<StatusCode> writeBitMaskAsync(Boolean[] bitMask);

    /**
     * Get the BitMask {@link PropertyType} Node, or {@code null} if it does not exist.
     * <p>
     * The Node is created when first accessed and cached for subsequent calls.
     *
     * @return the BitMask {@link PropertyType} Node, or {@code null} if it does not exist.
     * @throws UaException if an error occurs creating or getting the Node.
     */
    PropertyType getBitMaskNode() throws UaException;

    /**
     * Asynchronous implementation of {@link #getBitMaskNode()}.
     *
     * @return a CompletableFuture that completes successfully with the
     * ? extends PropertyType Node or completes exceptionally if an error occurs
     * creating or getting the Node.
     */
    CompletableFuture<? extends PropertyType> getBitMaskNodeAsync();
}
