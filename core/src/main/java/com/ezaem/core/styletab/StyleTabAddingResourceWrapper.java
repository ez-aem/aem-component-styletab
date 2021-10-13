package com.ezaem.core.styletab;

import com.google.common.collect.Iterators;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;

public class StyleTabAddingResourceWrapper extends ResourceWrapper {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
