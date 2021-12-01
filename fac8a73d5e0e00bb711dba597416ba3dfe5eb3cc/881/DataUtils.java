package org.onetwo.plugins.admin.utils;

import java.io.FileInputStream;
import java.util.List;

import org.onetwo.common.exception.ServiceException;
import org.onetwo.common.spring.Springs;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.plugins.admin.service.impl.DictionaryCachingServiceImpl;
import org.onetwo.plugins.admin.vo.DictInfo;
import org.onetwo.plugins.admin.vo.DictionaryList;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

final public class DataUtils {
	
	public static DictionaryCachingServiceImpl getDictionaryCachingService(){
		return Springs.getInstance().getBean(DictionaryCachingServiceImpl.class, true);
	}

	public static DictionaryList readDictResource(Resource config){
		DictionaryList m = null;
		try {
			XStream xstream = registerDictModel();
			if(config.exists()){
				m = (DictionaryList)xstream.fromXML(new FileInputStream(config.getFile()));
			}else{
				m = (DictionaryList)xstream.fromXML(config.getInputStream());
			}
		} catch (Exception e) {
			throw new ServiceException("读取模板["+config+"]配置出错：" + e.getMessage(), e);
		}
		
		return m;
	}
	
	public static XStream registerDictModel(){
		XStream xstream = new XStream(new DomDriver());
		xstream.alias("dictionary", DictionaryList.class);
//		xstream.alias("dictType", DictTypeInfo.class);
		xstream.alias("dicts", List.class);
		xstream.alias("dict", DictInfo.class);
		xstream.useAttributeFor(String.class);
		for(Class<?> btype : LangUtils.getBaseTypeClass()){
			xstream.useAttributeFor(btype);
		}
		return xstream;
	}
	
	private DataUtils(){}
}
