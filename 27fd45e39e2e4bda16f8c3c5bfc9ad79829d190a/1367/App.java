package com.car;


/*
Sec Security System is a security and electronic company which produces and assembles 
products for cars. It delivers any car electronic or security system you want, from 
air bags to GPS tracking system, reverse parking system etc. Big car companies use 
its products in their cars. The company uses a well defined object oriented approach 
to keep track of their products using software which is developed and maintained by 
them only. They get the car, produce the system for it and assemble it into the car.

Recently, they got new orders from BigWheel (a car company) to produce central locking 
and gear lock system for their new xz model. To maintain this, they are creating a new 
software system. They started by creating a new abstract class CarProductSecurity, in 
which they kept some car specific methods and some of the features which they thought 
are common to all security products. Then they extended the class and created two 
different sub classes named them BigWheelXZCentralLocking, and BigWheelXZGearLocking.


After a while, another car company Motoren asked them to produce a new system of central 
locking and gear lock for their lm model. Since, the same security system cannot be used 
in both models of different cars, the Sec Security System has produced the new system for 
them, and also has created to new classes MotorenLMCentralLocking, and MotorenLMGearLocking 
which also extend the CarProductSecurity class.



Use of Bridge Pattern
---------------------

You want to avoid a permanent binding between an abstraction and its implementation. This might 
be the case, for example, when the implementation must be selected or switched at run-time.

Both the abstractions and their implementations should be extensible by sub-classing. In this 
case, the Bridge pattern lets you combine the different abstractions and implementations and 
extend them independently.

Changes in the implementation of an abstraction should have no impact on clients; that is, their 
code should not have to be recompiled.

You want to share an implementation among multiple objects (perhaps using reference counting), 
and this fact should be hidden from the client.
*/
public class App{



   public static void main( String[] args ){

   	    System.out.println();
        System.out.println( "Hello! Bridge Design Pattern" );
        System.out.println();

        Product product = new CentralLocking("Central Locking System");
		Product product2 = new GearLocking("Gear Locking System");

		Car car = new BigWheel(product , "BigWheel xz model");

		car.produceProduct();
		car.assemble();
		car.printDetails();

		System.out.println();

		car = new BigWheel(product2 , "BigWheel xz model");
		car.produceProduct();
		car.assemble();
		car.printDetails();

		System.out.println("-----------------------------------------------------");

		car = new Motoren(product, "Motoren lm model");
		car.produceProduct();
		car.assemble();
		car.printDetails();

		System.out.println();

		car = new Motoren(product2, "Motoren lm model");
		car.produceProduct();
		car.assemble();
		car.printDetails();

		System.out.println();
   }
}
