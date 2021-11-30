package com.sample.WashMachine.apparatuses;

import com.sample.WashMachine.interfaces.MachineMediator;
import com.sample.WashMachine.interfaces.Operator;

public class Machine implements Operator {

	private MachineMediator mediator;
	
	@Override
	public void setMediator(MachineMediator mediator){
		this.mediator = mediator;
	}
	
	public void start(){
		mediator.open();
	}
	
	public void wash(){
		mediator.wash();
	}
}
