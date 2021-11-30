package com.balazsholczer.udemy;

import java.util.concurrent.RecursiveAction;


public class SimpleRecursiveAct extends RecursiveTask<Integer> {


	private int simulatedWork;
	
	public SimpleRecursiveAction(int simulatedWork) {
		this.simulatedWork = simulatedWork;
	}
	
	@Override
	protected Integer compute() {
		
		if( simulatedWork > 100 ) {
			
			System.out.println("Parallel execution and split the tasks..." + simulatedWork);
			
			SimpleRecursiveAction simpleRecursiveAction1 = new SimpleRecursiveAction(simulatedWork/2);
			SimpleRecursiveAction simpleRecursiveAction2 = new SimpleRecursiveAction(simulatedWork/2);
			
			simpleRecursiveAction1.fork();
			simpleRecursiveAction2.fork();
			
			int solution = 0;	

			solution += simpleRecursiveAction1.join();
			solution += solution + simpleRecursiveAction2.join();
			
			return solution;			
		} else {
			System.out.println("No need for parallel execution, sequential is OK for this task..." );
			return 2 * simulatedWork;
		}
	}
}
