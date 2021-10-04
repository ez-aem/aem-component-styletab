package com.ezaem.core.filters;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

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

import static javax.jcr.query.Query.JCR_SQL2;

@Component(service = Filter.class,
        property = {
                EngineConstants.SLING_FILTER_SCOPE + "=" + EngineConstants.FILTER_SCOPE_COMPONENT,
                EngineConstants.SLING_FILTER_RESOURCETYPES + "=" + "cq/gui/components/authoring/dialog",
        })
@ServiceDescription("Add style tab to component configuration dialog")
@ServiceRanking(-700)
public class StyleTabAddingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static String EXISTING_STYLETAB_QUERY_TEMPLATE = "SELECT * FROM [nt:base] WHERE ISDESCENDANTNODE([%s]) " +
            "AND [path]='cq/gui/components/authoring/dialog/style/tab_edit/styletab' " +
            "AND [sling:resourceType]='granite/ui/components/coral/foundation/include'";

    private static String EXISTING_TABS_QUERY_TEMPLATE = "SELECT * FROM [nt:base] WHERE ISDESCENDANTNODE([%s]) " +
            "AND [sling:resourceType]='granite/ui/components/coral/foundation/tabs'";
    @Override
    public void doFilter(ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) throws IOException, ServletException {

        SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
        Resource resource = slingRequest.getResource();
        if (resource.isResourceType("cq/gui/components/authoring/dialog")) {
//            ResourceResolver resolver = slingRequest.getResourceResolver();
//            String jcrPath = resource.getPath().replaceFirst("/mnt/override", "");
//            Iterator<Resource> existingStyletab = resolver.findResources(String.format(EXISTING_STYLETAB_QUERY_TEMPLATE, jcrPath), JCR_SQL2);
//            boolean styleTabNeeded = !existingStyletab.hasNext();
//
//            if (styleTabNeeded) {
//                String q = String.format(EXISTING_TABS_QUERY_TEMPLATE, jcrPath);
//                Iterator<Resource> tabsComponents = resolver.findResources(String.format(EXISTING_TABS_QUERY_TEMPLATE, jcrPath), JCR_SQL2);
//                Resource topTabComponent = null;
//                Optional<Integer> level = Arrays.stream(Iterators.toArray(
//                        Iterators.transform(tabsComponents, component -> StringUtils.countMatches(jcrPath, '/')), Integer.class
//                )).sorted().findFirst();
//                int baseline = StringUtils.countMatches(resource.getPath(), '/');
//            }

            request = new SlingHttpServletRequestWrapper(slingRequest) {

                @Override
                public Resource getResource() {
                    // 4 is the depth of the tabs in configuration dialogs of the CoreComponents
                    return new SyntheticStyleTabResourceWrapper(super.getResource(), 4);
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

        private int depth;

        public SyntheticStyleTabResourceWrapper(Resource resource, int depth) {
            super(resource);
            log.debug("Wrapping, depth {}, path {}", depth, resource.getPath());
            this.depth = depth;
        }

        @Override
        public Resource getChild(String relPath) {
            Resource original = super.getChild(relPath);
            if (original == null || depth == 0) {
                return original;
            } else {
                return new SyntheticStyleTabResourceWrapper(original, depth - 1);
            }
        }

        @Override
        public Iterator<Resource> listChildren() {
            Iterator<Resource> originalChildren = super.listChildren();
            if (depth > 0) {
                log.debug("Transforming children");
                return Iterators.transform(originalChildren, child -> new SyntheticStyleTabResourceWrapper(child, depth - 1));
            } else if (depth == 0) {
                log.debug("Adding synthetic style tab");
                Iterator<Resource> syntheticStyleTab = Iterators.singletonIterator(new SyntheticResourceWithProperties(
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