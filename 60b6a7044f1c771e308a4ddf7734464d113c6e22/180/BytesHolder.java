package ca.queensu.cs.aggregate;


import org.apache.drill.common.types.TypeProtos.MajorType;
import org.apache.drill.common.types.TypeProtos.MinorType;
import org.apache.drill.common.types.Types;
import org.apache.drill.exec.expr.holders.ValueHolder;


public final class BytesHolder implements ValueHolder{
  
  public static final MajorType TYPE = Types.optional(MinorType.GENERIC_OBJECT);
  
  public MajorType getType() {return TYPE;}
  
    public static final int WIDTH = 8;
    
    public int isSet;
    public Byte[] value;
    
    public byte[] getByteArray(){
    	byte[] bytes = new byte[value.length];
    	for(int i=0; i<value.length;i++){
    		bytes[i]=value[i].byteValue();
    	}
    	return bytes;
    }
    
    public void setByteArray(byte[] bytes){
    	value = new Byte[bytes.length];
    	for(int i=0; i<bytes.length;i++){
    		value[i]=bytes[i];
    	}
    }
    
    @Deprecated
    public int hashCode(){
      throw new UnsupportedOperationException();
    }

    /*
     * Reason for deprecation is that ValueHolders are potential scalar replacements
     * and hence we don't want any methods to be invoked on them.
     */
    @Deprecated
    public String toString(){
      throw new UnsupportedOperationException();
    }
    
    
    
    
}


