package com.bakdata.conquery.models.identifiable.ids.specific;

import java.util.List;

import com.bakdata.conquery.models.events.Bucket;
import com.bakdata.conquery.models.identifiable.ids.AId;
import com.bakdata.conquery.models.identifiable.ids.IId;
import com.bakdata.conquery.models.identifiable.ids.IdIterator;
import com.bakdata.conquery.models.identifiable.ids.NamespacedId;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter @AllArgsConstructor @EqualsAndHashCode(callSuper=false)
public class BucketId extends AId<Bucket> implements NamespacedId {

	private final ImportId imp;
	private final int bucket;
	
	@Override
	public DatasetId getDataset() {
		return imp.getDataset();
	}
	
	@Override
	public void collectComponents(List<Object> components) {
		imp.collectComponents(components);
		components.add(bucket);
	}
	
	public static enum Parser implements IId.Parser<BucketId> {
		INSTANCE;
		
		@Override
		public BucketId parseInternally(IdIterator parts) {
			int bucket = Integer.parseInt(parts.next());
			ImportId parent = ImportId.Parser.INSTANCE.parse(parts);
			return new BucketId(parent, bucket);
		}
	}
}