
/*
 As of JDK 1.5, you can create a singleton class using enums. The Enum constants 
 are implicitly static and final and you cannot change their values once created.

You will get a compile time error when you attempt to explicitly instantiate an 
Enum object. As Enum gets loaded statically, it is thread-safe. The clone method 
in Enum is final which ensures that enum constants never get cloned. Enum is 
inherently serializable, the serialization mechanism ensures that duplicate instances 
are never created as a result of deserialization. Instantiation using reflection 
is also prohibited. These things ensure that no instance of an enum exists beyond 
the one defined by the enum constants.

by default enums have implicit private constructor and that explicitly adding a 
private constructor is not needed unless you actually have code that you need to 
run in that constructor 

An enum type is a special type of class type.

Your enum declaration actually compiles to something like

	public final class MySingleton {
	    public final static MySingleton INSTANCE = new MySingleton();
	    private MySingleton(){} 
	}

When your code first accesses INSTANCE, the class MySingleton will be loaded and 
initialized by the JVM. This process initializes the static field above once (lazily).
*/

// we use an enum instead of class for creating the Singleton 
enum MyEnum {


    INSTANCE;

    int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}





public class SingletonEnum {


    public static void main(String[] args) {


    	// this is the one and only Singleton instance 
        MyEnum singleton = MyEnum.INSTANCE;

        System.out.println(singleton.getValue());
        singleton.setValue(2);
        System.out.println(singleton.getValue());
    }
}
