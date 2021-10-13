package com.ezaem.core.styletab;

import com.ezaem.core.util.SyntheticResourceWithProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class StyleTabAddingIterator implements Iterator<Resource> {

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
                && resource.getValueMap()
                .get("path", StringUtils.EMPTY)
                .equals("cq/gui/components/authoring/dialog/style/tab_edit/styletab");
    }
}
