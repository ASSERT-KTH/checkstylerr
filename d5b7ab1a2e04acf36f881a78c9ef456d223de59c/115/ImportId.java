package com.bakdata.conquery.models.identifiable.ids.specific;

import java.util.List;

import com.bakdata.conquery.models.datasets.Import;
import com.bakdata.conquery.models.identifiable.ids.AId;
import com.bakdata.conquery.models.identifiable.ids.IId;
import com.bakdata.conquery.models.identifiable.ids.IdIterator;
import com.bakdata.conquery.models.identifiable.ids.NamespacedId;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor @Getter @EqualsAndHashCode(callSuper=false)
public class ImportId extends AId<Import> implements NamespacedId {

	private final TableId table;
	private final String tag;
	
	@Override
	public DatasetId getDataset() {
		return table.getDataset();
	}
	
	@Override
	public void collectComponents(List<Object> components) {
		table.collectComponents(components);
		components.add(tag);
	}
	
	public static enum Parser implements IId.Parser<ImportId> {
		INSTANCE;
		
		@Override
		public ImportId parseInternally(IdIterator parts) {
			String tag = parts.next();
			TableId parent = TableId.Parser.INSTANCE.parse(parts);
			return new ImportId(parent, tag);
		}
	}
}