package com.synaptix.toast.dao.service.dao.common;

import java.util.List;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Datastore;
import com.github.jmkgreen.morphia.Key;
import com.github.jmkgreen.morphia.dao.BasicDAO;
import com.github.jmkgreen.morphia.query.Query;
import com.google.inject.Inject;
import com.synaptix.toast.dao.domain.impl.common.TagImpl;
import com.synaptix.toast.dao.domain.impl.test.block.ITaggable;

public abstract class AbstractMongoDaoService<E extends ITaggable> extends
		BasicDAO<E, ObjectId> implements ICrudDaoService<E> {

	@Inject
	protected EntityCollectionManager entityManager;

	protected CommonMongoDaoService commonService;

	Class<E> clazz;

	@Inject
	protected AbstractMongoDaoService(Class<E> clazz, Datastore ds,
			CommonMongoDaoService service) {
		super(ds);
		this.commonService = service;
		this.clazz = clazz;
	}

	public List<E> getByTag(TagImpl tag) {
		return commonService.getTaggedItems(getDatastore(), clazz, tag);
	}

	@Override
	public Key<E> saveAndIndex(E entity) {
		Key<E> save = super.save(entity);
		String collection = entityManager.getCollection(clazz);
		if (collection != null) {
			commonService.indexEntity(ds, save.getKind(), save.getId()
					.toString(), entity);
		}
		return save;
	}

}
