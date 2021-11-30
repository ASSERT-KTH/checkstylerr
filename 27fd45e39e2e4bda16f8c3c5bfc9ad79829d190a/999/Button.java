package com.sample.WashMachine.apparatuses;

import com.sample.WashMachine.interfaces.MachineMediator;
import com.sample.WashMachine.interfaces.Operator;

public class Button implements Operator {
	
	private MachineMediator mediator;
	
	@Override
    public void setMediator(MachineMediator mediator){
		this.mediator = mediator;
	}
	
	public void press(){
		System.out.println("Button pressed.");
		mediator.start();
	}

}
