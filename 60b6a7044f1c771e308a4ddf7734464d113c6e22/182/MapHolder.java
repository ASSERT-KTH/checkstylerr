package ca.queensu.cs.aggregate;

import java.io.Serializable;

import org.apache.drill.common.types.Types;
import org.apache.drill.common.types.TypeProtos.MajorType;
import org.apache.drill.common.types.TypeProtos.MinorType;
import org.apache.drill.exec.expr.holders.ValueHolder;

public class MapHolder<T,S> implements ValueHolder, Serializable{
	  
		 /** for serialization */
		static final long serialVersionUID = 1L;
		  
	  public static final MajorType TYPE = Types.optional(MinorType.GENERIC_OBJECT);
	  
	  public MajorType getType() {return TYPE;}
	  
	    public static final int WIDTH = 8;
	    
	    public int isSet;
	    public java.util.HashMap<T,S> map;

	    
	    
	    @Deprecated
	    public int hashCode(){
	      throw new UnsupportedOperationException();
	    }

	    /*
	     * Reason for deprecation is that ValueHolders are potential scalar replacements
	     * and hence we don't want any methods to be invoked on them.
	     */

	    public String toString(){
	    	return super.toString();
	    }

}
