package ca.queensu.cs.aggregate;



import java.io.Serializable;

import org.apache.drill.common.types.TypeProtos.MajorType;
import org.apache.drill.common.types.TypeProtos.MinorType;
import org.apache.drill.common.types.Types;
import org.apache.drill.exec.expr.holders.ValueHolder;






public final class LinkedListHolder <T> implements ValueHolder, Serializable{
  
	 /** for serialization */
	static final long serialVersionUID = 1L;
	  
  public static final MajorType TYPE = Types.optional(MinorType.GENERIC_OBJECT);
  
  public MajorType getType() {return TYPE;}
  
    public static final int WIDTH = 8;
    
    public int isSet;
    public java.util.LinkedList<T> list;
    public String algorithm;
    public String[] options;
    
    
    @Deprecated
    public int hashCode(){
      throw new UnsupportedOperationException();
    }

    /*
     * Reason for deprecation is that ValueHolders are potential scalar replacements
     * and hence we don't want any methods to be invoked on them.
     */

    public String toString(){
    	long timeBefore = System.currentTimeMillis();
    	java.lang.StringBuilder st = new java.lang.StringBuilder();
		for(int i=0;i<list.size();i++){
			st.append(list.get(i));
		}
		long timeAfter = System.currentTimeMillis();
		System.out.println("ToString time:"+(timeAfter-timeBefore));
      return st.toString();
    }
    
    
    
    
}


