package com.ezaem.core.models;

import com.adobe.cq.wcm.style.StyleGroupInfo;
import com.adobe.cq.wcm.style.StyleInfo;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StyleGroupInfoWrapper implements StyleGroupInfo {

    @Delegate
    private StyleGroupInfo sgi;

    @Getter
    private List<StyleInfo> styles;

    public StyleGroupInfoWrapper(StyleGroupInfo sgi, Set<String> appliedStyleIds) {
        this.sgi = sgi;
        styles = sgi.getStyles().stream()
                .map(styleInfo -> new StyleInfoWrapper(styleInfo, appliedStyleIds))
                .collect(Collectors.toList());

    }

    public static boolean shouldBeDisplayed(StyleGroupInfo sgi) {
        return shouldBeDisplayed(sgi.getLabel());
//        String[] nameParts = StringUtils.split(sgi.getLabel(), ':');
//        return noFiltering || nameParts.length == 1 || (nameParts.length >= 2 && nameParts[1].contains("author"));
//            if (nameParts.length >= 1) {
//                return true;
//            } else {
//                return nameParts[1].contains("author");
//            }
    }

    static boolean shouldBeDisplayed(String label) {
        return label.startsWith("B");
    }
}
