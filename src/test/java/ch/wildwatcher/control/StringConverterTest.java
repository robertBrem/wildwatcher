package ch.wildwatcher.control;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.net.InetAddress;

import org.junit.Before;
import org.junit.Test;

public class StringConverterTest {
	public static final Double EPSILON = 0.00000001;

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

	@Test
	public void should_return_true_as_Boolean() {
		Boolean actual = converter.getBoolean("true");
		assertEquals(true, actual);
	}

	@Test
	public void should_return_TRUE_as_Boolean() {
		Boolean actual = converter.getBoolean("TRUE");
		assertEquals(true, actual);
	}

	@Test
	public void should_return_TrUe_as_Boolean() {
		Boolean actual = converter.getBoolean("TrUe");
		assertEquals(true, actual);
	}

	@Test
	public void should_return_true_with_spaces_as_Boolean() {
		Boolean actual = converter.getBoolean("  true		");
		assertEquals(true, actual);
	}

	@Test
	public void should_return_null_as_Boolean() {
		Boolean actual = converter.getBoolean("richtig");
		assertEquals(null, actual);
	}

	@Test
	public void should_return_null_because_of_spaces() {
		Boolean actual = converter.getBoolean("t ru e");
		assertEquals(null, actual);
	}

	@Test
	public void should_return_false_as_Boolean() {
		Boolean actual = converter.getBoolean("false");
		assertEquals(false, actual);
	}

	@Test
	public void should_return_FALSE_as_Boolean() {
		Boolean actual = converter.getBoolean("FALSE");
		assertEquals(false, actual);
	}

	@Test
	public void should_return_FaLsE_as_Boolean() {
		Boolean actual = converter.getBoolean("FaLsE");
		assertEquals(false, actual);
	}

	@Test
	public void should_return_false_with_spaces_as_Boolean() {
		Boolean actual = converter.getBoolean("  false		");
		assertEquals(false, actual);
	}

	@Test
	public void should_return_null_as_Boolean_() {
		Boolean actual = converter.getBoolean("falsch");
		assertEquals(null, actual);
	}

	@Test
	public void should_return_null_because_of_spaces_() {
		Boolean actual = converter.getBoolean("f als e");
		assertEquals(null, actual);
	}

	@Test
	public void should_return_5_15_as_Double() {
		Double actual = converter.getDouble("5.15");
		assertEquals(5.15, actual, EPSILON);
	}

	@Test
	public void should_return_null_as_Double() {
		Double actual = converter.getDouble("5.eins5");
		assertEquals(null, actual);
	}

	@Test
	public void should_return_null_too_many_points_as_Double() {
		Double actual = converter.getDouble("5.5.0");
		assertEquals(null, actual);
	}

	@Test
	public void should_return_5_0() {
		Double actual = converter.getDouble("5");
		assertEquals(5.0, actual, EPSILON);
	}

	@Test
	public void should_return_4_25_with_whitespaces_as_Double() {
		Double actual = converter.getDouble("   4.25		");
		assertEquals(4.25, actual, EPSILON);
	}

	@Test
	public void should_return_5_as_Integer() {
		Integer actual = converter.getInt("5");
		assertEquals(new Integer(5), actual);
	}

	@Test
	public void should_return_null_as_Integer() {
		Integer actual = converter.getInt("ein5");
		assertEquals(null, actual);
	}

	@Test
	public void should_return_4_with_whitespaces_as_Integer() {
		Integer actual = converter.getInt("   4		");
		assertEquals(new Integer(4), actual);
	}

	@Test
	public void should_return_null_contains_point() {
		Integer actual = converter.getInt("4.");
		assertEquals(null, actual);
	}
}
