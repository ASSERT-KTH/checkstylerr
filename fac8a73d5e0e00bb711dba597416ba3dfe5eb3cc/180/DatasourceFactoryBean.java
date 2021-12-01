package org.onetwo.common.ds;

import java.util.Properties;

import javax.sql.DataSource;

import org.onetwo.common.spring.config.JFishPropertyPlaceholder;
import org.onetwo.common.spring.utils.BeanPropertiesMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class DatasourceFactoryBean implements FactoryBean<DataSource>, InitializingBean {

//	private final Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	
	private DataSource dataSource;
	
	private Class<? extends DataSource> implementClass;
	private Properties config;
	private String prefix;
	
	@Autowired
	private JFishPropertyPlaceholder configHolder;
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(config==null){
			config = configHolder.getMergedConfig();
		}
//		boolean hasPrefix = StringUtils.isNotBlank(prefix);
		
		dataSource = BeanUtils.instantiate(implementClass);
		
		BeanPropertiesMapper mapper = new BeanPropertiesMapper(config, prefix);
		mapper.mapToObject(dataSource);
	}

	@Override
	public DataSource getObject() throws Exception {
		return dataSource;
	}

	@Override
	public Class<?> getObjectType() {
		return DataSource.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setImplementClass(Class<? extends DataSource> implementClass) {
		this.implementClass = implementClass;
	}

	public void setConfig(Properties config) {
		this.config = config;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

}
