
package org.solmix.generator.codegen.mybatis.xmlmapper;

import static org.solmix.generator.util.Messages.getString;

import org.solmix.commons.xml.dom.Attribute;
import org.solmix.commons.xml.dom.Document;
import org.solmix.commons.xml.dom.Document.Model;
import org.solmix.commons.xml.dom.XmlElement;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.OperationType;
import org.solmix.generator.api.FullyQualifiedTable;
import org.solmix.generator.codegen.AbstractXmlGenerator;
import org.solmix.generator.codegen.datax.OperationElementGenerator;
import org.solmix.generator.codegen.mybatis.IntrospectedTableDataxImpl;
import org.solmix.generator.codegen.mybatis.xmlmapper.elements.AbstractXmlElementGenerator;

public class DataServiceXmlMapperGenerator extends AbstractXmlGenerator
{
    
    public static final String ID="http://www.solmix.org/schema/datax-ds/v1.0.0";

    @Override
    public Document getDocument() {

        Document document = new Document(ID, ID);
        document.setModel(Model.XSD);
        document.setRootElement(getDataServiceElement());

        if (!domain.getPlugins().sqlMapDocumentGenerated(document, introspectedTable)) {
            document = null;
        }

        return document;

    }
   
    private XmlElement getDataServiceElement() {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.12", table.toString()));
        
        XmlElement answer = new XmlElement("datax");
        domain.getCommentGenerator().addRootComment(answer);
        XmlElement configuration = new XmlElement("configuration");
        answer.addElement(configuration);
        String namespace = ((IntrospectedTableDataxImpl)introspectedTable).getDataServiceNamespace();
        configuration.addAttribute(new Attribute("namespace", namespace));
        
        String serviceId = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
        DataServiceInfo dsi = new DataServiceInfo(serviceId, "mybatis");
        XmlElement dataServiceElement = dsi.toElement();
        configuration.addElement(dataServiceElement);
        addInsertElement(dataServiceElement);
        addInsertSelectiveElement(dataServiceElement);
        addSelectByPrimaryKeyElement(dataServiceElement);
        addSelectAllElement(dataServiceElement);
        addDeleteByPrimaryKeyElement(dataServiceElement);
        addUpdateByPrimaryKeySelectiveElement(dataServiceElement);
        addUpdateByPrimaryKeyWithBLOBsElement(dataServiceElement);
        addUpdateByPrimaryKeyWithoutBLOBsElement(dataServiceElement);
        return answer;
    }
    protected void addUpdateByPrimaryKeySelectiveElement(
        XmlElement parentElement) {
    if (introspectedTable.getRules().generateUpdateByPrimaryKeySelective()) {
        AbstractXmlElementGenerator elementGenerator = new OperationElementGenerator(OperationType.UPDATE,introspectedTable.getUpdateByPrimaryKeySelectiveStatementId());
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }
}

protected void addUpdateByPrimaryKeyWithBLOBsElement(
        XmlElement parentElement) {
    if (introspectedTable.getRules().generateUpdateByPrimaryKeyWithBLOBs()) {
        AbstractXmlElementGenerator elementGenerator = new OperationElementGenerator(OperationType.UPDATE,introspectedTable.getUpdateByPrimaryKeyWithBLOBsStatementId());
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }
}

protected void addUpdateByPrimaryKeyWithoutBLOBsElement(
        XmlElement parentElement) {
    if (introspectedTable.getRules()
            .generateUpdateByPrimaryKeyWithoutBLOBs()) {
        AbstractXmlElementGenerator elementGenerator = new OperationElementGenerator(OperationType.UPDATE,introspectedTable.getUpdateByPrimaryKeyStatementId());

        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }
}
    protected void addDeleteByPrimaryKeyElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateDeleteByPrimaryKey()) {
            AbstractXmlElementGenerator elementGenerator = new OperationElementGenerator(OperationType.REMOVE
                ,introspectedTable.getDeleteByPrimaryKeyStatementId());
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addSelectAllElement(XmlElement parentElement) {
        AbstractXmlElementGenerator elementGenerator = new OperationElementGenerator(OperationType.FETCH,introspectedTable.getSelectAllStatementId());
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addSelectByPrimaryKeyElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateSelectByPrimaryKey()) {
            AbstractXmlElementGenerator elementGenerator = new OperationElementGenerator(OperationType.FETCH,introspectedTable.getSelectByPrimaryKeyStatementId());
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void addInsertSelectiveElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateInsertSelective()) {
            AbstractXmlElementGenerator elementGenerator = new OperationElementGenerator(OperationType.ADD,introspectedTable.getInsertSelectiveStatementId());
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }
    protected void addInsertElement(XmlElement parentElement) {
        if (introspectedTable.getRules().generateInsert()) {
            AbstractXmlElementGenerator elementGenerator = new OperationElementGenerator(OperationType.ADD,introspectedTable.getInsertStatementId());
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }
    
    protected void initializeAndExecuteGenerator(
        AbstractXmlElementGenerator elementGenerator,
        XmlElement parentElement) {
    elementGenerator.setDomain(domain);
    elementGenerator.setIntrospectedTable(introspectedTable);
    elementGenerator.setProgressCallback(progressCallback);
    elementGenerator.setWarnings(warnings);
    elementGenerator.addElements(parentElement);
}

}
