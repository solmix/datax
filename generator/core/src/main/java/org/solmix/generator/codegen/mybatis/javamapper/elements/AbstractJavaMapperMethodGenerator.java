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
package org.solmix.generator.codegen.mybatis.javamapper.elements;

import static org.solmix.generator.codegen.mybatis.MyBatis3FormattingUtilities.getRenamedColumnNameForResultMap;
import static org.solmix.commons.util.StringUtils.stringHasValue;

import org.solmix.generator.api.IntrospectedColumn;
import org.solmix.generator.api.java.FullyQualifiedJavaType;
import org.solmix.generator.api.java.Interface;
import org.solmix.generator.api.java.Method;
import org.solmix.generator.codegen.AbstractGenerator;
import org.solmix.generator.config.GeneratedKey;

/**
 * 
 * @author Jeff Butler
 */
public abstract class AbstractJavaMapperMethodGenerator extends
        AbstractGenerator {
    public abstract void addInterfaceElements(Interface interfaze);

    public AbstractJavaMapperMethodGenerator() {
        super();
    }
    
    protected String getResultAnnotation(Interface interfaze, IntrospectedColumn introspectedColumn,
            boolean idColumn, boolean constructorBased) {
        StringBuilder sb = new StringBuilder();
        if (constructorBased) {
            interfaze.addImportedType(introspectedColumn.getFullyQualifiedJavaType());
            sb.append("@Arg(column=\""); 
            sb.append(getRenamedColumnNameForResultMap(introspectedColumn));
            sb.append("\", javaType="); 
            sb.append(introspectedColumn.getFullyQualifiedJavaType().getShortName());
            sb.append(".class"); 
        } else {
            sb.append("@Result(column=\""); 
            sb.append(getRenamedColumnNameForResultMap(introspectedColumn));
            sb.append("\", property=\""); 
            sb.append(introspectedColumn.getJavaProperty());
            sb.append('\"');
        }

        if (stringHasValue(introspectedColumn.getTypeHandler())) {
            FullyQualifiedJavaType fqjt =
                    new FullyQualifiedJavaType(introspectedColumn.getTypeHandler());
            interfaze.addImportedType(fqjt);
            sb.append(", typeHandler="); 
            sb.append(fqjt.getShortName());
            sb.append(".class"); 
        }

        sb.append(", jdbcType=JdbcType."); 
        sb.append(introspectedColumn.getJdbcTypeName());
        if (idColumn) {
            sb.append(", id=true"); 
        }
        sb.append(')');

        return sb.toString();
    }

    protected void addGeneratedKeyAnnotation(Method method, GeneratedKey gk) {
        StringBuilder sb = new StringBuilder();
        IntrospectedColumn introspectedColumn = introspectedTable.getColumn(gk.getColumn());
        if (introspectedColumn != null) {
            if (gk.isJdbcStandard()) {
                sb.append("@Options(useGeneratedKeys=true,keyProperty=\""); 
                sb.append(introspectedColumn.getJavaProperty());
                sb.append("\")"); 
                method.addAnnotation(sb.toString());
            } else {
                FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
                sb.append("@SelectKey(statement=\""); 
                sb.append(gk.getRuntimeSqlStatement());
                sb.append("\", keyProperty=\""); 
                sb.append(introspectedColumn.getJavaProperty());
                sb.append("\", before="); 
                sb.append(gk.isIdentity() ? "false" : "true");  //$NON-NLS-2$
                sb.append(", resultType="); 
                sb.append(fqjt.getShortName());
                sb.append(".class)"); 
                method.addAnnotation(sb.toString());
            }
        }
    }

    protected void addGeneratedKeyImports(Interface interfaze, GeneratedKey gk) {
        IntrospectedColumn introspectedColumn = introspectedTable.getColumn(gk.getColumn());
        if (introspectedColumn != null) {
            if (gk.isJdbcStandard()) {
                interfaze.addImportedType(
                        new FullyQualifiedJavaType("org.apache.ibatis.annotations.Options")); 
            } else {
                interfaze.addImportedType(
                        new FullyQualifiedJavaType("org.apache.ibatis.annotations.SelectKey")); 
                FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
                interfaze.addImportedType(fqjt);
            }
        }
    }
}
