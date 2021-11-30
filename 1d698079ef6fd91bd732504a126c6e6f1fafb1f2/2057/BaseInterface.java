package com.hlj.service.inter;

public interface BaseInterface {
	
	//将一些通用的方法 放到基础接口中
		
	//通过id号 获取 用户对象，这里不用Integer 使用Serializable，将对象序列化
	//我们所有的包装类都实现了这个接口
	public Object findById(Class clazz,java.io.Serializable id);
	

}
