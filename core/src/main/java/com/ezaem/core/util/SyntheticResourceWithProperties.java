package com.ezaem.core.util;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import java.util.Map;

public class SyntheticResourceWithProperties extends SyntheticResource {
    private Map<String, Object> properties;

    public SyntheticResourceWithProperties(ResourceResolver resourceResolver, String path,
                                    String resourceType, Map<String, Object> properties) {
        super(resourceResolver, path, resourceType);
        this.properties = properties;
    }

    public <T> T adaptTo(Class<T> type) {
        return type == ValueMap.class
               ? (T) new ValueMapDecorator(this.properties)
               : super.adaptTo(type);
    }
}
