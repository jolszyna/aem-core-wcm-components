/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2017 Adobe
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
package com.adobe.cq.wcm.core.components.internal.models.v3;

import com.adobe.cq.dam.cfm.ContentElement;
import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.ElementTemplate;
import com.adobe.cq.dam.cfm.FragmentTemplate;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.wcm.core.components.models.ThemeSelector;
import com.adobe.cq.wcm.core.components.util.AbstractComponentImpl;
import com.day.cq.wcm.api.Page;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;

@Model(adaptables = SlingHttpServletRequest.class, adapters = { ThemeSelector.class, ComponentExporter.class }, resourceType = {
        ThemeSelectorImpl.RESOURCE_TYPE_V3})
public class ThemeSelectorImpl extends AbstractComponentImpl implements ThemeSelector {
    private static final Logger LOG = LoggerFactory.getLogger(ThemeSelectorImpl.class);

    protected static final String RESOURCE_TYPE_V3 = "core/wcm/components/themeselector/v3/themeselector";
    protected static final String DEFAULT_CSS_CN = ":root";

    @ValueMapValue(name=PROPERTY_RESOURCE_TYPE, injectionStrategy=InjectionStrategy.OPTIONAL)
    @Default(values="No resourceType")
    protected String resourceType;

    @SlingObject
    private Resource currentResource;

    @SlingObject
    private ResourceResolver resourceResolver;

    @Inject
    private Page currentPage;

    private Map<String, List<String>> variablesMap;

    @PostConstruct
    private void initModel() {
        String themePath = resolveThemePath();
        Resource themeResource = resourceResolver.getResource(themePath);

        if(themeResource != null) {
            try {
                ContentFragment theme = themeResource.adaptTo(ContentFragment.class);

                if(theme != null) {
                    ContentElement element = theme.getElement(CSS_CF_PATH);

                    if(element != null)
                        resolveVariables(element.getValue().getValue(String[].class));
                }
            } catch(Exception ex) {
                LOG.error("Couldn't read theme CF path for current page", ex);
            }
        }
    }

    private void resolveVariables(String[] paths) {
        this.variablesMap = new HashMap<>();
        for(String cssPath : paths) {
            if(StringUtils.isEmpty(cssPath))
                continue;

            Resource cssResource = resourceResolver.getResource(cssPath);

            if(cssResource != null) {
                ContentFragment master = cssResource.adaptTo(ContentFragment.class);

                if(master != null) {
                    List<String> variables = new ArrayList<>();
                    FragmentTemplate template = master.getTemplate();

                    Iterator<ElementTemplate> it = template.getElements();
                    while(it.hasNext()){
                        ElementTemplate et = it.next();
                        String name = et.getName();
                        String value = master.hasElement(name) ? master.getElement(name).getValue().getValue(String.class) : et.getDefaultContent();

                        if(StringUtils.isNotEmpty(value))
                            variables.add(String.format("%s: %s", et.getTitle(), value));
                    }

                    String className = master.hasElement(CSS_CN) ? master.getElement(CSS_CN).getValue().getValue(String.class) : DEFAULT_CSS_CN;
                    this.variablesMap.put(className, variables);
                }
            }
        }
    }

    private String resolveThemePath() {
        String themePath = currentPage.getProperties().get(THEME_CF_PATH, StringUtils.EMPTY);

        if(StringUtils.isNotEmpty(themePath))
            return themePath;

        com.day.cq.wcm.api.Page tmp = currentPage;

        while( tmp != null && tmp.hasContent() && tmp.getDepth() > 1 ) {
            ValueMap props = tmp.getProperties();
            if( props != null ) {
                String tmpPath = props.get(THEME_CF_PATH, StringUtils.EMPTY);
                if(StringUtils.isNotEmpty(tmpPath)) {
                    return tmpPath;
                }
                tmp = tmp.getParent();
            }
        }

        return StringUtils.EMPTY;
    }

    public String getVariables() {
        StringBuilder sb = new StringBuilder();

        for(Map.Entry<String, List<String>> entry : this.variablesMap.entrySet())
            sb.append(String.format("%s {%s;} ", entry.getKey(), String.join(";", entry.getValue())));

        return sb.toString();
    }
}
