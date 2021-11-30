package com.javacodegeeks.patterns.proxypattern.remoteproxy;

import java.rmi.Naming;


/*
The report application for the owner of the pizza company will use this service 
in order to generate and check the report. We need to provide the interface 
ReportGenerator and the stub to the clients which will use the service. You can 
simply hand-deliver the stub and any other classes or interfaces required in the 
service.
*/
public class ReportGeneratorClient {


	public void generateReport(){

		try {

			ReportGenerator reportGenerator = (ReportGenerator)Naming.lookup("rmi://127.0.0.1/PizzaCoRemoteGenerator");
			System.out.println(reportGenerator.generateDailyReport());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		new ReportGeneratorClient().generateReport();
	}
}
