package fr.lelouet.server.perf.snapshot.storage;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.server.perf.snapshot.HVSnapshotTest;

public class FileStorageTest {

	@Test
	public void simpleWriteAndRead() throws IOException {
		HVSnapshot snap = HVSnapshotTest.simpleSnapshot();
		FileStorage store = new FileStorage();
		File toWrite = File.createTempFile("hvsnapshot", null);
		toWrite.deleteOnExit();
		Assert.assertTrue(store.setFile(toWrite));
		Assert.assertTrue(store.add(snap));
		store.close();
		List<HVSnapshot> loaded = FileStorage.loadFromFile(toWrite);
		Assert.assertEquals(loaded.size(), 1);
		Assert.assertTrue(loaded.get(0).equals(snap));
	}

	@Test(dependsOnMethods = "simpleWriteAndRead")
	public void simpleTwoWriteAndRead() throws IOException {
		HVSnapshot snap1 = HVSnapshotTest.simpleSnapshot();
		HVSnapshot snap2 = HVSnapshotTest.snapshotWithVMs();
		FileStorage store = new FileStorage();
		File toWrite = File.createTempFile("hvsnapshot", null);
		System.err.println(toWrite.getAbsolutePath());
		// toWrite.deleteOnExit();
		Assert.assertTrue(store.setFile(toWrite));
		Assert.assertTrue(store.add(snap1));
		Assert.assertTrue(store.add(snap2));
		store.close();
		List<HVSnapshot> loaded = FileStorage.loadFromFile(toWrite);
		Assert.assertEquals(loaded.size(), 2);
		Assert.assertTrue(loaded.get(0).equals(snap1));
		Assert.assertTrue(loaded.get(1).equals(snap2));
	}

}
