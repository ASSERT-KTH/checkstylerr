package com.bakdata.conquery.models.execution;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.bakdata.conquery.io.xodus.MasterMetaStorage;
import com.bakdata.conquery.models.auth.entities.PermissionOwner;
import com.bakdata.conquery.models.auth.entities.User;
import com.bakdata.conquery.models.auth.permissions.Ability;
import com.bakdata.conquery.models.auth.permissions.ConqueryPermission;
import com.bakdata.conquery.models.identifiable.Identifiable;
import com.bakdata.conquery.models.identifiable.ids.IId;
import com.bakdata.conquery.resources.api.StoredQueriesResource.QueryPatch;
import com.bakdata.conquery.util.QueryUtils;

public interface Shareable {
	static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Shareable.class);
	
	boolean isShared();
	void setShared(boolean shared);
	
	
	default  <ID extends IId<?>,S extends Identifiable<? extends ID> & Shareable, O extends PermissionOwner<? extends IId<O>>> Consumer<QueryPatch> sharer(MasterMetaStorage storage, User user, O other, BiFunction<Ability, ID , ConqueryPermission> sharedPermissionCreator) {
		if(!(this instanceof Identifiable<?>)) {
			log.warn("Cannot share {} ({}) because it does not implement Identifiable", this.getClass(), this.toString());
			return QueryUtils.getNoOpEntryPoint();
		}
		return (patch) -> {
			shareWithOther(
				storage,
				user,
				(S) this,
				sharedPermissionCreator, 
				other,
				patch.getShared());
		};
		
	}

	
	/**
	 * (Un)Shares a query with a specific group. Set or unsets the shared flag.
	 * Does persist this change made to the {@link Shareable}. 
	 */
	public static <ID extends IId<?>, S extends Identifiable<? extends ID> & Shareable, O extends PermissionOwner<? extends IId<O>>> void shareWithOther(
		MasterMetaStorage storage,
		User user,
		S shareable,
		BiFunction<Ability,ID, ConqueryPermission> sharedPermissionCreator,
		O other,
		boolean shared) {
		
		ConqueryPermission sharePermission = sharedPermissionCreator.apply(Ability.SHARE, shareable.getId());
		if (shared) {
			other.addPermission(storage, sharePermission);
			log.trace("User {} shares query {}. Adding permission {} to group {}.", user, shareable, shareable.getId(), sharePermission, other);
		}
		else {
			other.removePermission(storage, sharePermission);
			log.trace("User {} unshares query {}. Removing permission {} from group {}.", user, shareable, shareable.getId(), sharePermission, other);
		}
		shareable.setShared(shared);
	}
}
