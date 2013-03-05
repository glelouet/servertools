package fr.lelouet.consumption.basic;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.testng.annotations.Test;

public class BasicConsumptionListTest {

	public BasicConsumptionList simpleList() {
		BasicConsumptionList list = new BasicConsumptionList();
		list.addData(1, 120);
		list.addData(2, 100);
		list.addData(3, 50);
		return list;
	}

	/** test wether the simple list contains the added data */
	@Test
	public void testSimpleList() {
		BasicConsumptionList list = simpleList();
		Assert.assertEquals(3, list.getEntries().size());
		Assert.assertEquals(120.0, list.getConsumption(1));
		Assert.assertEquals(100.0, list.getConsumption(2));
		Assert.assertEquals(50.0, list.getConsumption(3));
	}

	@Test(dependsOnMethods = "testSimpleList")
	public void testBadInsertion() {
		BasicConsumptionList list = new BasicConsumptionList();
		Assert.assertTrue(list.addData(1, 1.0));
		Assert.assertFalse(list.addData(-1, -2.0));
		Assert.assertFalse(list.addData(1, -21));
		Assert.assertEquals(1, list.getEntries().size());
		Assert.assertEquals(1.0, list.getConsumption(1));
	}

	@Test(dependsOnMethods = "testSimpleList")
	public void testExtrapolation() {
		BasicConsumptionList list = simpleList();
		Assert.assertEquals(120.0, list.getConsumption(-100));
		Assert.assertEquals(50.0, list.getConsumption(100));
		list.addData(5, 70);
		Assert.assertEquals(60.0, list.getConsumption(4));

	}

	@Test(dependsOnMethods = "testSimpleList")
	public void testWriteAndRead() throws IOException {
		BasicConsumptionList list = simpleList();
		File tmpFile = File.createTempFile("serverConsumption", null);
		tmpFile.deleteOnExit();
		list.setWriter(new FileWriter(tmpFile));
		list.commit();
		BasicConsumptionList read = new BasicConsumptionList();
		read.load(new FileReader(tmpFile));

		for (Entry<Long, Double> entry : list.getEntries()) {
			Assert.assertEquals(entry.getValue(), read.getConsumption(entry
					.getKey()));
		}
	}

	@Test(dependsOnMethods = "testSimpleList")
	public void testAggregation() {
		BasicConsumptionList list = simpleList();
		BasicConsumptionList aggreg = new BasicConsumptionList(list, 50000);
		Entry<Long, Double> value = aggreg.getEntries().get(0);
		Assert.assertEquals((Long) 2L, value.getKey());
		Assert.assertEquals(90.0, value.getValue());
	}

}
