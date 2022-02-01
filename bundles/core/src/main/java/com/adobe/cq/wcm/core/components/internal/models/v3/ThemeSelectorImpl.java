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

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.*;

import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;

@Model(adaptables = SlingHttpServletRequest.class, adapters = { ThemeSelector.class, ComponentExporter.class }, resourceType = {
        ThemeSelectorImpl.RESOURCE_TYPE_V1})
public class ThemeSelectorImpl extends AbstractComponentImpl implements ThemeSelector {

    protected static final String RESOURCE_TYPE_V1 = "core/wcm/components/themeselector/v1/themeselector";

    @ValueMapValue(name=PROPERTY_RESOURCE_TYPE, injectionStrategy=InjectionStrategy.OPTIONAL)
    @Default(values="No resourceType")
    protected String resourceType;

    @SlingObject
    private Resource currentResource;

    @SlingObject
    private ResourceResolver resourceResolver;

    @Inject
    private Page currentPage;

    private List variables;

    @PostConstruct
    private void initModel() {
        String themePath = currentPage.getProperties().get(THEME_CF_PATH, "");
        String  a = "";
        Resource themeResource = resourceResolver.getResource(themePath + "/jcr:content/data/master");

        if(themeResource != null) {
            try {
                Node themeNode = themeResource.adaptTo(Node.class);
                String cssPath = themeNode.getProperty(CSS_CF_PATH).getString();

                Resource cssResource = resourceResolver.getResource(cssPath + "/jcr:content/data");

                if (cssResource != null) {
                    Node cssNode = cssResource.adaptTo(Node.class);
                    String modelPath = cssNode.getProperty("cq:model").getString();

                    Resource modelResource = resourceResolver.getResource(modelPath + "/jcr:content/model/cq:dialog/content/items/");

                    if (modelResource != null) {
                        this.variables = new ArrayList();
                        for (Resource res : modelResource.getChildren()) {
                            Node model = res.adaptTo(Node.class);
                            Node master = cssResource.getChild("master").adaptTo(Node.class);

                            String name = model.getProperty("name").getString();
                            String label = model.getProperty("fieldLabel").getString();
                            String value = master.getProperty(name) != null ? master.getProperty(name).getString() : model.getProperty("value").getString();


                            this.variables.add(label + ": " + value);
                        }
                    }
                }
            } catch(RepositoryException ex) {
            }
        }
    }

    public String getVariables() {
        return ":root {" + String.join(";", this.variables) + ";}";
    }
}
