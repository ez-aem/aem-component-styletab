package com.ezaem.core.servlets;

import com.adobe.cq.wcm.style.ComponentStyleInfo;
import com.adobe.cq.wcm.style.ContentPolicyStyleInfo;
import com.adobe.cq.wcm.style.StyleGroupInfo;
import com.adobe.cq.wcm.style.StyleInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component(
        service = Servlet.class
)
public class StylesServlet extends SlingSafeMethodsServlet {



//    private void initModel() {
//        final Resource styledResource = this.getStyledResource(this.path, this.resourceType);
//        List<StyleGroupItem> styleGroups = new ArrayList<>();
//        List<StyleInfo> appliedStyles = new ArrayList<>();
//        if (styledResource != null) {
//            final ComponentStyleInfo componentStyleInfo = (ComponentStyleInfo)styledResource.adaptTo((Class)ComponentStyleInfo.class);
//            if (componentStyleInfo != null) {
//                final ContentPolicyStyleInfo contentPolicyStyleInfo = componentStyleInfo.getContentPolicyStyleInfo();
//                this.appliedStyles = (List<StyleInfo>)componentStyleInfo.getAppliedStyles();
//                if (contentPolicyStyleInfo != null) {
//                    final List<StyleGroupInfo> styleGroupInfos = (List<StyleGroupInfo>)contentPolicyStyleInfo.getStyleGroups();
//                    for (final StyleGroupInfo styleGroupInfo : styleGroupInfos) {
//                        this.styleGroups.add((Object)new StyleGroupItemImpl(styleGroupInfo, this.appliedStyles));
//                    }
//                }
//            }
//        }
//    }

    private class StyleGroupItem {
    }
}
