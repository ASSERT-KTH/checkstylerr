package com.javacodegeeks.patterns.facadepattern;


// after the usage of the Facade pattern 
public class TestFacade {

	public static void main(String[] args) {
		
		ScheduleServer scheduleServer = new ScheduleServer();
		ScheduleServerFacade facadeServer = new ScheduleServerFacade(scheduleServer);


		// execute the complex operations one-by-one 
		scheduleServer.startBooting();
		scheduleServer.readSystemConfigFile();
		scheduleServer.init();
		scheduleServer.initializeContext();
		scheduleServer.initializeListeners();
		scheduleServer.createSystemObjects();
		
		System.out.println("Start working......");
		System.out.println("After work done.........");
		
		scheduleServer.releaseProcesses();
		scheduleServer.destory();
		scheduleServer.destroySystemObjects();
		scheduleServer.destoryListeners();
		scheduleServer.destoryContext();
		scheduleServer.shutdown();
		//


		// use the Facade pattern 
		facadeServer.startServer();
		
		System.out.println("Start working......");
		System.out.println("After work done.........");
		
		facadeServer.stopServer();
	}

}
