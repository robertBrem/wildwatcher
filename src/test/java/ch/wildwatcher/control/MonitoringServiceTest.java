package ch.wildwatcher.control;

import static org.junit.Assert.assertArrayEquals;

import java.net.InetAddress;

import org.junit.Before;
import org.junit.Test;

public class MonitoringServiceTest {

	MonitoringService service;

	@Before
	public void setUp() {
		service = new MonitoringService();
	}

	@Test
	public void should_return_the_converted_ip_address() {
		InetAddress ipAddress = service.getHost("10.12.13.14");
		byte[] expected = new byte[] { 10, 12, 13, 14 };
		assertArrayEquals(expected, ipAddress.getAddress());
	}
}
