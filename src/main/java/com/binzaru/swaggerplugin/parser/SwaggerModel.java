package com.binzaru.swaggerplugin.parser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class SwaggerModel {
    private final String name;
    private final Map<String, PropertyInfo> properties;

    public SwaggerModel(String name) {
        this.name = name;
        this.properties = new LinkedHashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<String, PropertyInfo> getProperties() {
        return properties;
    }

    public void addProperty(String name, PropertyInfo info) {
        this.properties.put(name, info);
    }

    public PropertyInfo getProperty(String name) {
        return properties.get(name);
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    public static class PropertyInfo {
        private final String type;
        private final String description;
        private final String ref;

        public PropertyInfo(String type, String description, String ref) {
            this.type = type;
            this.description = description;
            this.ref = ref;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public String getRef() {
            return ref;
        }

        public boolean isReference() {
            return ref != null && !ref.isEmpty();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PropertyInfo that = (PropertyInfo) o;
            return Objects.equals(type, that.type) &&
                    Objects.equals(description, that.description) &&
                    Objects.equals(ref, that.ref);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, description, ref);
        }
    }
}