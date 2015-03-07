package ch.openpixx.monitoring.control;

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

import org.jboss.as.controller.client.MessageSeverity;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationMessageHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.threads.AsyncFuture;

@Stateless
public class MonitoringService {

	public ModelControllerClient createClient(final InetAddress host, final int port, final String username, final String password,
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

	public InetAddress getHost(String ip) {
		String[] ipParts = ip.split("\\.");
		if (ipParts.length != 4) {
			throw new IllegalArgumentException(ip + " is not a valid ip address");
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
			throw new IllegalArgumentException(ipAddress + " is not a correct ip address!");
		}
		return host;
	}

	public String getResult(ModelControllerClient client, ModelNode serverState) {
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
			returnVal = resultFuture.get(60, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e1) {
			return e1.getMessage();
		}
		serverStateResult = returnVal.get("result").toString();
		return serverStateResult;
	}

}
