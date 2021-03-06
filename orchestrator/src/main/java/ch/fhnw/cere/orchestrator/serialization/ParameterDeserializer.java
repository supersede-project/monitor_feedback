package ch.fhnw.cere.orchestrator.serialization;

import ch.fhnw.cere.orchestrator.models.Parameter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ParameterDeserializer extends StdDeserializer<Parameter> {

    public ParameterDeserializer() {
        this(null);
    }

    public ParameterDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Parameter deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = jp.getCodec().readTree(jp);
        String key = node.get("key").asText();

        List<Parameter> parameters = new ArrayList<Parameter>();
        String value = null;

        if(node.get("value") == null || node.get("value").toString() == null || node.get("value").toString().length() < 1) {
            value = null;
        } else if(node.get("value").toString().trim().substring(0,1).equals("[")) {
            parameters = mapper.readValue(node.get("value").toString(), new TypeReference<List<Parameter>>() {});
        } else {
            value = node.get("value").asText();
        }
        String language = node.get("language") == null ? null : node.get("language").asText();
        int order = node.get("order") == null ? 0 : node.get("order").asInt();

        if(parameters.size() == 0 && node.get("parameters") != null) {
            // if no nested parameters then the value under 'parameters' key can hold parameters
            parameters = mapper.readValue(node.get("parameters").toString(), new TypeReference<List<Parameter>>() {});
        }

        if(node.get("id") != null && node.get("id").numberValue() != null) {
            long id = node.get("id").numberValue().longValue();
            Parameter parameter = new Parameter(id, key, value, language, null, parameters);
            parameter.setOrder(order);
            return parameter;
        } else {
            Parameter parameter = new Parameter(key, value, language, null, parameters);
            parameter.setOrder(order);
            return parameter;
        }
    }
}