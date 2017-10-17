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

import org.solmix.commons.xml.dom.Attribute;
import org.solmix.commons.xml.dom.TextElement;
import org.solmix.commons.xml.dom.XmlElement;


/**
 * 
 * @author Jeff Butler
 * 
 */
public class CountByExampleElementGenerator extends AbstractXmlElementGenerator {

    public CountByExampleElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("select"); 

        String fqjt = introspectedTable.getExampleType();

        answer.addAttribute(new Attribute(
                "id", introspectedTable.getCountByExampleStatementId())); 
        answer.addAttribute(new Attribute("parameterType", fqjt)); 
        answer.addAttribute(new Attribute("resultType", "java.lang.Long"));  //$NON-NLS-2$

        domain.getCommentGenerator().addComment(answer);

        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from "); 
        sb.append(introspectedTable
                .getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));
        answer.addElement(getExampleIncludeElement());

        if (domain.getPlugins().sqlMapCountByExampleElementGenerated(
                answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
