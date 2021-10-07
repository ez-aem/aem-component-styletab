package com.ezaem.core.filters;

import com.google.common.collect.Iterators;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.*;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.engine.EngineConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

@Component(service = Filter.class,
        property = {
                EngineConstants.SLING_FILTER_SCOPE + "=" + EngineConstants.FILTER_SCOPE_COMPONENT,
                EngineConstants.SLING_FILTER_RESOURCETYPES + "=" + "cq/gui/components/authoring/dialog",
        })
@ServiceDescription("Add style tab to component configuration dialog")
@ServiceRanking(-700)
public class StyleTabAddingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void doFilter(ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) throws IOException, ServletException {

        SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
        Resource resource = slingRequest.getResource();
        if (resource.isResourceType("cq/gui/components/authoring/dialog")) {

            log.trace("Root at {}", resource.getPath());
            request = new SlingHttpServletRequestWrapper(slingRequest) {

                @Override
                public Resource getResource() {
                    Resource originalResource = super.getResource();
                    // 4 is the depth of the tabs in configuration dialogs of the CoreComponents
                    if (originalResource.isResourceType("cq/gui/components/authoring/dialog")) {
                        return new StyleTabAddingResourceWrapper(originalResource, 4);
                    } else {
                        return originalResource;
                    }
                }
            };
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }

    private class StyleTabAddingResourceWrapper extends ResourceWrapper {

        private int depth;

        public StyleTabAddingResourceWrapper(Resource resource, int depth) {
            super(resource);
            log.trace("{} New wrapper, path {}", depth, resource.getPath());
            this.depth = depth;
        }

        @Override
        public Resource getChild(String relPath) {
            Resource original = super.getChild(relPath);
            if (original == null || depth == 0) {
                log.trace("{} Child original, {} {}", depth, relPath, getPath());
                return original;
            } else {
                log.trace("{} Child wrapping, {} {}", depth, relPath, getPath());
                return new StyleTabAddingResourceWrapper(original, depth - 1);
            }
        }

        @Override
        public Iterable<Resource> getChildren() {
            return () -> listChildren();
        }

        @Override
        public Iterator<Resource> listChildren() {
            Iterator<Resource> originalChildren = super.listChildren();
            log.trace("{} getChildren of {}", depth, getPath());
            if (depth > 0) {
                log.trace("{} Children wrapping, {}", depth, getPath());
                return Iterators.transform(originalChildren, child -> new StyleTabAddingResourceWrapper(child, depth - 1));
            } else if (depth == 0 && getResource().getParent().isResourceType("granite/ui/components/coral/foundation/tabs")) {
                log.trace("{} Adding tab, {}", depth, getPath());
                return new StyleTabAddingIterator(originalChildren, getResourceResolver(), getPath());
            } else {
                log.trace("{} Children original, {}", depth, getPath());
                return originalChildren;
            }
        }
    }

    private static class StyleTabAddingIterator implements Iterator<Resource> {

        private Iterator<Resource> existingItems;
        private ResourceResolver resolver;
        private String path;
        private boolean needsStyleTab = true;

        public StyleTabAddingIterator(Iterator<Resource> existingItems, ResourceResolver resolver, String path) {
            this.existingItems = existingItems;
            this.resolver = resolver;
            this.path = path;
        }

        @Override
        public boolean hasNext() {
            return existingItems.hasNext() || needsStyleTab;
        }

        @Override
        public Resource next() {
            if (existingItems.hasNext()) {
                Resource existingItem = existingItems.next();
                if (isStyleTab(existingItem)) {
                    needsStyleTab = false;
                }
                return existingItem;
            } else {
                if (needsStyleTab) {
                    needsStyleTab = false;
                    return new SyntheticResourceWithProperties(
                            resolver, path + "/styletab", "granite/ui/components/coral/foundation/include",
                            Map.of("path", "cq/gui/components/authoring/dialog/style/tab_edit/styletab"));
                } else {
                    throw new NoSuchElementException();
                }
            }
        }

        private boolean isStyleTab(Resource resource) {
            return resource.isResourceType("granite/ui/components/coral/foundation/include")
                    && resource.getValueMap().get("path", StringUtils.EMPTY)
                            .equals("cq/gui/components/authoring/dialog/style/tab_edit/styletab");
        }
    }

    private static class SyntheticResourceWithProperties extends SyntheticResource {
        private Map<String, Object> properties;

        SyntheticResourceWithProperties(ResourceResolver resourceResolver, String path,
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
}