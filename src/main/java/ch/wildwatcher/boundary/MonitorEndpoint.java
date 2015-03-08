package ch.wildwatcher.boundary;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
	@Produces(MediaType.APPLICATION_JSON)
	public JsonArray getServerAttributes(@PathParam("ip") String ip, @QueryParam("username") String username,
			@QueryParam("password") String password, @QueryParam("realm") String securityRealmName) {
		return getServerAttributes(ip, MonitoringService.DEFAULT_MANAGEMENT_PORT, username, password, securityRealmName);
	}

	@GET
	@Path("{ip}:{port}")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonArray getServerAttributes(@PathParam("ip") String ip, @PathParam("port") String port, @QueryParam("username") String username,
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

		JsonArrayBuilder builder = Json.createArrayBuilder();
		attributes.stream().forEach(attribute -> {
			String resultString = service.readAttributeResult(attribute, client);
			JsonObjectBuilder jsonObjBuilder = Json.createObjectBuilder();

			Integer integerResult = null;
			try {
				integerResult = Integer.parseInt(resultString);
			} catch (Exception e) {
			}

			Double doubleResult = null;
			try {
				doubleResult = Double.parseDouble(resultString);
			} catch (Exception e) {
			}

			Boolean booleanResult = null;
			if (resultString.trim().equalsIgnoreCase("true") || resultString.trim().equalsIgnoreCase("false"))
				try {
					booleanResult = Boolean.parseBoolean(resultString);
				} catch (Exception e) {
				}

			if (resultString.equals("null")) {
				jsonObjBuilder.add(attribute, "null");
			} else if (integerResult != null) {
				jsonObjBuilder.add(attribute, integerResult);
			} else if (doubleResult != null) {
				jsonObjBuilder.add(attribute, doubleResult);
			} else if (booleanResult != null) {
				jsonObjBuilder.add(attribute, booleanResult);
			} else {

				InputStream stream = new ByteArrayInputStream(resultString.getBytes(StandardCharsets.UTF_8));
				JsonReader jsonReader = Json.createReader(stream);

				JsonStructure object = null;
				try {
					object = jsonReader.read();
				} catch (Exception e) {

				}
				jsonReader.close();

				if (object == null) {
					if (resultString.startsWith("\"")) {
						resultString = resultString.substring(1, resultString.length() - 1);
						jsonObjBuilder.add(attribute, resultString);
					}
				} else {
					jsonObjBuilder.add(attribute, object);
				}
			}
			builder.add(jsonObjBuilder.build());
		});
		service.closeClient(client);

		return builder.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{ip}/deployments/{warFile}")
	public JsonArray getDeploymentStatus(@PathParam("ip") String ip, @PathParam("warFile") String warFile, @QueryParam("username") String username,
			@QueryParam("password") String password, @QueryParam("realm") String securityRealmName) {
		return getDeploymentStatus(ip, MonitoringService.DEFAULT_MANAGEMENT_PORT, warFile, username, password, securityRealmName);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{ip}:{port}/deployments/{warFile}")
	public JsonArray getDeploymentStatus(@PathParam("ip") String ip, @PathParam("port") String port, @PathParam("warFile") String warFile,
			@QueryParam("username") String username, @QueryParam("password") String password, @QueryParam("realm") String securityRealmName) {
		ModelNode deploymentStatus = new ModelNode();
		deploymentStatus.add("deployment", warFile);
		final ModelNode op = Operations.createReadResourceOperation(deploymentStatus, true);

		ModelControllerClient client = service.createClient(ip, port, username, password, securityRealmName);

		List<String> attributes = new ArrayList<>();
		attributes.add("content");
		attributes.add("enabled");
		attributes.add("name");
		attributes.add("persistent");
		attributes.add("runtime-name");
		attributes.add("status");

		JsonArrayBuilder builder = Json.createArrayBuilder();
		attributes.stream().forEach(attribute -> {
			String resultString = service.readAttributeResult(op, attribute, client);
			JsonObjectBuilder jsonObjBuilder = Json.createObjectBuilder();

			Integer integerResult = null;
			try {
				integerResult = Integer.parseInt(resultString);
			} catch (Exception e) {
			}

			Double doubleResult = null;
			try {
				doubleResult = Double.parseDouble(resultString);
			} catch (Exception e) {
			}

			Boolean booleanResult = null;
			if (resultString.trim().equalsIgnoreCase("true") || resultString.trim().equalsIgnoreCase("false"))
				try {
					booleanResult = Boolean.parseBoolean(resultString);
				} catch (Exception e) {
				}

			if (resultString.equals("null")) {
				jsonObjBuilder.add(attribute, "null");
			} else if (integerResult != null) {
				jsonObjBuilder.add(attribute, integerResult);
			} else if (doubleResult != null) {
				jsonObjBuilder.add(attribute, doubleResult);
			} else if (booleanResult != null) {
				jsonObjBuilder.add(attribute, booleanResult);
			} else {

				InputStream stream = new ByteArrayInputStream(resultString.getBytes(StandardCharsets.UTF_8));
				JsonReader jsonReader = Json.createReader(stream);

				JsonStructure object = null;
				try {
					object = jsonReader.read();
				} catch (Exception e) {

				}
				jsonReader.close();

				if (object == null) {
					if (resultString.startsWith("\"")) {
						resultString = resultString.substring(1, resultString.length() - 1);
						jsonObjBuilder.add(attribute, resultString);
					}
				} else {
					jsonObjBuilder.add(attribute, object);
				}
			}
			builder.add(jsonObjBuilder.build());
		});
		service.closeClient(client);

		return builder.build();
	}
}
