package org.onetwo.common.utils.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.utils.Assert;

import com.google.common.collect.Maps;

@SuppressWarnings("serial")
public class MapList<E> extends ArrayList<E> {
	
	public static <R> MapList<R> wrap(List<R> list){
		MapList<R> maplist = new MapList<>();
		list.stream().forEach(e->maplist.add(e));
		return maplist;
	}

	
	public static <R> MapList<R> wrap(List<R> list, String idName){
		MapList<R> maplist = new MapList<>(idName);
		list.stream().forEach(e->maplist.add(e));
		return maplist;
	}
	/**
	 * 
	 */
	private Map<Object, E> dataMap = Maps.newHashMap();
	private String idName = "id";
	
	public MapList() {
		super();
	}

	public MapList(String idName) {
		super();
		this.idName = idName;
	}

	
	public MapList<E> addMapList(MapList<E> list){
		list.forEach(e->add(e));
		return this;
	}
	
	@Override
	public boolean add(E e) {
		if(ReflectUtils.hasProperty(e, idName)){
			Object id = ReflectUtils.getPropertyValue(e, idName);
			dataMap.put(id, e);
		}
		return super.add(e);
	}
	
	public E findById(Object id){
		return dataMap.get(id);
	}
	
	public List<E> findList(String propName, Object value){
		Assert.notNull(value);
		return this.stream().filter(e->{
			Object fieldValue = ReflectUtils.getPropertyValue(e, propName);
			return value.equals(fieldValue);
		})
		.collect(Collectors.toList());
	}
	
	public E findOne(String propName, Object value){
		Assert.notNull(value);
		return this.stream().filter(e->{
			Object fieldValue = ReflectUtils.getPropertyValue(e, propName);
			return value.equals(fieldValue);
		})
		.findFirst()
		.orElse(null);
	}
	
	public UsingData using(Object id){
		E e = dataMap.get(id);
		if(e==null)
			throw new RuntimeException("not found: "+id);
		return new UsingData(e);
	}
	class UsingData {
		private E element;

		public UsingData(E element) {
			super();
			this.element = element;
		}
		
		protected E cloneElement(){
			E cloneObj;
			try {
				byte[] data = SerializationUtils.serialize((Serializable)element);
				cloneObj = SerializationUtils.deserialize(data);
			} catch (Exception e) {
				throw new RuntimeException("serializeClone error: " + e.getMessage(), e);
			} 
			return cloneObj;
		}
		
		public MapList<E> generateDatas(int count, BiConsumer<E, Integer> newObjConsumer){
			List<E> datas = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				E newObj = cloneElement();
				newObjConsumer.accept(newObj, i);
				datas.add(newObj);
			}
			MapList.this.addAll(datas);
			return MapList.this;
		}
		
	}

}
