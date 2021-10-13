package com.ezaem.core.models;

import com.adobe.cq.wcm.style.StyleInfo;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.util.Set;

public class StyleInfoWrapper implements StyleInfo {

    @Delegate
    private StyleInfo styleInfo;

    @Getter
    private boolean selected;

    public StyleInfoWrapper(StyleInfo styleInfo, Set<String> appliedStyleIds) {
        this.styleInfo = styleInfo;
        this.selected = appliedStyleIds.contains(styleInfo.getId());
    }
}
