package com.bakdata.conquery.io.xodus;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import ch.qos.logback.access.jetty.RequestLogRegistry;
import com.bakdata.conquery.io.xodus.stores.IdentifiableStore;
import com.bakdata.conquery.io.xodus.stores.SingletonStore;
import com.bakdata.conquery.io.xodus.stores.Store;
import com.bakdata.conquery.models.concepts.Concept;
import com.bakdata.conquery.models.concepts.Connector;
import com.bakdata.conquery.models.concepts.filters.Filter;
import com.bakdata.conquery.models.datasets.*;
import com.bakdata.conquery.models.dictionary.Dictionary;
import com.bakdata.conquery.models.dictionary.EncodedDictionary;
import com.bakdata.conquery.models.identifiable.CentralRegistry;
import com.bakdata.conquery.models.identifiable.ids.specific.ConceptId;
import com.bakdata.conquery.models.identifiable.ids.specific.DictionaryId;
import com.bakdata.conquery.models.identifiable.ids.specific.ImportId;
import com.bakdata.conquery.models.identifiable.ids.specific.SecondaryIdDescriptionId;
import com.bakdata.conquery.models.identifiable.ids.specific.TableId;

public interface NamespacedStorage extends ConqueryStorage {
	
	void addDictionary(Dictionary dict);
	Dictionary getDictionary(DictionaryId id);
	void updateDictionary(Dictionary dict);
	void removeDictionary(DictionaryId id);
	EncodedDictionary getPrimaryDictionary();
	
	void addImport(Import imp);
	Import getImport(ImportId id);
	Collection<Import> getAllImports();
	void updateImport(Import imp);
	void removeImport(ImportId id);
	
	Dataset getDataset();
	void updateDataset(Dataset dataset);

	List<Table> getTables();
	Table getTable(TableId tableId);
	void addTable(Table table);
	void removeTable(TableId table);

	List<SecondaryIdDescription> getSecondaryIds();
	SecondaryIdDescription getSecondaryId(SecondaryIdDescriptionId descriptionId);
	void addSecondaryId(SecondaryIdDescription secondaryIdDescription);
	void removeSecondaryId(SecondaryIdDescriptionId secondaryIdDescriptionId);



	Concept<?> getConcept(ConceptId id);
	boolean hasConcept(ConceptId id);
	void updateConcept(Concept<?> concept);
	void removeConcept(ConceptId id);
	Collection<? extends Concept<?>> getAllConcepts();

	boolean isRegisterImports();


	default SingletonStore<Dataset> createDatasetStore(Function<StoreInfo,Store> baseStoreCreator) {
		return StoreInfo.DATASET.<Dataset>singleton(baseStoreCreator.apply(StoreInfo.DATASET))
				.onAdd(getCentralRegistry()::register)
				.onRemove(getCentralRegistry()::remove);
	}

	default IdentifiableStore<SecondaryIdDescription> createSecondaryIdDescriptionStore(Function<StoreInfo,Store> baseStoreCreator) {
		return StoreInfo.SECONDARY_IDS.<SecondaryIdDescription>identifiable(baseStoreCreator.apply(StoreInfo.SECONDARY_IDS), getCentralRegistry());
	}

	default IdentifiableStore<Table> createTableStore(Function<StoreInfo,Store> baseStoreCreator) {
		return StoreInfo.TABLES.<Table>identifiable(baseStoreCreator.apply(StoreInfo.TABLES), getCentralRegistry())
				.onAdd(table -> {
					for (Column c : table.getColumns()) {
						getCentralRegistry().register(c);
					}
				})
				.onRemove(table -> {
					for (Column c : table.getColumns()) {
						getCentralRegistry().remove(c);
					}
				});
	}

	default IdentifiableStore<Concept<?>> createConceptStore(Function<StoreInfo,Store> baseStoreCreator) {
		CentralRegistry centralRegistry = getCentralRegistry();
		return StoreInfo.CONCEPTS.<Concept<?>>identifiable(baseStoreCreator.apply(StoreInfo.CONCEPTS), getCentralRegistry())
				.onAdd(concept -> {
					Dataset ds = centralRegistry.resolve(
							concept.getDataset() == null
									? concept.getId().getDataset()
									: concept.getDataset()
					);
					concept.setDataset(ds.getId());

					concept.initElements(getValidator());

					concept.getSelects().forEach(centralRegistry::register);
					for (Connector c : concept.getConnectors()) {
						centralRegistry.register(c);
						c.collectAllFilters().forEach(centralRegistry::register);
						c.getSelects().forEach(centralRegistry::register);
					}
					//add imports of table
					if (isRegisterImports()) {
						for (Import imp : getAllImports()) {
							for (Connector con : concept.getConnectors()) {
								if (con.getTable().getId().equals(imp.getTable())) {
									con.addImport(imp);
								}
							}
						}
					}
				})
				.onRemove(concept -> {
					concept.getSelects().forEach(centralRegistry::remove);
					//see #146  remove from Dataset.concepts
					for (Connector c : concept.getConnectors()) {
						c.getSelects().forEach(centralRegistry::remove);
						c.collectAllFilters().stream().map(Filter::getId).forEach(centralRegistry::remove);
						centralRegistry.remove(c.getId());
					}
				});
	}

	default IdentifiableStore<Import> createImportStore(Function<StoreInfo,Store> baseStoreCreator) {
		return StoreInfo.IMPORTS.<Import>identifiable(baseStoreCreator.apply(StoreInfo.IMPORTS), getCentralRegistry())
				.onAdd(imp -> {
					imp.loadExternalInfos(this);

					if (isRegisterImports()) {
						for (Concept<?> c : getAllConcepts()) {
							for (Connector con : c.getConnectors()) {
								if (con.getTable().getId().equals(imp.getTable())) {
									con.addImport(imp);
								}
							}
						}
					}

					getCentralRegistry().register(imp);

				})
				.onRemove(imp -> {
					getCentralRegistry().remove(imp);

				});
	}

}
