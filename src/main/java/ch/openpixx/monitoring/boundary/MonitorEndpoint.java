package ch.openpixx.monitoring.boundary;

import java.net.InetAddress;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;

import ch.openpixx.monitoring.control.MonitoringService;

@Path("servers")
public class MonitorEndpoint {

	@Inject
	MonitoringService service;

	@GET
	@Path("{ip}")
	public String getServerStatus(@PathParam("ip") String ip) {
		return getServerStatus(ip, MonitoringService.DEFAULT_MANAGEMENT_PORT);
	}

	@GET
	@Path("{ip}:{port}")
	public String getServerStatus(@PathParam("ip") String ip, @PathParam("port") String port) {
		InetAddress host = service.getHost(ip);
		int parsedPort = 9990;
		if (port != null && !port.isEmpty()) {
			parsedPort = Integer.parseInt(port);
		}
		String username = "admin";
		String password = "admin";
		String securityRealmName = "ManagementRealm";

		ModelNode serverState = new ModelNode();
		serverState.get("operation").set("read-attribute");
		serverState.get("name").set("server-state");

		ModelControllerClient client = service.createClient(host, parsedPort, username, password, securityRealmName);
		String serverStateResult = service.getResult(serverState, client);
		return "Server is: " + serverStateResult;
	}

	@GET
	@Path("{ip}:{port}/deployments/{warFile}")
	public String getDeploymentStatus(@PathParam("ip") String ip, @PathParam("port") String port, @PathParam("warFile") String warFile) {
		InetAddress host = service.getHost(ip);
		int parsedPort = Integer.parseInt(port);
		String username = "admin";
		String password = "admin";
		String securityRealmName = "ManagementRealm";

		ModelNode deploymentStatus = new ModelNode();
		deploymentStatus.add("deployment", warFile);
		final ModelNode op = Operations.createReadResourceOperation(deploymentStatus, true);
		op.get("operation").set("read-attribute");
		op.get("name").set("status");

		ModelControllerClient client = service.createClient(host, parsedPort, username, password, securityRealmName);
		String deploymentStatusResult = service.getResult(op, client);

		return "Deployment is: " + deploymentStatusResult;
	}
}
