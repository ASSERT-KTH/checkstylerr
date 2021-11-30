package com.sample.WashMachine.mediators;

import com.sample.WashMachine.apparatuses.Heater;
import com.sample.WashMachine.apparatuses.Machine;
import com.sample.WashMachine.apparatuses.Valve;
import com.sample.WashMachine.common.Motor;
import com.sample.WashMachine.common.Sensor;
import com.sample.WashMachine.common.SoilRemoval;
import com.sample.WashMachine.interfaces.MachineMediator;

public class DenimMediator implements MachineMediator {

	private final Machine machine;
	private final Heater heater;
	private final Motor motor;
	private final Sensor sensor;
	private final SoilRemoval soilRemoval;
	private final Valve valve;

	public DenimMediator(Machine machine,Heater heater,Motor motor,Sensor sensor,SoilRemoval soilRemoval,Valve valve){
		this.machine = machine;
		this.heater = heater;
		this.motor = motor;
		this.sensor = sensor;
		this.soilRemoval = soilRemoval;
		this.valve = valve;

		System.out.println(".........................Setting up for DENIM program.........................");
	}

	@Override
	public void start() {
		machine.start();
	}

	@Override
	public void wash() {
		motor.startMotor();
		motor.rotateDrum(1400);
		System.out.println("Adding detergent");
		soilRemoval.medium();
		System.out.println("No softener is required");
	}

	@Override
	public void open() {
		valve.open();
	}

	@Override
	public void closed() {
		valve.closed();
	}

	@Override
	public void on() {
		heater.on(30);
	}

	@Override
	public void off() {
		heater.off();
	}

	@Override
	public boolean checkTemperature(int temp) {
		return sensor.checkTemperature(temp);
	}	
}
