package fr.lelouet.fake

import fr.lelouet.plugwise.PlugwiseDevice


/** a fake device used to test data.<br />
 * internal data is set and get as a bean.<br />
 * calls to functions only set to true the flags associated.<br />
 * #cleaned() returns true not to block a loop*/
class FakeDevice implements PlugwiseDevice{

	String id;

	public String shortString() {
		return "fake plugwise with ID $id"
	}

	int MinimumRequestDelay

	double instantConsumption

	double averageConsumption

	double totalConsumption

	boolean on=false
	public void sendOn(){
		on=true
	}

	boolean off=false
	public void sendOff(){
		off=true
	}

	boolean refresh=false
	public void refresh(){
		refresh=true
	}

	boolean cleaned = true
	public boolean cleaned(){
		return cleaned
	}

	boolean dirtyCache=false
	public void dirtyCache(){
		dirtyCache=true
	}

	void writeInFile(File file, long updateperiodMS) {
		while(true){
		}
	}
}
