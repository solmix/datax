/**
 *    Copyright 2006-2016 the original author or authors.
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
package org.solmix.generator.codegen.mybatis.xmlmapper.elements;

import org.solmix.generator.api.IntrospectedColumn;
import org.solmix.commons.xml.dom.Attribute;
import org.solmix.commons.xml.dom.TextElement;
import org.solmix.commons.xml.dom.XmlElement;
import org.solmix.generator.codegen.AbstractGenerator;
import org.solmix.generator.config.GeneratedKey;

/**
 * 
 * @author Jeff Butler
 * 
 */
public abstract class AbstractXmlElementGenerator extends AbstractGenerator {
    public abstract void addElements(XmlElement parentElement);

    public AbstractXmlElementGenerator() {
        super();
    }

    /**
     * This method should return an XmlElement for the select key used to
     * automatically generate keys.
     * 
     * @param introspectedColumn
     *            the column related to the select key statement
     * @param generatedKey
     *            the generated key for the current table
     * @return the selectKey element
     */
    protected XmlElement getSelectKey(IntrospectedColumn introspectedColumn,
            GeneratedKey generatedKey) {
        String identityColumnType = introspectedColumn
                .getFullyQualifiedJavaType().getFullyQualifiedName();

        XmlElement answer = new XmlElement("selectKey"); 
        answer.addAttribute(new Attribute("resultType", identityColumnType)); 
        answer.addAttribute(new Attribute(
                "keyProperty", introspectedColumn.getJavaProperty())); 
        answer.addAttribute(new Attribute("order", 
                generatedKey.getMyBatis3Order())); 
        
        answer.addElement(new TextElement(generatedKey
                        .getRuntimeSqlStatement()));

        return answer;
    }

    protected XmlElement getBaseColumnListElement() {
        XmlElement answer = new XmlElement("include"); 
        answer.addAttribute(new Attribute("refid", 
                introspectedTable.getBaseColumnListId()));
        return answer;
    }

    protected XmlElement getBlobColumnListElement() {
        XmlElement answer = new XmlElement("include"); 
        answer.addAttribute(new Attribute("refid", 
                introspectedTable.getBlobColumnListId()));
        return answer;
    }

    protected XmlElement getExampleIncludeElement() {
        XmlElement ifElement = new XmlElement("if"); 
        ifElement.addAttribute(new Attribute("test", "_parameter != null"));  //$NON-NLS-2$

        XmlElement includeElement = new XmlElement("include"); 
        includeElement.addAttribute(new Attribute("refid", 
                introspectedTable.getExampleWhereClauseId()));
        ifElement.addElement(includeElement);

        return ifElement;
    }

    protected XmlElement getUpdateByExampleIncludeElement() {
        XmlElement ifElement = new XmlElement("if"); 
        ifElement.addAttribute(new Attribute("test", "_parameter != null"));  //$NON-NLS-2$

        XmlElement includeElement = new XmlElement("include"); 
        includeElement.addAttribute(new Attribute("refid", 
                introspectedTable.getMyBatis3UpdateByExampleWhereClauseId()));
        ifElement.addElement(includeElement);

        return ifElement;
    }
}
