/**
 *    Copyright 2006-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.solmix.generator.plugin;

import java.util.List;

import org.solmix.commons.util.StringUtils;
import org.solmix.commons.xml.dom.Attribute;
import org.solmix.commons.xml.dom.Document;
import org.solmix.commons.xml.dom.XmlElement;
import org.solmix.generator.api.IntrospectedTable;
import org.solmix.generator.api.PluginAdapter;

/**
 * This plugin adds a cache element to generated sqlMaps.  This plugin
 * is for MyBatis3 targeted runtimes only.  The plugin accepts the
 * following properties (all are optional):
 * 
 * <ul>
 *   <li>cache_eviction</li>
 *   <li>cache_flushInterval</li>
 *   <li>cache_size</li>
 *   <li>cache_readOnly</li>
 *   <li>cache_type</li>
 * </ul>
 * 
 * <p>All properties correspond to properties of the MyBatis cache element and
 * are passed "as is" to the corresponding properties of the generated cache
 * element.  All properties can be specified at the table level, or on the
 * plugin element.  The property on the table element will override any
 * property on the plugin element.
 * 
 * @author Jason Bennett
 * @author Jeff Butler
 */
public class CachePlugin extends PluginAdapter {
    public enum CacheProperty {
        EVICTION("cache_eviction", "eviction"),  //$NON-NLS-2$
        FLUSH_INTERVAL("cache_flushInterval", "flushInterval"),  //$NON-NLS-2$
        READ_ONLY("cache_readOnly", "readOnly"),  //$NON-NLS-2$
        SIZE("cache_size", "size"),  //$NON-NLS-2$
        TYPE("cache_type", "type");  //$NON-NLS-2$

        private String propertyName;
        private String attributeName;

        CacheProperty(String propertyName, String attributeName) {
            this.propertyName = propertyName;
            this.attributeName = attributeName;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getAttributeName() {
            return attributeName;
        }
    }

    public CachePlugin() {
        super();
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {

        XmlElement element = new XmlElement("cache"); 
        domain.getCommentGenerator().addComment(element);

        for (CacheProperty cacheProperty : CacheProperty.values()) {
            addAttributeIfExists(element, introspectedTable, cacheProperty);
        }

        document.getRootElement().addElement(element);

        return true;
    }

    private void addAttributeIfExists(XmlElement element, IntrospectedTable introspectedTable,
            CacheProperty cacheProperty) {
        String property = introspectedTable.getTableInfoProperty(cacheProperty.getPropertyName());
        if (property == null) {
            property = properties.getProperty(cacheProperty.getPropertyName());
        }

        if (StringUtils.stringHasValue(property)) {
            element.addAttribute(new Attribute(cacheProperty.getAttributeName(), property));
        }
    }
}
