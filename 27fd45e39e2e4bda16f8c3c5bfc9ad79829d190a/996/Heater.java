package com.sample.WashMachine.apparatuses;

import com.sample.WashMachine.interfaces.MachineMediator;
import com.sample.WashMachine.interfaces.Operator;

public class Heater implements Operator {

	private MachineMediator mediator;
	
	@Override
	public void setMediator(MachineMediator mediator){
		this.mediator = mediator;
	}
	
	public void on(int temp){
		System.out.println("Heater is on...");
		if(mediator.checkTemperature(temp)){
			System.out.println("Temperature is set to "+temp);
			mediator.off();
		}
	}
	
	public void off(){
		System.out.println("Heater is off...");
		mediator.wash();
	}
}
