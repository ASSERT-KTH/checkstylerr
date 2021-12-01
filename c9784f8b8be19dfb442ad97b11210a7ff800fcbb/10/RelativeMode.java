package com.bakdata.conquery.apiv1.forms.export_form;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.bakdata.conquery.apiv1.forms.DateContextMode;
import com.bakdata.conquery.apiv1.forms.IndexPlacement;
import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.models.forms.export.RelExportGenerator;
import com.bakdata.conquery.models.forms.managed.RelativeFormQuery;
import com.bakdata.conquery.models.identifiable.ids.specific.DatasetId;
import com.bakdata.conquery.models.identifiable.ids.specific.UserId;
import com.bakdata.conquery.models.query.QueryResolveContext;
import com.bakdata.conquery.models.query.Visitable;
import com.bakdata.conquery.models.query.concept.CQElement;
import com.bakdata.conquery.models.query.concept.specific.temporal.TemporalSampler;
import com.bakdata.conquery.models.worker.DatasetRegistry;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

@Getter @Setter
@CPSType(id="RELATIVE", base=Mode.class)
public class RelativeMode extends Mode {
	@NotNull
	private DateContextMode timeUnit;
	@Min(0)
	private int timeCountBefore;
	@Min(0)
	private int timeCountAfter;
	@NotNull
	private IndexPlacement indexPlacement;
	@NotNull
	private TemporalSampler indexSelector;
	@NotEmpty
	private List<CQElement> features = Collections.emptyList();
	@NotEmpty
	private List<CQElement> outcomes = Collections.emptyList();

	@Override
	public void visit(Consumer<Visitable> visitor) {
		features.forEach(e -> visitor.accept(e));
		outcomes.forEach(e -> visitor.accept(e));
	}
	
	@Override
	public RelativeFormQuery createSpecializedQuery(DatasetRegistry datasets, UserId userId, DatasetId submittedDataset) {
		return RelExportGenerator.generate(datasets, this, userId, submittedDataset);
	}

	@Override
	public void resolve(QueryResolveContext context) {
		// Resolve all
		features.replaceAll(e -> e.resolve(context));
		outcomes.replaceAll(e -> e.resolve(context));
	}
}
