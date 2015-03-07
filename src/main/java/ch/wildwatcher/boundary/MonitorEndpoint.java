package ch.wildwatcher.boundary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;

import ch.wildwatcher.control.MonitoringService;

@Path("servers")
public class MonitorEndpoint {

	@Inject
	MonitoringService service;

	@GET
	@Path("{ip}")
	public String getServerAttributes(@PathParam("ip") String ip, @QueryParam("username") String username, @QueryParam("password") String password,
			@QueryParam("realm") String securityRealmName) {
		return getServerAttributes(ip, MonitoringService.DEFAULT_MANAGEMENT_PORT, username, password, securityRealmName);
	}

	@GET
	@Path("{ip}:{port}")
	public String getServerAttributes(@PathParam("ip") String ip, @PathParam("port") String port, @QueryParam("username") String username,
			@QueryParam("password") String password, @QueryParam("realm") String securityRealmName) {

		ModelControllerClient client = service.createClient(ip, port, username, password, securityRealmName);

		List<String> attributes = new ArrayList<>();
		attributes.add("server-state");
		attributes.add("launch-type");
		attributes.add("management-major-version");
		attributes.add("management-micro-version");
		attributes.add("management-minor-version");
		attributes.add("name");
		attributes.add("namespaces");
		attributes.add("process-type");
		attributes.add("product-name");
		attributes.add("product-version");
		attributes.add("profile-name");
		attributes.add("release-codename");
		attributes.add("release-version");
		attributes.add("running-mode");
		attributes.add("schema-locations");

		Map<String, String> results = new HashMap<>();
		for (String attribute : attributes) {
			results.put(attribute, service.readAttributeResult(attribute, client));
		}
		service.closeClient(client);

		StringBuilder resultString = new StringBuilder();
		for (String attribute : attributes) {
			resultString.append(attribute);
			resultString.append(" : ");
			resultString.append(results.get(attribute));
			resultString.append("<br />");
		}

		return resultString.toString();
	}

	@GET
	@Path("{ip}/deployments/{warFile}")
	public String getDeploymentStatus(@PathParam("ip") String ip, @PathParam("warFile") String warFile, @QueryParam("username") String username,
			@QueryParam("password") String password, @QueryParam("realm") String securityRealmName) {
		return getDeploymentStatus(ip, MonitoringService.DEFAULT_MANAGEMENT_PORT, warFile, username, password, securityRealmName);
	}

	@GET
	@Path("{ip}:{port}/deployments/{warFile}")
	public String getDeploymentStatus(@PathParam("ip") String ip, @PathParam("port") String port, @PathParam("warFile") String warFile,
			@QueryParam("username") String username, @QueryParam("password") String password, @QueryParam("realm") String securityRealmName) {
		ModelNode deploymentStatus = new ModelNode();
		deploymentStatus.add("deployment", warFile);
		final ModelNode op = Operations.createReadResourceOperation(deploymentStatus, true);
		op.get("operation").set("read-attribute");
		op.get("name").set("status");

		ModelControllerClient client = service.createClient(ip, port, username, password, securityRealmName);
		String deploymentStatusResult = service.getResult(op, client);

		return "Deployment is: " + deploymentStatusResult;
	}
}
