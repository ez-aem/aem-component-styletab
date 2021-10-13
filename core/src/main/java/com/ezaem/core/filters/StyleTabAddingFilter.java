package com.ezaem.core.filters;

import com.ezaem.core.models.StyleHidingModel;
import com.ezaem.core.styletab.StyleTabAddingResourceWrapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.*;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.apache.sling.engine.EngineConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

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
        if (resource.isResourceType("cq/gui/components/authoring/dialog")
                && (resource.getPath().endsWith("cq:dialog") || resource.getPath().endsWith("_cq_dialog"))) {

            boolean hasSomeStyles = Optional.ofNullable(slingRequest.adaptTo(StyleHidingModel.class))
                    .map(StyleHidingModel::getStyleGroups)
                    .flatMap(styleGroups -> styleGroups.stream()
                            .flatMap(styleGroup -> styleGroup.getStyles().stream())
                            .findAny())
                    .isPresent();
            if (hasSomeStyles) {
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
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }

}