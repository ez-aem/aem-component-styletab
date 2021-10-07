package com.ezaem.core.filters;

import com.google.common.collect.Iterators;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class StyleTabAddingFilterTest {

    AemContext context = new AemContext();

    StyleTabAddingFilter filter = new StyleTabAddingFilter();

    @Mock
    FilterChain filterChain;

    @Captor
    ArgumentCaptor<ServletRequest> requestCaptor;

    @Test
    void testThatTheTabIsAddedWhenNotPresent() throws ServletException, IOException {
        context.load().json("/teaser_dialog.json", "/libs/wcm/core/teaser/cq:dialog");
        context.currentResource("/libs/wcm/core/teaser/cq:dialog");
        context.request().setResource(context.currentResource());
        filter.doFilter(context.request(), context.response(), filterChain);
        verify(filterChain).doFilter(requestCaptor.capture(), any());
        SlingHttpServletRequest augmentedRequest = (SlingHttpServletRequest) requestCaptor.getValue();
        Resource augmentedResource = augmentedRequest.getResource();
        List<Resource> fourthLevelResources = Arrays.stream(
                Iterators.toArray(
                        augmentedResource.getChild("content")
                                .getChild("items")
                                .getChild("tabs")
                                .getChild("items")
                                .listChildren(),
                        Resource.class))
                .collect(Collectors.toList());

        assertThat(fourthLevelResources, hasSize(4));
        assertThat(fourthLevelResources.stream().map(Resource::getResourceType).collect(Collectors.toList()),
                hasItem("granite/ui/components/coral/foundation/include"));
    }

    @Test
    void testThatTheTabIsNotAddedWhenAlreadyPresent() throws ServletException, IOException {
        context.load().json("/teaser_dialog_with_styletab.json", "/libs/wcm/core/teaser/cq:dialog");
        context.currentResource("/libs/wcm/core/teaser/cq:dialog");
        context.request().setResource(context.currentResource());
        filter.doFilter(context.request(), context.response(), filterChain);
        verify(filterChain).doFilter(requestCaptor.capture(), any());
        SlingHttpServletRequest augmentedRequest = (SlingHttpServletRequest) requestCaptor.getValue();
        Resource augmentedResource = augmentedRequest.getResource();
        List<Resource> fourthLevelResources = Arrays.stream(
                        Iterators.toArray(
                                augmentedResource.getChild("content")
                                        .getChild("items")
                                        .getChild("tabs")
                                        .getChild("items")
                                        .listChildren(),
                                Resource.class))
                .collect(Collectors.toList());

        assertThat(fourthLevelResources, hasSize(4));
        assertThat(fourthLevelResources.stream().map(Resource::getResourceType).collect(Collectors.toList()),
                hasItem("granite/ui/components/coral/foundation/include"));
    }

    @Test
    void testThatTheTabIsAddedOnlyOnce() throws ServletException, IOException {
        context.load().json("/teaser_dialog.json", "/libs/wcm/core/teaser/cq:dialog");
        context.currentResource("/libs/wcm/core/teaser/cq:dialog");
        context.request().setResource(context.currentResource());
        filter.doFilter(context.request(), context.response(), filterChain);
        verify(filterChain).doFilter(requestCaptor.capture(), any());
        SlingHttpServletRequest augmentedRequest = (SlingHttpServletRequest) requestCaptor.getValue();
        Resource augmentedResource = augmentedRequest.getResource();

        List<Resource> styleTabs = new ArrayList<>();
        extractStyleTabs(augmentedResource, styleTabs);

        assertThat(styleTabs, hasSize(1));
    }

    private void extractStyleTabs(Resource resource, List<Resource> accumulator) {
        if (resource.isResourceType("granite/ui/components/coral/foundation/include")
            && resource.getValueMap()
                .get("path", StringUtils.EMPTY)
                .equals("cq/gui/components/authoring/dialog/style/tab_edit/styletab")) {
            accumulator.add(resource);
        }
        resource.getChildren().forEach(child -> extractStyleTabs(child, accumulator));
    }
}