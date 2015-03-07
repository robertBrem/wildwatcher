package ch.openpixx.monitoring.boundry;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.jboss.as.controller.client.MessageSeverity;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationMessageHandler;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.threads.AsyncFuture;

@Path("monitoring")
public class MonitorEndpoint {

	static ModelControllerClient createClient(final InetAddress host, final int port, final String username, final String password,
			final String securityRealmName) {

		final CallbackHandler callbackHandler = new CallbackHandler() {

			public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
				for (Callback current : callbacks) {
					if (current instanceof NameCallback) {
						NameCallback ncb = (NameCallback) current;
						ncb.setName(username);
					} else if (current instanceof PasswordCallback) {
						PasswordCallback pcb = (PasswordCallback) current;
						pcb.setPassword(password.toCharArray());
					} else if (current instanceof RealmCallback) {
						RealmCallback rcb = (RealmCallback) current;
						rcb.setText(rcb.getDefaultText());
					} else {
						throw new UnsupportedCallbackException(current);
					}
				}
			}
		};

		return ModelControllerClient.Factory.create(host, port, callbackHandler);
	}

	@GET
	@Path("servers/{ip}:{port}")
	public String getServerStatus(@PathParam("ip") String ip, @PathParam("port") String port) {

		String[] ipParts = ip.split("\\.");
		if (ipParts.length != 4) {
			return ip + " is not a valid ip address";
		}

		byte[] ipAddress = new byte[4];
		int index = 0;
		for (String ipString : ipParts) {
			ipAddress[index++] = ((Integer) Integer.parseInt(ipString)).byteValue();
		}

		InetAddress host = null;
		try {
			host = InetAddress.getByAddress(ipAddress);
		} catch (UnknownHostException e1) {
			return ipAddress + " is not a correct ip address!";
		}
		int parsedPort = Integer.parseInt(port);
		String username = "admin";
		String password = "admin";
		String securityRealmName = "ManagementRealm";

		ModelControllerClient client = createClient(host, parsedPort, username, password, securityRealmName);

		ModelNode serverState = new ModelNode();
		serverState.get("operation").set("read-attribute");
		serverState.get("name").set("server-state");

		String serverStateResult = "";

		AsyncFuture<ModelNode> resultFuture = client.executeAsync(serverState, new OperationMessageHandler() {

			@Override
			public void handleReport(MessageSeverity severity, String message) {
				System.out.println("severity: " + severity);
				System.out.println("message: " + message);
			}
		});
		ModelNode returnVal;
		try {
			returnVal = resultFuture.get(20, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e1) {
			return e1.getMessage();
		}
		serverStateResult = returnVal.get("result").toString();

		try {
			client.close();
		} catch (IOException e) {
			return e.getMessage();
		}

		return "Server is: " + serverStateResult;
	}

	@GET
	@Path("servers/{ip}/deployments/{warFile}")
	public String getDeploymentStatus(@PathParam("ip") String ip, @PathParam("warFile") String warFile) {

		String[] ipParts = ip.split("\\.");

		if (ipParts.length != 4) {
			return ip + " is not a valid ip address";
		}

		byte[] ipAddress = new byte[4];
		int index = 0;
		for (String ipString : ipParts) {
			ipAddress[index++] = ((Integer) Integer.parseInt(ipString)).byteValue();
		}

		InetAddress host = null;
		try {
			host = InetAddress.getByAddress(ipAddress);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		int port = 9990;
		String username = "admin";
		String password = "admin";
		String securityRealmName = "ManagementRealm";

		ModelControllerClient client = createClient(host, port, username, password, securityRealmName);

		ModelNode deploymentStatus = new ModelNode();
		deploymentStatus.add("deployment", "brandservice.war");
		final ModelNode op = Operations.createReadResourceOperation(deploymentStatus, true);
		op.get("operation").set("read-attribute");
		op.get("name").set("status");

		String deploymentStatusResult = "";
		try {
			final ModelNode outcome = client.execute(op);
			deploymentStatusResult = outcome.get("result").toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "Deployment is: " + deploymentStatusResult;
	}
}
