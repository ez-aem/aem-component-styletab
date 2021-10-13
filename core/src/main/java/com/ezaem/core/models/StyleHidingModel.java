package com.ezaem.core.models;

import com.adobe.cq.wcm.style.ComponentStyleInfo;
import com.adobe.cq.wcm.style.ContentPolicyStyleInfo;
import com.adobe.cq.wcm.style.StyleGroupInfo;
import com.adobe.cq.wcm.style.StyleInfo;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.policies.ContentPolicy;
import com.day.cq.wcm.api.policies.ContentPolicyManager;
import lombok.Getter;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Model(
        adaptables = SlingHttpServletRequest.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class StyleHidingModel {

    @Self
    private SlingHttpServletRequest request;

    @SlingObject
    private ResourceResolver resourceResolver;

    @Getter
    @RequestAttribute
    private String path;

    @Getter
    private List<StyleInfo> appliedStyles;

    @Getter
    private List<String> hiddenStyleIds;

    @Getter
    private Set<String> visibleStyleIds;

    @Getter
    private List<? extends StyleGroupInfo> styleGroups;

    private ComponentStyleInfo componentStyleInfo;
    private boolean noFiltering;
    private ContentPolicy policy;
    private Resource targetResource;

    @PostConstruct
    private void init() {
        if (path == null) {
            path = request.getRequestPathInfo().getSuffix();
        }
        targetResource = resourceResolver.resolve(path);
        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);

        Page containingPage = pageManager.getContainingPage(targetResource);

        noFiltering = path.startsWith("/conf") || path.startsWith("/content/experience-fragments");
        noFiltering = true;

        componentStyleInfo = targetResource.adaptTo(ComponentStyleInfo.class);

        appliedStyles = componentStyleInfo.getAppliedStyles();
        Set<String> appliedIds = appliedStyles.stream().map(StyleInfo::getId).collect(Collectors.toSet());
        styleGroups = Optional.ofNullable(componentStyleInfo.getContentPolicyStyleInfo())
                .map(ContentPolicyStyleInfo::getStyleGroups)
                .map(styleGroups -> styleGroups.stream()
                        .filter(StyleGroupInfoWrapper::shouldBeDisplayed)
                        .map(styleGroup -> new StyleGroupInfoWrapper(styleGroup, appliedIds))
                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);

        visibleStyleIds = styleGroups.stream()
                .flatMap(styleGroupInfo -> styleGroupInfo.getStyles().stream())
                .map(StyleInfoWrapper.class::cast)
                .filter(StyleInfoWrapper::isSelected)
                .map(StyleInfoWrapper::getId)
                .collect(Collectors.toSet());

        hiddenStyleIds = appliedStyles.stream()
                .map(StyleInfo::getId)
                .filter(not(visibleStyleIds::contains))
                .collect(Collectors.toList());


        ContentPolicyManager policyManager = resourceResolver.adaptTo(ContentPolicyManager.class);
        policy = policyManager.getPolicy(targetResource);
    }

    public String getAction() {
        return path;
    }

    public String getResourceType() {
        return targetResource.getResourceType();
    }

}


//<section data-sly-use.model="com.ezaem.core.models.StyleHidingModel" class="coral-Form-fieldset" data-cq-styleselector="true">
//    <div data-sly-repeat.group="${model.styleGroups}" class="coral-Form-fieldwrapper">
//        <label class="coral-Form-fieldlabel" id="label_cq-styleselector-group-${groupList.index}">${group.label}</label>
//        <coral-select name="./cq:styleIds" class="coral-Form-field" multiple="${group.multiple}" labelledby="label_cq-styleselector-group-${groupList.index}">
//            <coral-select-item data-sly-test="${!group.multiple}" value="">${'None' @ i18n}</coral-select-item>
//            <coral-select-item data-sly-repeat.style="${group.styles}" value="${style.id}" selected="${style.selected}">${style.label}</coral-select-item>
//        </coral-select>
//    </div>
//    <input type="hidden" name="./cq:styleIds@Delete"/>
//    <input type="hidden" name="./cq:styleIds@TypeHint" value="String[]"/>
//</section>
//}

//<form data-sly-use.styleSelector="${'com.ezaem.core.models.StyleHidingModel' @ path=request.requestPathInfo.suffix, resourceType=request.requestParameterMap['resourceType']}" id="editor-styleselector-form" action="${styleSelector.action}" method="post" data-editor-styleselector-item-id="${styleSelector.path}" class="foundation-form" data-foundation-form-ajax="true" data-editor-styleselector-form>
//    <coral-selectlist class="editor-StyleSelector-selectList" multiple data-editor-styleselector-selectlist data-sly-list.group="${styleSelector.styleGroups}">
//        <coral-selectlist-group label="${group.label}" data-editor-styleselector-group-multiple="${group.multiple}" data-sly-list.style="${group.styles}">
//            <coral-selectlist-item selected="${style.selected}" value="${style.id}">${style.label}</coral-selectlist-item>
//        </coral-selectlist-group>
//    </coral-selectlist>
//    <sly data-sly-list.style="${styleSelector.appliedStyles}">
//        <input type="hidden" name="./cq:styleIds" value="${style.id}" data-editor-styleselector-id-input="true"/>
//    </sly>
//    <input type="hidden" name="./sling:resourceType" value="${styleSelector.resourceType}"/>
//    <input type="hidden" name="./cq:styleIds@Delete"/>
//    <input type="hidden" name="./cq:styleIds@TypeHint" value="String[]"/>
//</form>