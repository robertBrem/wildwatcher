package ch.wildwatcher.control;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.jboss.threads.AsyncFuture;

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

		return getHost(toByteArray(ipParts));
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

	private InetAddress getHost(byte[] ipAddress) {
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

	public String readAttributeResult(ModelNode node, String attribute, ModelControllerClient client) {
		node.get("operation").set("read-attribute");
		node.get("name").set(attribute);
		return getResult(node, client);
	}

	public String readAttributeResult(String attribute, ModelControllerClient client) {
		return readAttributeResult(new ModelNode(), attribute, client);
	}

	public String getResult(ModelNode serverState, ModelControllerClient client) {
		try {
			return getResult(client, serverState);
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public Consumer<? super String> toJSON(ModelControllerClient client, JsonArrayBuilder builder) {
		return toJSON(new ModelNode(), client, builder);
	}

	public Consumer<? super String> toJSON(final ModelNode op, ModelControllerClient client, JsonArrayBuilder builder) {
		return attribute -> {
			String resultString = readAttributeResult(op, attribute, client);
			JsonObjectBuilder jsonObjBuilder = Json.createObjectBuilder();

			Integer integerResult = getInt(resultString);
			Double doubleResult = getDouble(resultString);
			Boolean booleanResult = getBoolean(resultString);

			if (resultString.equals("null")) {
				jsonObjBuilder.add(attribute, "null");
			} else if (integerResult != null) {
				jsonObjBuilder.add(attribute, integerResult);
			} else if (doubleResult != null) {
				jsonObjBuilder.add(attribute, doubleResult);
			} else if (booleanResult != null) {
				jsonObjBuilder.add(attribute, booleanResult);
			} else {
				addJsonStructure(attribute, resultString, jsonObjBuilder);
			}
			builder.add(jsonObjBuilder.build());
		};
	}

	public void addJsonStructure(String attribute, String resultString, JsonObjectBuilder jsonObjBuilder) {
		JsonStructure structure = getJsonStructure(resultString);
		if (structure == null) {
			if (resultString.startsWith("\"")) {
				resultString = removeQuotes(resultString);
				jsonObjBuilder.add(attribute, resultString);
			}
		} else {
			jsonObjBuilder.add(attribute, structure);
		}
	}

	public String removeQuotes(String resultString) {
		return resultString.substring(1, resultString.length() - 1);
	}

	public JsonStructure getJsonStructure(String resultString) {
		InputStream stream = new ByteArrayInputStream(resultString.getBytes(StandardCharsets.UTF_8));
		JsonReader jsonReader = Json.createReader(stream);
		JsonStructure structure = null;
		try {
			structure = jsonReader.read();
		} catch (Exception e) {
		} finally {
			jsonReader.close();
		}
		return structure;
	}

	public Boolean getBoolean(String resultString) {
		Boolean booleanResult = null;
		if (resultString.trim().equalsIgnoreCase("true") || resultString.trim().equalsIgnoreCase("false")) {
			try {
				booleanResult = Boolean.parseBoolean(resultString);
			} catch (Exception e) {
			}
		}
		return booleanResult;
	}

	public Double getDouble(String resultString) {
		Double doubleResult = null;
		try {
			doubleResult = Double.parseDouble(resultString);
		} catch (Exception e) {
		}
		return doubleResult;
	}

	public Integer getInt(String resultString) {
		Integer integerResult = null;
		try {
			integerResult = Integer.parseInt(resultString);
		} catch (Exception e) {
		}
		return integerResult;
	}

}
