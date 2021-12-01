package com.bakdata.conquery.models.types;

import java.io.IOException;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.util.function.Consumer;

import com.bakdata.conquery.io.cps.CPSBase;
import com.bakdata.conquery.io.xodus.NamespacedStorage;
import com.bakdata.conquery.models.dictionary.Dictionary;
import com.bakdata.conquery.models.identifiable.ids.specific.DatasetId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.google.common.math.LongMath;
import com.google.common.primitives.Primitives;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@JsonTypeInfo(use=JsonTypeInfo.Id.CUSTOM, property="type")
@CPSBase
@Getter @Setter @RequiredArgsConstructor
public abstract class CType<MAJOR_JAVA_TYPE, JAVA_TYPE> implements MajorTypeIdHolder {

	@JsonIgnore
	private transient final MajorTypeId typeId;
	@JsonIgnore @NonNull
	private transient Class<?> primitiveType;

	private long lines = 0;
	private long nullLines = 0;

	public void init(DatasetId dataset) {}
	
	public Object createScriptValue(JAVA_TYPE value) {
		return value;
	}

	public Object createPrintValue(JAVA_TYPE value) { return value != null ? createScriptValue(value) : ""; }

	public void writeHeader(OutputStream out) throws IOException {}
	public void readHeader(JsonParser input) throws IOException {}
	public void storeExternalInfos(NamespacedStorage storage, Consumer<Dictionary> dictionaryConsumer) {}
	public void loadExternalInfos(NamespacedStorage storage) {}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	public abstract boolean canStoreNull();

	public boolean requiresExternalNullStore() {
		return !canStoreNull() && getNullLines()>0;
	}

	@JsonIgnore
	public Class<?> getBoxedType(){
		return Primitives.wrap(getPrimitiveType());
	}
	
	public long estimateMemoryConsumption() {
		long width = estimateMemoryBitWidth();
		if(!canStoreNull()) {
			width++;
		}
		return LongMath.divide(
			(lines-nullLines) * width + nullLines * Math.min(Long.SIZE, width),
			8, RoundingMode.CEILING
		); 
	}

	public abstract long estimateMemoryBitWidth();
	
	public long estimateTypeSize() {
		return 0;
	}
}
