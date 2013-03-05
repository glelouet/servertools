package fr.lelouet.server.perf.snapshot;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;

public class HVSnapshotTest {

	public static HVSnapshot simpleSnapshot() {
		HVSnapshot ret = new HVSnapshot();
		ret.setDate(100L);
		ret.setDuration(1L);
		ret.setVMMaxAge(0);
		ret.put("act1", 50.0);
		ret.put("act2", -5.0);
		return ret;
	}

	public static HVSnapshot snapshotWithVMs() {
		HVSnapshot ret = simpleSnapshot();
		ActivityReport vm1 = new ActivityReport();
		ret.updateVM(vm1, "vm1");
		ActivityReport vm2 = new ActivityReport();
		ret.updateVM(vm2, "vm2");
		for (String name : ret.getVMsNames()) {
			ActivityReport rep = ret.getSnapshot(name);
			rep.setDate(ret.getDate());
			rep.setDuration(ret.getDuration());
			rep.put("vmact1", 0.0);
			rep.put("vmact2", 10.0);
		}
		return ret;
	}

	@Test
	public void testSimpleEquality() {
		HVSnapshot snap1 = simpleSnapshot(), snap2 = simpleSnapshot();
		Assert.assertTrue(snap1.equals(snap1));
		Assert.assertTrue(snap1.equals(snap2));
		Assert.assertTrue(snap2.equals(snap1));
		Assert.assertTrue(snap2.equals(snap2));
		snap2.put("act1", 55.0);
		Assert.assertFalse(snap1.equals(snap2));
		Assert.assertFalse(snap2.equals(snap1));
	}

	@Test(dependsOnMethods = "testSimpleEquality")
	public void testDeepEquality() {
		HVSnapshot snap1 = snapshotWithVMs(), snap2 = snapshotWithVMs();
		Assert.assertTrue(snap1.equals(snap1));
		Assert.assertTrue(snap1.equals(snap2));
		Assert.assertTrue(snap2.equals(snap1));
		Assert.assertTrue(snap2.equals(snap2));
	}
}
