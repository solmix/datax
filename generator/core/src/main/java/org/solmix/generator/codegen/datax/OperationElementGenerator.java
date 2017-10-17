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

package org.solmix.generator.codegen.datax;

import org.solmix.commons.xml.dom.XmlElement;
import org.solmix.datax.model.OperationInfo;
import org.solmix.datax.model.OperationType;
import org.solmix.generator.codegen.mybatis.xmlmapper.elements.AbstractXmlElementGenerator;

/**
 * 
 * @author Jeff Butler
 * 
 */
public class OperationElementGenerator extends AbstractXmlElementGenerator
{

    private OperationType type;
    
    private String id;

    public OperationElementGenerator(OperationType type,String id)
    {
        super();
        this.type=type;
        this.id=id;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        OperationInfo op = new OperationInfo(id,id,type);
        parentElement.addElement(op.toElement());
        
    }
}
