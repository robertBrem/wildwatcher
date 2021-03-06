package ch.wildwatcher.control;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;

import ch.wildwatcher.entity.Attribute;

@Stateless
public class JsonConverter {

	@Inject
	StringConverter stringConverter;

	public void addJsonStructure(Attribute attribute, JsonObjectBuilder jsonObjBuilder) {
		JsonStructure structure = getJsonStructure(attribute.getValue());
		if (structure == null) {
			if (attribute.getValue().startsWith(StringConverter.QUOTE)) {
				jsonObjBuilder.add(attribute.getKey(), stringConverter.removeQuotes(attribute.getValue()));
			}
		} else {
			jsonObjBuilder.add(attribute.getKey(), structure);
		}
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

	public Consumer<? super Attribute> toJSON(JsonObjectBuilder builder) {
		return attribute -> {
			Integer integerResult = stringConverter.getInt(attribute.getValue());
			Double doubleResult = stringConverter.getDouble(attribute.getValue());
			Boolean booleanResult = stringConverter.getBoolean(attribute.getValue());

			if (attribute.getValue().equals(StringConverter.NULL_VALUE)) {
				builder.add(attribute.getKey(), JsonValue.NULL);
			} else if (integerResult != null) {
				builder.add(attribute.getKey(), integerResult);
			} else if (doubleResult != null) {
				builder.add(attribute.getKey(), doubleResult);
			} else if (booleanResult != null) {
				builder.add(attribute.getKey(), booleanResult);
			} else {
				addJsonStructure(attribute, builder);
			}
		};
	}

}
