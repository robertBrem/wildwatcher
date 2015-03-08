package ch.wildwatcher.control;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ejb.Stateless;
import javax.inject.Inject;
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
	private static final int DEFAULT_TIMEOUT = 60;

	public static final String DEFAULT_MANAGEMENT_PORT = "9990";
	public static final String DEFAULT_USERNAME = "admin";
	public static final String DEFAULT_PASSWORD = "admin";

	@Inject
	StringConverter stringConverter;

	public ModelControllerClient createClient(String ipOrHostname, String port, String username, String password, String securityRealmName) {
		int parsedPort = Integer.parseInt(port);
		InetAddress ipAddress = stringConverter.getIpAddress(ipOrHostname);

		if (ipAddress == null) {
			return createClient(ipOrHostname, parsedPort, username, password, securityRealmName);
		} else {
			return createClient(ipAddress, parsedPort, username, password, securityRealmName);
		}
	}

	public ModelControllerClient createClient(InetAddress ip, int port, String username, String password, final String securityRealmName) {
		final CallbackHandler callbackHandler = getClientCallbackHandler(username, password, securityRealmName);
		return ModelControllerClient.Factory.create(ip, port, callbackHandler);
	}

	public ModelControllerClient createClient(String hostname, int port, String username, String password, final String securityRealmName) {
		final CallbackHandler callbackHandler = getClientCallbackHandler(username, password, securityRealmName);
		try {
			return ModelControllerClient.Factory.create(hostname, port, callbackHandler);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException(hostname + " is not a correct hostname!");
		}
	}

	public CallbackHandler getClientCallbackHandler(String username, String password, final String securityRealmName) {
		if (username == null || username.isEmpty()) {
			username = MonitoringService.DEFAULT_USERNAME;
		}
		if (password == null || password.isEmpty()) {
			password = MonitoringService.DEFAULT_PASSWORD;
		}

		final String usernameFinal = username;
		final String passwordFinal = password;

		return callbacks -> {
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
