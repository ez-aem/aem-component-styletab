package com.ezaem.core.filters;

import com.google.common.collect.Iterators;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.jupiter.api.Assertions.*;
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
    void testThatTheTabIsAdded() throws ServletException, IOException {
        context.load().json("/teaser_dialog.json", "/libs/wcm/core/teaser/cq:dialog");
        context.currentResource("/libs/wcm/core/teaser/cq:dialog");
        context.request().setResource(context.currentResource());
        filter.doFilter(context.request(), context.response(), filterChain);
        verify(filterChain).doFilter(requestCaptor.capture(), any());
        SlingHttpServletRequest augmentedRequest = (SlingHttpServletRequest) requestCaptor.getValue();
        Resource augmentedResource = augmentedRequest.getResource();
        List<String> fourthLevelResourceTypes = Arrays.stream(
                Iterators.toArray(
                        augmentedResource.getChild("content")
                                .getChild("items")
                                .getChild("tabs")
                                .getChild("items")
                                .listChildren(),
                        Resource.class))
                .map(Resource::getResourceType)
                .collect(Collectors.toList());

        assertThat(fourthLevelResourceTypes,
                hasItem("granite/ui/components/coral/foundation/include"));
    }
}