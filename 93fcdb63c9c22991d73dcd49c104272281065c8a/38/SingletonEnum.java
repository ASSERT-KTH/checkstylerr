
// we use an enum instead of class for creating the Singleton 
enum MyEnum {

    // we can create only one Enum for a singleton class 
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
