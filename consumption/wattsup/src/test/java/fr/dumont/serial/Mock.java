package fr.dumont.serial;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.lelouet.tools.containers.Container;

public class Mock {

	@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
	public static ControlledSerial mockSerial(final Container<String> internal,
			final String... returns) {
		ControlledSerial ret = Mockito.mock(ControlledSerial.class);
		when(ret.setOnMessage(any(Container.class))).thenReturn(null);
		Answer ans = new Answer() {

			int pos = -1;
			String[] rets = returns;

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				pos++;
				if (rets != null && pos >= rets.length) {
					pos = 0;
				}
				internal.set(rets != null ? rets[pos] : null);
				return null;
			}
		};
		stubVoid(ret).toAnswer(ans).on().write(anyString());
		return ret;
	}

	@Test
	public void testMockedSerial() {
		Container<String> cont = new Container<String>();
		String s1 = "lol", s2 = "dtc";
		ControlledSerial serial = Mock.mockSerial(cont, s1, s2);
		String e1 = "nothing", e2 = "re-nothing FFS !";
		cont.set("megalol");
		serial.write(e1);
		Assert.assertEquals(cont.get(), s1);
		serial.write(e2);
		Assert.assertEquals(cont.get(), s2);
		verify(serial).write(e1);
		verify(serial).write(e2);
	}

}
