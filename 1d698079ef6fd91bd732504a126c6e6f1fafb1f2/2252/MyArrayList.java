package cn.bjsxt.myCollection;

import java.util.ArrayList;

/**
 * 模拟实现JDK中提供的ArrayList类
 * @author dell
 *
 */
public class MyArrayList {
	/**
     * The value is used for object storage.
     */
    private Object[] value;

    /**
     * The size is the number of objects used.
     */
    private int size;
    
    public MyArrayList(){
    //	value = new Object[16];
    	this(10);
    }
    public MyArrayList(int size){
    	if(size<0){
    		try {
				throw new Exception();  //手动抛出一个异常。 讲到异常章节再说，先混个眼熟
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	value = new Object[size];
    }
    
    public int size(){
    	return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public void add(Object obj){
    	value[size] = obj;
    	size++;
    	if(size>=value.length){
    		//装不下了。扩容吧！
    		int newCapacity = value.length*2;
    		Object[] newList = new Object[newCapacity];
//    		System.arraycopy(src, srcPos, dest, destPos, length);
    		
    		for(int i=0;i<value.length;i++){
    			newList[i] = value[i];
    		}
    		
    		value = newList;
    	}
    }
    
    public Object get(int index){
    	rangeCheck(index);
    	
    	return value[index];
    }
    
    public int indexOf(Object obj){
    	if(obj==null){
    		return -1;
    	}else{
    		for(int i=0;i<value.length;i++){
    			if(obj==value[i]){
    				return i;
    			}
    		}
    		return -1;
    	}
    }
    
    public int lastIndexOf(Object obj){
    	if(obj==null){
    		return -1;
    	}else{
    		for(int i=value.length-1;i>=0;i--){
    			if(obj==value[i]){
    				return i;
    			}
    		}
    		return -1;
    	}
    }
    
    public Object set(int index, Object object) {
    	rangeCheck(index);
    	Object old = value[index];
        value[index] = object;
        return old;
    }
    
    public void rangeCheck(int index){
    	if(index<0||index>size-1){    //[0,size-1]
    		try {
				throw new Exception();  //手动抛出一个异常。 讲到异常章节再说，先混个眼熟
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }
    
    
    public static void main(String[] args) {
		MyArrayList  list = new MyArrayList(2);
		list.add("aaa");
		list.add(new Human("高琪"));
		list.add("bbbb");
		list.add("bbbb");
		list.add("bbbb");
		list.add("bbbb");
		ArrayList list2;
		
		Human h = (Human) list.get(1);
		System.out.println(h.getName());
		
//		System.out.println(list.get(3)); 
		System.out.println(list.size());
		
	}
    
}
