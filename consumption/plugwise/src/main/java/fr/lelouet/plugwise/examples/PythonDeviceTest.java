package fr.lelouet.plugwise.examples;

import fr.lelouet.plugwise.PlugwiseDevice;
import fr.lelouet.plugwise.python.PythonDevice;

public class PythonDeviceTest {

	public static void main(String args[]) throws InterruptedException {

		PlugwiseDevice device = new PythonDevice();
		device.setId("000D6F000072AB5E");
		device.sendOff();
		Thread.sleep(100);
		device.sendOn();
		System.out.println("1s->" + device.getInstantConsumption());
		System.out.println("8s->" + device.getAverageConsumption());
		System.out.println("total->" + device.getTotalConsumption());
	}

}
