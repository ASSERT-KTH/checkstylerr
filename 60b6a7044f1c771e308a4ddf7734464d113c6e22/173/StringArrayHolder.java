package ca.queensu.cs.aggregate;


import org.apache.drill.common.types.TypeProtos.MajorType;
import org.apache.drill.common.types.TypeProtos.MinorType;
import org.apache.drill.common.types.Types;
import org.apache.drill.exec.expr.holders.ValueHolder;






public final class StringArrayHolder implements ValueHolder{
  
  public static final MajorType TYPE = Types.optional(MinorType.GENERIC_OBJECT);
  
  public MajorType getType() {return TYPE;}
  
    public static final int WIDTH = 8;
    
    public int isSet;
    public String[] value;
    
    
    @Deprecated
    public int hashCode(){
      throw new UnsupportedOperationException();
    }

    
    
    
    
}


