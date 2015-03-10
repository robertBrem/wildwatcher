package ch.wildwatcher.control;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.ejb.Stateless;

@Stateless
public class StringConverter {
	public static final int IP_PARTS = 4;
	public static final String POINT = "\\.";
	public static final String QUOTE = "\"";
	public static final String NULL_VALUE = "null";

	public static final String FALSE = "false";
	public static final String TRUE = "true";

	public String removeQuotes(String toUpdate) {
		return toUpdate.substring(1, toUpdate.length() - 1);
	}

	public Boolean getBoolean(String toParse) {
		String toParseTrimed = setUpForParsing(toParse);
		if (toParseTrimed.equalsIgnoreCase(TRUE) || toParseTrimed.equalsIgnoreCase(FALSE)) {
			try {
				return Boolean.parseBoolean(toParseTrimed);
			} catch (Exception e) {
			}
		}
		return null;
	}

	public Double getDouble(String toParse) {
		String toParseTrimed = setUpForParsing(toParse);
		try {
			return Double.parseDouble(toParseTrimed);
		} catch (Exception e) {
		}
		return null;
	}

	public Integer getInt(String toParse) {
		String toParseTrimed = setUpForParsing(toParse);
		try {
			return Integer.parseInt(toParseTrimed);
		} catch (Exception e) {
		}
		return null;
	}

	private String setUpForParsing(String toParse) {
		String toParseTrimed = toParse.trim();
		return toParseTrimed;
	}

	public InetAddress getIpAddress(String ipOrHostname) {
		try {
			return getIp(ipOrHostname);
		} catch (Exception e) {
		}
		return null;
	}

	public InetAddress getIp(String ip) {
		String[] ipParts = ip.split(POINT);
		if (ipParts.length != IP_PARTS) {
			throw new IllegalArgumentException(ip + " is not a valid ip address");
		}
		return getIpAddress(toByteArray(ipParts));
	}

	public byte[] toByteArray(String[] ipParts) {
		byte[] ipAddress = new byte[4];
		int index = 0;
		for (String ipString : ipParts) {
			Integer intValue = (Integer) Integer.parseInt(ipString);
			if (intValue < 0 || intValue > 255) {
				throw new IllegalArgumentException(intValue + " is out of range!");
			}
			ipAddress[index++] = intValue.byteValue();
		}
		return ipAddress;
	}

	public InetAddress getIpAddress(byte[] ipAddress) {
		try {
			return InetAddress.getByAddress(ipAddress);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException(ipAddress + " is not a correct ip address!");
		}
	}
}
