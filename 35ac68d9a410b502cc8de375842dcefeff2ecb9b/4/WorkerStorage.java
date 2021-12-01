package com.bakdata.conquery.io.storage;

import com.bakdata.conquery.ConqueryConstants;
import com.bakdata.conquery.io.storage.xodus.stores.SingletonStore;
import com.bakdata.conquery.models.concepts.Concept;
import com.bakdata.conquery.models.config.StoreFactory;
import com.bakdata.conquery.models.dictionary.Dictionary;
import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.events.CBlock;
import com.bakdata.conquery.models.identifiable.ids.specific.BucketId;
import com.bakdata.conquery.models.identifiable.ids.specific.CBlockId;
import com.bakdata.conquery.models.identifiable.ids.specific.ConceptId;
import com.bakdata.conquery.models.worker.WorkerInformation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Validator;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Slf4j
public class WorkerStorage extends NamespacedStorage {

    private SingletonStore<WorkerInformation> worker;
    private IdentifiableStore<Bucket> buckets;
    private IdentifiableStore<CBlock> cBlocks;

    @Getter
    private final boolean registerImports = false;

    public WorkerStorage(Validator validator, StoreFactory storageFactory, List<String> pathName) {
        super(validator, storageFactory, pathName);

        worker = storageFactory.createWorkerInformationStore(pathName);
        buckets = storageFactory.createBucketStore(centralRegistry, pathName);
        cBlocks = storageFactory.createCBlockStore(centralRegistry, pathName);

        decorateWorkerStore(worker);
        decorateBucketStore(buckets);
        decorateCBlockStore(cBlocks);
    }

    public void loadData() {
        super.loadData();
        worker.loadData();
        buckets.loadData();
        cBlocks.loadData();
    }

    @Override
    public void removeStorage() {
        super.removeStorage();

        worker.removeStore();
        buckets.removeStore();
        cBlocks.removeStore();
    }

    public void close() throws IOException {
        super.close();

        worker.close();
        buckets.close();
        cBlocks.close();
    }



    private void decorateWorkerStore(SingletonStore<WorkerInformation> store) {
        // Nothing to decorate
    }

    private void decorateBucketStore(IdentifiableStore<Bucket> store) {
        store
                .onAdd((bucket) -> {
                    bucket.loadDictionaries(this);
                });
    }

    private void decorateCBlockStore(IdentifiableStore<CBlock> baseStoreCreator) {
        // Nothing to decorate
    }


    public void addCBlock(CBlock cBlock) {
        log.debug("Adding CBlock[{}]", cBlock.getId());
        cBlocks.add(cBlock);
    }

    public CBlock getCBlock(CBlockId id) {
        return cBlocks.get(id);
    }

    // TODO method is unused, delete it.
    public void updateCBlock(CBlock cBlock) {
        cBlocks.update(cBlock);
    }

    public void removeCBlock(CBlockId id) {
        log.debug("Removing CBlock[{}]", id);
        cBlocks.remove(id);
    }

    public void addDictionary(Dictionary dict) {
        if (dict.getId().equals(ConqueryConstants.getPrimaryDictionary(getDataset()))) {
            throw new IllegalStateException("Workers may not receive the primary dictionary");
        }

        super.addDictionary(dict);
    }

    public Collection<CBlock> getAllCBlocks() {
        return cBlocks.getAll();
    }

    public void addBucket(Bucket bucket) {
        log.debug("Adding Bucket[{}]", bucket.getId());
        buckets.add(bucket);
    }

    public Bucket getBucket(BucketId id) {
        return buckets.get(id);
    }

    public void removeBucket(BucketId id) {
        log.debug("Removing Bucket[{}]", id);
        buckets.remove(id);
    }

    public Collection<Bucket> getAllBuckets() {
        return buckets.getAll();
    }

    public WorkerInformation getWorker() {
        return worker.get();
    }

    //TODO remove duplication
    public void setWorker(WorkerInformation worker) {
        this.worker.add(worker);
    }

    public void updateWorker(WorkerInformation worker) {
        this.worker.update(worker);
    }

    //block manager overrides
    public void updateConcept(Concept<?> concept) {
        log.debug("Updating Concept[{}]", concept.getId());
        concepts.update(concept);
    }

    public void removeConcept(ConceptId id) {
        log.debug("Removing Concept[{}]", id);
        concepts.remove(id);
    }
}
