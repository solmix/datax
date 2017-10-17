
package org.solmix.generator.codegen.mybatis.xmlmapper;

import static org.solmix.generator.util.Messages.getString;

import org.solmix.commons.xml.dom.Attribute;
import org.solmix.commons.xml.dom.Document;
import org.solmix.commons.xml.dom.Document.Model;
import org.solmix.commons.xml.dom.XmlElement;
import org.solmix.generator.api.FullyQualifiedTable;
import org.solmix.generator.codegen.AbstractXmlGenerator;
import org.solmix.generator.codegen.mybatis.IntrospectedTableDataxImpl;

public class DataServiceXmlMapperGenerator extends AbstractXmlGenerator
{
    
    public static final String ID="http://www.solmix.org/schema/datax-ds/v1.0.0";

    @Override
    public Document getDocument() {

        Document document = new Document(ID, ID);
        document.setModel(Model.XSD);
        document.setRootElement(getDataServiceMapElement());

        if (!domain.getPlugins().sqlMapDocumentGenerated(document, introspectedTable)) {
            document = null;
        }

        return document;

    }
   
    private XmlElement getDataServiceMapElement() {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.12", table.toString()));
        XmlElement answer = new XmlElement("datax");
        XmlElement configuration = new XmlElement("configuration");
        answer.addElement(configuration);
        String namespace = ((IntrospectedTableDataxImpl)introspectedTable).getDataServiceNamespace();
        configuration.addAttribute(new Attribute("namespace", namespace));

        domain.getCommentGenerator().addRootComment(answer);
/*
        addResultMapWithoutBLOBsElement(answer);
        addResultMapWithBLOBsElement(answer);
        addExampleWhereClauseElement(answer);
        addMyBatis3UpdateByExampleWhereClauseElement(answer);
        addBaseColumnListElement(answer);
        addBlobColumnListElement(answer);
        addSelectByExampleWithBLOBsElement(answer);
        addSelectByExampleWithoutBLOBsElement(answer);
        addSelectByPrimaryKeyElement(answer);
        addSelectAllElement(answer);
        addDeleteByPrimaryKeyElement(answer);
        addDeleteByExampleElement(answer);
        addInsertElement(answer);
        addInsertSelectiveElement(answer);
        addCountByExampleElement(answer);
        addUpdateByExampleSelectiveElement(answer);
        addUpdateByExampleWithBLOBsElement(answer);
        addUpdateByExampleWithoutBLOBsElement(answer);
        addUpdateByPrimaryKeySelectiveElement(answer);
        addUpdateByPrimaryKeyWithBLOBsElement(answer);
        addUpdateByPrimaryKeyWithoutBLOBsElement(answer);*/

        return answer;
    }

}
