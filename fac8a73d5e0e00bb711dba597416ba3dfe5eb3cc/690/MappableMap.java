package org.onetwo.common.utils.map;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.onetwo.common.reflect.ReflectUtils;

/***
 * 把对象映射为map
 * @author way
 *
 */
@SuppressWarnings("serial")
public class MappableMap extends HashMap<String, Object>{
	
	public static <E> StaticMappingBuilder<E> newMappingBuilder(){
		return new StaticMappingBuilder<E>();
	}
	
//	final private Object sourceObject;
//	final private List<MappingInfo> mappingInfos = new ArrayList<>();

	public MappableMap() {
	    super();
    }
	
	public MappableMap from(Object sourceObject){
	    PropertyDescriptor[] props = ReflectUtils.desribProperties(sourceObject.getClass());
	    Stream.of(props).forEach(p->{
	    	Object value = ReflectUtils.getProperty(sourceObject, p);
	    	if(value==null)
	    		return ;
	    	super.put(p.getName(), value);
	    });
	    return this;
	}
	
	/***
	 * 
	 * @author way
	 *
	 * @param <T> sourceObject
	 */
	public static class StaticMappingBuilder<T> {
		final private List<MappingInfo<T>> mappingInfos = new ArrayList<>();
		private boolean mapAllFields = true;
//		final private List<T> sourceObjects;
		public StaticMappingBuilder() {
//	        this.sourceObjects = sourceObjects;//List<T> sourceObjects
        }

		public StaticMappingBuilder<T> mapAllFields(){
			this.mapAllFields = true;
			return this;
		}
		public StaticMappingBuilder<T> specifyMappedFields(){
			this.mapAllFields = false;
			return this;
		}
		public StaticMappingBuilder<T> addMapping(String jsonFieldName, String objectFieldName){
			this.mappingInfos.add(new MappingInfo<T>(jsonFieldName, objectFieldName));
			return this;
		}
		
		public StaticMappingBuilder<T> addMapping(String jsonFieldName, MappingValueFunc<T, ?> valueFunc){
			this.mappingInfos.add(new MappingInfo<T>(jsonFieldName, valueFunc));
			return this;
		}
		
		
		public List<MappableMap> bindValues(List<T> sourceObjects){
		    return sourceObjects.stream().map(obj->bindMappings(new MappableMap(), obj))
										    .collect(Collectors.toList());
		}
		
		public MappableMap bindValue(T sourceObject){
			return bindMappings(new MappableMap(), sourceObject);
		}
		
		private MappableMap bindMappings(MappableMap mappingObject, T sourceObject){
			if(mapAllFields){
				mappingObject.from(sourceObject);
			}
		    mappingInfos.stream().forEach(maping->{
		    	if(maping.isMappingValueFunc()){
//			    	mappingObject.put(maping.jsonFieldName, maping.addMappingValueFunc.mapping(sourceObject, maping));
			    	mappingObject.put(maping.jsonFieldName, maping.addMappingValueFunc.mapping(sourceObject));
		    	}else{
			    	Object value = ReflectUtils.getPropertyValue(sourceObject, maping.objectFieldName);
			    	mappingObject.put(maping.jsonFieldName, value);
		    	}
		    });
		    return mappingObject;
		}
		
	}

	public static interface MappingValueFunc<T, R> {
//		R mapping(T sourceObject, MappingInfo<T> mapping);
		R mapping(T sourceObject);
	}
	
	
	/***
	 * 
	 * @author way
	 *
	 * @param <T> sourceObject
	 */
	public static final class MappingInfo<T> {
		final private String jsonFieldName;
		final private String objectFieldName;
		final private MappingValueFunc<T, ?> addMappingValueFunc;
		public MappingInfo(String jsonFieldName, String objectFieldName) {
	        super();
	        this.jsonFieldName = jsonFieldName;
	        this.objectFieldName = objectFieldName;
	        this.addMappingValueFunc = null;
        }
		public MappingInfo(String sourceName, MappingValueFunc<T, ?> addMappingValueFunc) {
	        super();
	        this.jsonFieldName = sourceName;
	        this.objectFieldName = null;
	        this.addMappingValueFunc = addMappingValueFunc;
        }
		
		public boolean isMappingValueFunc(){
			return this.addMappingValueFunc!=null;
		}
		public String getJsonFieldName() {
			return jsonFieldName;
		}
		public String getObjectFieldName() {
			return objectFieldName;
		}
		public MappingValueFunc<T, ?> getAddMappingValueFunc() {
			return addMappingValueFunc;
		}
		
	}
	

}
