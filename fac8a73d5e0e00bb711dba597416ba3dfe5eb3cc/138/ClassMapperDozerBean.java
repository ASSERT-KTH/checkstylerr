package org.onetwo.common.spring.dozer;

import java.util.Collection;
import java.util.List;

import org.onetwo.common.utils.LangUtils;

public class ClassMapperDozerBean extends DozerBean {

	private PropertyUnderlineMappingBuilder builder;
	

	public ClassMapperDozerBean(Collection<DozerClassMapper> dozerClasses) {
		this(dozerClasses, null);
	}
	public ClassMapperDozerBean(Collection<DozerClassMapper> dozerClasses, List<String> mappingFiles) {
		super(mappingFiles);
		if(LangUtils.isNotEmpty(dozerClasses)){
			this.builder = new PropertyUnderlineMappingBuilder(dozerClasses);
		}
	}

	@Override
	public void initDozer() {
		if(builder!=null){
			getDozer().addMapping(builder);
		}
	}

}
