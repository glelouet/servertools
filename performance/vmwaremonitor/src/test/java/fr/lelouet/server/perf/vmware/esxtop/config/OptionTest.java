package fr.lelouet.server.perf.vmware.esxtop.config;

import fr.lelouet.server.perf.vmware.esxtop.config.flags.Cpu;
import fr.lelouet.server.perf.vmware.esxtop.config.flags.DiskVM;
import fr.lelouet.server.perf.vmware.esxtop.config.flags.Interrupt;

import org.testng.Assert;

import org.testng.annotations.Test;

public class OptionTest {
	@Test
	public void testCPUConfig() {
		Assert.assertEquals(Option.CPU.formatLine(Cpu.ID), "Abcdefghij");
		Assert.assertEquals(Option.CPU.formatLine(Cpu.ID, Cpu.ALLOC,
				Cpu.PCSTATETIMES), "AHFbcdegij");
	}

	@Test
	public void testDiskadapterConfig() {
		Assert.assertEquals(Option.DISKADAPTER.formatLine(Interrupt.DEVICES,
				Interrupt.VECTOR, DiskVM.IOSTATS), "abcdefghijkl");
	}
}
