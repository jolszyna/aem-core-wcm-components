/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2019 Adobe
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
package com.adobe.cq.wcm.core.components.models;

import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface ThemeSelector extends Component {
    /**
     * Path to the theme Content Fragment
     */
    String THEME_CF_PATH = "themeCFPath";

    /**
     * Path to the css Content Fragment (to be extracted from the theme Content Fragment)
     */
    String CSS_CF_PATH = "cssCFPath";

    /**
     * CSS class name (to be extracted from the theme Content Fragment)
     */
    String CSS_CN = "cssCN";

    /**
     * Returns string with css variables to be exposed (all properties from the css Content Fragment)
     */
    @Nullable
    default String getVariables() {
        return null;
    }
}
