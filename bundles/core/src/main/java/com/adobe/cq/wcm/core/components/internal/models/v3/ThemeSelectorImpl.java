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

    @ValueMapValue(name=PROPERTY_RESOURCE_TYPE, injectionStrategy=InjectionStrategy.OPTIONAL)
    @Default(values="No resourceType")
    protected String resourceType;

    @SlingObject
    private Resource currentResource;

    @SlingObject
    private ResourceResolver resourceResolver;

    @Inject
    private Page currentPage;

    private List<String> variables;

    @PostConstruct
    private void initModel() {
        String themePath = resolveThemePath();
        Resource themeResource = resourceResolver.getResource(String.format("%s/jcr:content/data/master", themePath));

        if(themeResource != null) {
            try {
                ValueMap themeMap = themeResource.getValueMap();
                resolveVariables(themeMap.get(CSS_CF_PATH, new String[0]));
            } catch(Exception ex) {
                LOG.error("Couldn't read theme CF path for current page", ex);
            }
        }
    }

    private void resolveVariables(String[] paths) {
        this.variables = new ArrayList<>();
        for(String cssPath : paths) {
            Resource cssResource = resourceResolver.getResource(String.format("%s/jcr:content/data", cssPath));

            if (cssResource != null) {
                Resource master = cssResource.getChild("master");
                ValueMap stylesMap = cssResource.getValueMap();
                String modelPath = stylesMap.get("cq:model", String.class);
                Resource modelResource = resourceResolver.getResource(String.format("%s/jcr:content/model/cq:dialog/content/items/", modelPath));

                if (modelResource != null && master != null) {
                    ValueMap masterMap = master.getValueMap();

                    for (Resource res : modelResource.getChildren()) {
                        ValueMap modelMap = res.getValueMap();
                        String name = modelMap.get("name", String.class);
                        String label = modelMap.get("fieldLabel", String.class);
                        String value = masterMap.containsKey(name) ? masterMap.get(name, String.class) : modelMap.get("value", String.class);

                        if (value != null)
                            this.variables.add(String.format("%s: %s", label, value));
                    }
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
        return String.format(":root {%s;}", String.join(";", this.variables));
    }
}
