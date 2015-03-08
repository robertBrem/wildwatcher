package ch.wildwatcher.control;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ejb.Stateless;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.threads.AsyncFuture;

import ch.wildwatcher.entity.Attribute;

@Stateless
public class MonitoringService {

	private static final int IP_PARTS = 4;
	private static final int DEFAULT_TIMEOUT = 60;

	public static final String DEFAULT_MANAGEMENT_PORT = "9990";
	public static final String DEFAULT_USERNAME = "admin";
	public static final String DEFAULT_PASSWORD = "admin";

	public ModelControllerClient createClient(String ip, String port, String username, String password, String securityRealmName) {
		InetAddress ipAddress = getIp(ip);
		int parsedPort = Integer.parseInt(port);
		return createClient(ipAddress, parsedPort, username, password, securityRealmName);
	}

	public ModelControllerClient createClient(InetAddress ip, int port, String username, String password, final String securityRealmName) {
		if (username == null || username.isEmpty()) {
			username = MonitoringService.DEFAULT_USERNAME;
		}
		if (password == null || password.isEmpty()) {
			password = MonitoringService.DEFAULT_PASSWORD;
		}

		final String usernameFinal = username;
		final String passwordFinal = password;

		final CallbackHandler callbackHandler = (callbacks) -> {
			for (Callback current : callbacks) {
				if (current instanceof NameCallback) {
					NameCallback ncb = (NameCallback) current;
					ncb.setName(usernameFinal);
				} else if (current instanceof PasswordCallback) {
					PasswordCallback pcb = (PasswordCallback) current;
					pcb.setPassword(passwordFinal.toCharArray());
				} else if (current instanceof RealmCallback) {
					RealmCallback rcb = (RealmCallback) current;
					String realmName = rcb.getDefaultText();
					if (securityRealmName != null && !securityRealmName.isEmpty()) {
						realmName = securityRealmName;
					}
					rcb.setText(realmName);
				} else {
					throw new UnsupportedCallbackException(current);
				}
			}
		};

		return ModelControllerClient.Factory.create(ip, port, callbackHandler);
	}

	public InetAddress getIp(String ip) {
		String[] ipParts = ip.split("\\.");
		if (ipParts.length != IP_PARTS) {
			throw new IllegalArgumentException(ip + " is not a valid ip address");
		}

		return getIpAddress(toByteArray(ipParts));
	}

	private byte[] toByteArray(String[] ipParts) {
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

	private InetAddress getIpAddress(byte[] ipAddress) {
		InetAddress host = null;
		try {
			host = InetAddress.getByAddress(ipAddress);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException(ipAddress + " is not a correct ip address!");
		}
		return host;
	}

	public String getResult(ModelControllerClient client, ModelNode serverState) {
		AsyncFuture<ModelNode> resultFuture = client.executeAsync(serverState, (severity, message) -> System.out.println("message: " + message));
		return getResult(resultFuture);
	}

	private String getResult(AsyncFuture<ModelNode> resultFuture) {
		ModelNode returnVal;
		try {
			returnVal = resultFuture.get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			return e.getMessage();
		}
		return returnVal.get("result").toJSONString(true);
	}

	public void closeClient(ModelControllerClient client) {
		try {
			client.close();
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not close client!");
		}
	}

	public Attribute readAttributeResult(ModelNode node, String attributeKey, ModelControllerClient client) {
		node.get("operation").set("read-attribute");
		node.get("name").set(attributeKey);
		return new Attribute(attributeKey, getResult(node, client));
	}

	public Attribute readAttributeResult(String attributeKey, ModelControllerClient client) {
		return readAttributeResult(new ModelNode(), attributeKey, client);
	}

	public String getResult(ModelNode node, ModelControllerClient client) {
		try {
			return getResult(client, node);
		} catch (Exception e) {
			return e.getMessage();
		}
	}

}
