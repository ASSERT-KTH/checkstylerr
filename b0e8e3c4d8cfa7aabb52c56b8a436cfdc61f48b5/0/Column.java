package com.bakdata.conquery.models.datasets;

import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.bakdata.conquery.io.jackson.InternalOnly;
import com.bakdata.conquery.io.jackson.serializer.NsIdRef;
import com.bakdata.conquery.io.storage.NamespaceStorage;
import com.bakdata.conquery.models.dictionary.Dictionary;
import com.bakdata.conquery.models.dictionary.MapDictionary;
import com.bakdata.conquery.models.events.MajorTypeId;
import com.bakdata.conquery.models.events.stores.root.ColumnStore;
import com.bakdata.conquery.models.identifiable.Labeled;
import com.bakdata.conquery.models.identifiable.ids.NamespacedIdentifiable;
import com.bakdata.conquery.models.identifiable.ids.specific.ColumnId;
import com.bakdata.conquery.models.identifiable.ids.specific.DictionaryId;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import io.dropwizard.validation.ValidationMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Column extends Labeled<ColumnId> implements NamespacedIdentifiable<ColumnId> {

	public static final int UNKNOWN_POSITION = -1;

	@JsonBackReference
	@NotNull
	@ToString.Exclude
	private Table table;
	@NotNull
	private MajorTypeId type;

	@InternalOnly
	private int position = UNKNOWN_POSITION;
	/**
	 * if set this column should use the given dictionary
	 * if it is of type string, instead of its own dictionary
	 */
	private String sharedDictionary;
	/**
	 * if this is set this column counts as the secondary id of the given name for this
	 * table
	 */
	@NsIdRef
	private SecondaryIdDescription secondaryId;

	@Override
	public ColumnId createId() {
		return new ColumnId(table.getId(), getName());
	}

	//TODO try to remove this method methods, they are quite leaky
	public ColumnStore getTypeFor(Import imp) {
		if (!imp.getTable().equals(getTable())) {
			throw new IllegalArgumentException(String.format("Import %s is not for same table as %s", imp.getTable().getId(), getTable().getId()));
		}

		return Objects.requireNonNull(imp.getColumns()[getPosition()].getTypeDescription(), () -> "No description for Column/Import " + getId() + "/" + imp.getId());
	}

	@Override
	public String toString() {
		return String.format("Column[%s](type = %s)", getId(), getType());
	}

	@ValidationMethod(message = "Only STRING columns can be part of shared Dictionaries.")
	@JsonIgnore
	public boolean isSharedString() {
		return sharedDictionary == null || type.equals(MajorTypeId.STRING);
	}

	@JsonIgnore
	@Override
	public Dataset getDataset() {
		return table.getDataset();
	}

	public void createSharedDictionaryReplacement(Map<String, Dictionary> dicts, NamespaceStorage storage, Map<DictionaryId, Dictionary> out) {
		Preconditions.checkArgument(sharedDictionary != null && type.equals(MajorTypeId.STRING));
		// If the column is based on a shared dict. We reference a new empty dictionary or the existing one
		// but without updated entries. The entries are updated later on, see ImportJob#applyDictionaryMappings.
		Dictionary sharedDict = null;
		synchronized (storage.getLockDummy()) {
			sharedDict = storage.getDictionary(new DictionaryId(table.getDataset().getId(), getSharedDictionary()));
			if (sharedDict == null) {
				sharedDict = new MapDictionary(table.getDataset(), getSharedDictionary());
				storage.updateDictionary(sharedDict);
			}
		}
		out.put(new DictionaryId(Dataset.PLACEHOLDER.getId(), dicts.get(getName()).getName()), sharedDict);
	}



	public void createdSingleColumnDictionaryReplacement(Map<String, Dictionary> dicts, String importName, Map<DictionaryId, Dictionary> out) {
		Preconditions.checkArgument(sharedDictionary == null && type.equals(MajorTypeId.STRING));
		
		final Dictionary dict = dicts.get(getName());
		final String name = computeDefaultDictionaryName(importName);

		out.put(new DictionaryId(Dataset.PLACEHOLDER.getId(), dict.getName()), dict);

		dict.setDataset(table.getDataset());
		dict.setName(name);
	}


	private String computeDefaultDictionaryName(String importName) {
		return String.format("%s#%s", importName, getId().toString());
	}
}
