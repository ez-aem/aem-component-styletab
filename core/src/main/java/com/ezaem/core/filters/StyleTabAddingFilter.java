package com.ezaem.core.filters;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.google.common.collect.Iterators;
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

@Component(service = Filter.class,
        property = {
                EngineConstants.SLING_FILTER_SCOPE + "=" + EngineConstants.FILTER_SCOPE_COMPONENT,
                EngineConstants.SLING_FILTER_RESOURCETYPES + "=" + "cq/gui/components/authoring/dialog",
        })
@ServiceDescription("Add style tab to component configuration dialog")
@ServiceRanking(Integer.MAX_VALUE)
public class StyleTabAddingFilter implements Filter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void doFilter(ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) throws IOException, ServletException {

        Resource resource = ((SlingHttpServletRequest) request).getResource();
        if (resource.isResourceType("cq/gui/components/authoring/dialog")) {
            request = new SlingHttpServletRequestWrapper((SlingHttpServletRequest) request) {

                @Override
                public Resource getResource() {
                    return new SyntheticStyleTabResourceWrapper(resource, 0);
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

    private class SyntheticStyleTabResourceWrapper extends ResourceWrapper {
        private static final int DEPTH_AT_WHICH_ADD_STYLE_TAB = 4;

        private int depth;

        public SyntheticStyleTabResourceWrapper(Resource resource, int depth) {
            super(resource);
            this.depth = depth;
        }

        @Override
        public Resource getChild(String relPath) {
            Resource original = super.getChild(relPath);
            if (original == null || depth >= DEPTH_AT_WHICH_ADD_STYLE_TAB) {
                return original;
            } else {
                return new SyntheticStyleTabResourceWrapper(original, depth + 1);
            }
        }

        @Override
        public Iterator<Resource> listChildren() {
            Iterator<Resource> originalChildren = super.listChildren();
            if (depth < DEPTH_AT_WHICH_ADD_STYLE_TAB) {
                return Iterators.transform(originalChildren, child -> new SyntheticStyleTabResourceWrapper(child, depth + 1));
            } else if (depth == DEPTH_AT_WHICH_ADD_STYLE_TAB) {
                Iterator<Resource> syntheticStyleTab = Iterators.forArray(new SyntheticResourceWithProperties(
                        getResourceResolver(), getResource().getPath() + "/styletab", "granite/ui/components/coral/foundation/include",
                        Map.of("path", "cq/gui/components/authoring/dialog/style/tab_edit/styletab")));
                return Iterators.concat(originalChildren, syntheticStyleTab);
            } else {
                return originalChildren;
            }
        }
    }

    private static class SyntheticResourceWithProperties extends SyntheticResource {
        private Map<String, Object> properties;

        SyntheticResourceWithProperties(ResourceResolver resourceResolver, String path, String resourceType, Map<String, Object> properties) {
            super(resourceResolver, path, resourceType);
            this.properties = properties;
        }

        public <T> T adaptTo(Class<T> type) {
            return type == ValueMap.class ? (T) new ValueMapDecorator(this.properties) : super.adaptTo(type);
        }
    }
}