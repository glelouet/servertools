package fr.lelouet.server.perf.vmware;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import fr.lelouet.server.perf.ActivityReport;
import fr.lelouet.server.perf.HVSnapshot;
import fr.lelouet.vmware.model.VCenter;
import fr.lelouet.vmware.model.managed.Host;
import fr.lelouet.vmware.model.managed.VirtualMachine;

public class VMWareMonitorTest {

	public static VirtualMachine[] mockedVMs(int number) {
		VirtualMachine[] ret = new VirtualMachine[number];

		for (int i = 0; i < number; i++) {
			VirtualMachine vm = mock(VirtualMachine.class);
			when(vm.getName()).thenReturn("mockedVM" + i);
			ret[i] = vm;
		}

		return ret;
	}

	public static Host mockedHost(VirtualMachine[] vms) {
		VCenter vcenter = mock(VCenter.class);
		when(vcenter.getConnectionTarget()).thenReturn("mockitoVcenter");

		Host ret = mock(Host.class);
		when(ret.getVCenter()).thenReturn(vcenter);
		when(ret.getName()).thenReturn("monitoredHost");
		when(ret.getVirtualMachines()).thenReturn(Arrays.asList(vms));

		return ret;
	}

	/** test wether one snapshot retrieval leads to correct values */
	@Test
	public void testSimpleRetrieval() {
		final int nbVMs = 10;
		final int vmCPUUsage = 500;
		VirtualMachine[] monitoredVMs = mockedVMs(nbVMs);

		for (VirtualMachine vm : monitoredVMs) {
			when(vm.getOverallCpuUsageMHz()).thenReturn(vmCPUUsage);
		}

		final long hostCPUUsage = 1000L;
		Host monitoredHost = mockedHost(monitoredVMs);
		when(monitoredHost.getCPUUsageMHz()).thenReturn(hostCPUUsage);

		VCenterHostMonitor testMonitor = new VCenterHostMonitor(monitoredHost);
		testMonitor.setMonitoredVMs(monitoredVMs);
		testMonitor.setMonitoredPerfs(testMonitor.getAvailablePerfs());

		testMonitor.asynchronousRetrieval();

		while (testMonitor.dirty()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new UnsupportedOperationException("TODO : handle this.",
						e);
			}
		}

		HVSnapshot snap = testMonitor.getLastSnapshot();
		assertTrue(snap.getDate() <= System.currentTimeMillis());
		assertEquals(snap.getDuration(),
				VCenterHostMonitor.MONITORINGDURATIONMS);

		assertEquals(snap.size(), 1);
		assertEquals(snap.get(VCenterHostMonitor.CPUKEY), (double) hostCPUUsage);

		assertEquals(snap.getStoredVmsUsages().size(), nbVMs,
				"present vms snapshots are : "
						+ snap.getStoredVmsUsages().keySet());

		for (ActivityReport report : snap.getStoredVmsUsages().values()) {
			assertEquals(report.entrySet().size(), 1);
			assertEquals(report.get(VCenterHostMonitor.CPUKEY),
					(double) vmCPUUsage);
		}
	}
}
