package com.binzaru.swaggerplugin.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe cache for SwaggerParser instances.
 * Caches parsed Swagger files and automatically invalidates when the file is modified.
 */
public class SwaggerCache {

    private static final ConcurrentHashMap<String, CachedSwagger> cache = new ConcurrentHashMap<>();

    private static class CachedSwagger {
        final long fileLastModified;
        final SwaggerParser swaggerParser;

        CachedSwagger(long fileLastModified, SwaggerParser swaggerParser) {
            this.fileLastModified = fileLastModified;
            this.swaggerParser = swaggerParser;
        }
    }

    /**
     * Returns a cached SwaggerParser for the given file, or parses and caches it if not present
     * or if the file has been modified since last parse.
     *
     * @param swaggerFile the Swagger file to parse
     * @return SwaggerParser instance, or null if the file cannot be parsed
     */
    public static SwaggerParser getOrLoad(File swaggerFile) {
        String absolutePath = swaggerFile.getAbsolutePath();
        long currentLastModified = swaggerFile.lastModified();

        CachedSwagger cached = cache.compute(absolutePath, (path, existing) -> {
            // If cache is valid, return as is
            if (existing != null && existing.fileLastModified == currentLastModified) {
                return existing;
            }

            // Otherwise parse again
            SwaggerParser newParser = new SwaggerParser();
            try {
                newParser.loadSwagger(path);
                return new CachedSwagger(currentLastModified, newParser);
            } catch (FileNotFoundException e) {
                return null;
            }
        });

        return cached != null ? cached.swaggerParser : null;
    }
}