package com.binzaru.swaggerplugin.parser;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;

public class SwaggerParser {
    private final Map<String, SwaggerModel> models = new LinkedHashMap<>();

    /**
     * Loads and parses a Swagger/OpenAPI schema file into memory.
     * <p>
     * Clears any previously loaded models and populates the internal model registry
     * from the schemas defined in the Swagger file.
     * <p>
     * Supports both OpenAPI 3.0 (components/schemas) and Swagger 2.0 (definitions) formats.
     *
     * @param absoluteSwaggerPath the absolute Swagger file path, typically resolved via
     *                    {@link SqlFileParser#resolveSwaggerPath(String, com.intellij.openapi.vfs.VirtualFile)}
     * @throws FileNotFoundException if the Swagger file does not exist at the specified path
     */
    public void loadSwagger(String absoluteSwaggerPath) throws FileNotFoundException {
        this.models.clear();

        Gson gson = new Gson();
        JsonObject swagger = gson.fromJson(new FileReader(absoluteSwaggerPath), JsonObject.class);

        JsonObject schemas = extractSchemas(swagger);
        if (schemas != null) {
            parseSchemas(schemas);
        }
    }

    private JsonObject extractSchemas(JsonObject swagger) {
        // OpenAPI 3.0
        if (swagger.has("components") && swagger.getAsJsonObject("components").has("schemas")) {
            return swagger.getAsJsonObject("components").getAsJsonObject("schemas");
        }
        // Swagger 2.0
        else if (swagger.has("definitions")) {
            return swagger.getAsJsonObject("definitions");
        }
        return null;
    }

    private void parseSchemas(JsonObject schemas) {
        for (Map.Entry<String, JsonElement> entry : schemas.entrySet()) {
            String modelName = entry.getKey();
            JsonObject schema = entry.getValue().getAsJsonObject();

            SwaggerModel model = new SwaggerModel(modelName);

            if (schema.has("properties")) {
                JsonObject properties = schema.getAsJsonObject("properties");

                for (Map.Entry<String, JsonElement> propEntry : properties.entrySet()) {
                    String propName = propEntry.getKey();
                    JsonObject propData = propEntry.getValue().getAsJsonObject();

                    String type = propData.has("type") ? propData.get("type").getAsString() : null;
                    String description = propData.has("description") ? propData.get("description").getAsString() : null;
                    String ref = propData.has("$ref") ? propData.get("$ref").getAsString() : null;

                    SwaggerModel.PropertyInfo info = new SwaggerModel.PropertyInfo(type, description, ref);
                    model.addProperty(propName, info);
                }
            }

            models.put(modelName.toLowerCase(), model);
        }
    }

    public SwaggerModel getModel(String name) {
        return models.get(name.toLowerCase());
    }

    public SwaggerModel resolveRef(String ref) {
        // $ref format: "#/components/schemas/AddressDetails"
        String[] parts = ref.split("/");
        String modelName = parts[parts.length - 1];
        return getModel(modelName);
    }

    public boolean isLoaded() {
        return !models.isEmpty();
    }

    public Set<String> getAllModelNames() {
        return models.keySet().stream()
                .map(name -> models.get(name).getName())
                .collect(Collectors.toSet());
    }
}