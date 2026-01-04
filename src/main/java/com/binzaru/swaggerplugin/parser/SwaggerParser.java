package com.binzaru.swaggerplugin.parser;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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

    /**
     * Extracts the schemas section from a Swagger/OpenAPI JSON object.
     * <p>
     * Supports both OpenAPI 3.0 format (components/schemas) and Swagger 2.0 format (definitions).
     *
     * @param swagger the root Swagger/OpenAPI JSON object
     * @return JsonObject containing the schemas, or null if no schemas section found
     */
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

    /**
     * Parses the schemas section of a Swagger JSON object and populates the internal model registry.
     * <p>
     * For each schema, creates a SwaggerModel and extracts all properties including their types,
     * descriptions, and $ref references to other schemas.
     *
     * @param schemas the "schemas" or "definitions" JsonObject from the Swagger file
     */
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

            models.put(modelName, model);
        }
    }

    /**
     * Retrieves a Swagger model by its name.
     * <p>
     * Model names are case-sensitive and must match exactly as defined in the Swagger schema.
     *
     * @param modelName the exact name of the model to retrieve (case-sensitive).
     *                  Typically obtained via {@link SqlFileParser#extractModelName(String)}
     *                  or {@link #resolveRef(String)}
     * @return the SwaggerModel object if found, or null if no model exists with the given name
     */
    public SwaggerModel getModel(@NotNull String modelName) {
        return models.get(modelName);
    }

    /**
     * Resolves a Swagger $ref string to the corresponding model.
     *
     * @param ref the $ref string in format "#/components/schemas/ModelName"
     * @return the SwaggerModel object if found, or null if no model exists with the given name
     */
    public SwaggerModel resolveRef(@NotNull String ref) {
        // $ref format: "#/components/schemas/AddressDetails"
        String[] parts = ref.split("/");
        String modelName = parts[parts.length - 1];
        return getModel(modelName);
    }

    /**
     * Checks whether a Swagger schema has been successfully loaded into memory.
     *
     * @return true if at least one model has been loaded, false otherwise
     */
    public boolean isLoaded() {
        return !models.isEmpty();
    }

    /**
     * Returns all model names defined in the loaded Swagger schema.
     * <p>
     * The returned set maintains insertion order (the order models appear in the Swagger file).
     *
     * @return a new LinkedHashSet containing all model names, or an empty set if no Swagger is loaded
     */
    public Set<String> getAllModelNames() {
        return new LinkedHashSet<>(models.keySet());
    }
}