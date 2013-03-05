package fr.lelouet.server.perf.snapshot.storage;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.server.perf.snapshot.HVSnapshotTest;

public class StringConverterTest {

	/**
	 * test if the conversion of a snapshot to a String[] is then converted back
	 * to the same snapshot
	 */
	@Test(dataProvider = "createSnapshots")
	public void testEqualInvert(HVSnapshot snap) {
		List<String> encoded = StringConverter.convertSnapshot(snap);
		encoded.add(null);
		List<HVSnapshot> decoded = new StringConverter()
				.convertStrings(encoded);
		org.testng.Assert.assertEquals(decoded.size(), 1, encoded.toString());
		Assert.assertTrue(decoded.get(0).equals(snap), "got " + decoded.get(0)
				+ " while excpecting " + snap);
	}

	@DataProvider(name = "createSnapshots")
	public Object[][] createSnapshots() {
		return new Object[][]{{HVSnapshotTest.simpleSnapshot()},
				{HVSnapshotTest.snapshotWithVMs()}};

	}

	@Test(dependsOnMethods = "testEqualInvert")
	public void testSeveralCodings() {
		HVSnapshot snap = HVSnapshotTest.simpleSnapshot();
		HVSnapshot snap2 = HVSnapshotTest.simpleSnapshot();
		snap2.setDate(snap.getDate() + 100);
		HVSnapshot snap3 = HVSnapshotTest.simpleSnapshot();
		snap3.setDuration(snap.getDuration() + 10);
		List<String> parsed = StringConverter.convertSnapshot(snap);
		parsed.addAll(StringConverter.convertSnapshot(snap2));
		parsed.addAll(StringConverter.convertSnapshot(snap3));
		parsed.add(null);
		List<HVSnapshot> decoded = new StringConverter().convertStrings(parsed);
		Assert.assertEquals(decoded.size(), 3, parsed.toString());
		Assert.assertTrue(decoded.get(0).equals(snap));
		Assert.assertTrue(decoded.get(1).equals(snap2));
		Assert.assertTrue(decoded.get(2).equals(snap3));
	}
}
