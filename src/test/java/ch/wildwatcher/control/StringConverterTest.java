package ch.wildwatcher.control;

import static org.junit.Assert.assertArrayEquals;

import java.net.InetAddress;

import org.junit.Before;
import org.junit.Test;

public class StringConverterTest {

	StringConverter converter;

	@Before
	public void setUp() {
		converter = new StringConverter();
	}

	@Test
	public void should_return_the_converted_ip_address() {
		InetAddress ipAddress = converter.getIp("10.12.13.14");
		byte[] expected = new byte[] { 10, 12, 13, 14 };
		assertArrayEquals(expected, ipAddress.getAddress());
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_an_exception_because_one_part_is_too_big() {
		converter.getIp("266.12.13.14");
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_an_exception_because_one_part_is_negative() {
		converter.getIp("-15.12.13.14");
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_an_exception_because_there_are_too_less_parts() {
		converter.getIp("15.12.13");
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_an_exception_because_this_is_no_ip() {
		converter.getIp("helloWorld");
	}
}
