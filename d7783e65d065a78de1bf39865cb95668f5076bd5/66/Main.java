package Multithreading;

/**
 * Created by Chaklader on 1/14/17.
 */

//  Excellent Tutorials about Java Concurrency 
//  <http://tutorials.jenkov.com/java-concurrency/java-memory-model.html>

 
public class Main {


    public static void main(String[] arStrings){

        System.out.println("Start multi-threading");
        MultithreadSys multithreadSys = new MultithreadSys();
        multithreadSys.testAllMethods();
    }
}
