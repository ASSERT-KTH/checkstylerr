package com.sample.WashMachine;

import com.sample.WashMachine.apparatuses.Button;
import com.sample.WashMachine.apparatuses.Heater;
import com.sample.WashMachine.apparatuses.Machine;
import com.sample.WashMachine.apparatuses.Valve;
import com.sample.WashMachine.common.Motor;
import com.sample.WashMachine.common.Sensor;
import com.sample.WashMachine.common.SoilRemoval;
import com.sample.WashMachine.interfaces.MachineMediator;
import com.sample.WashMachine.mediators.CottonMediator;
import com.sample.WashMachine.mediators.DenimMediator;



/*
.........................Setting up for COTTON program.........................
Button pressed.
Valve is opened...
Filling water...
Valve is closed...
Heater is on...
Temperature reached 40 *C
Temperature is set to 40
Heater is off...
Start motor...
Rotating drum at 700 rpm.
Adding detergent
Setting Soil Removal to low
Adding softener
******************************************************************************
.........................Setting up for DENIM program.........................
Button pressed.
Valve is opened...
Filling water...
Valve is closed...
Heater is on...
Temperature reached 30 *C
Temperature is set to 30
Heater is off...
Start motor...
Rotating drum at 1400 rpm.
Adding detergent
Setting Soil Removal to medium
No softener is required
*/
public class MediatorApp {



	public static void main(String[] args) {

		MachineMediator mediator = null;

		Sensor sensor = new Sensor();
		SoilRemoval soilRemoval = new SoilRemoval();
		Motor motor = new Motor();

		Machine machine = new Machine();
		Heater heater = new Heater();
		Valve valve = new Valve();
		Button button = new Button();

		mediator = new CottonMediator(machine, heater, motor, sensor, soilRemoval, valve);

		button.setMediator(mediator);
		machine.setMediator(mediator);
		heater.setMediator(mediator);
		valve.setMediator(mediator);

		button.press();

		System.out.println("******************************************************************************");

		mediator = new DenimMediator(machine, heater, motor, sensor, soilRemoval, valve);

		button.setMediator(mediator);
		machine.setMediator(mediator);
		heater.setMediator(mediator);
		valve.setMediator(mediator);

		button.press();
	}

}
