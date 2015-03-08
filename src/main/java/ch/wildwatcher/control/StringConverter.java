package ch.wildwatcher.control;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.ejb.Stateless;

@Stateless
public class StringConverter {
	public static final int IP_PARTS = 4;

	public static final String FALSE = "false";
	public static final String TRUE = "true";

	public String removeQuotes(String resultString) {
		return resultString.substring(1, resultString.length() - 1);
	}

	public Boolean getBoolean(String resultString) {
		if (resultString.trim().equalsIgnoreCase(TRUE) || resultString.trim().equalsIgnoreCase(FALSE)) {
			try {
				return Boolean.parseBoolean(resultString);
			} catch (Exception e) {
			}
		}
		return null;
	}

	public Double getDouble(String resultString) {
		try {
			return Double.parseDouble(resultString);
		} catch (Exception e) {
		}
		return null;
	}

	public Integer getInt(String resultString) {
		try {
			return Integer.parseInt(resultString);
		} catch (Exception e) {
		}
		return null;
	}

	public InetAddress getIpAddress(String ipOrHostname) {
		try {
			return getIp(ipOrHostname);
		} catch (Exception e) {
		}
		return null;
	}

	public InetAddress getIp(String ip) {
		String[] ipParts = ip.split("\\.");
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
