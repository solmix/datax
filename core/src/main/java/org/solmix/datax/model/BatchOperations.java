/*
 * Copyright 2014 The Solmix Project
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/ 
 * or see the FSF site: http://www.fsf.org. 
 */

package org.solmix.datax.model;

import java.util.ArrayList;
import java.util.List;

import org.solmix.commons.xml.XMLNode;
import org.solmix.datax.repository.builder.XmlParserContext;
import org.solmix.datax.repository.builder.xml.BaseXmlNodeParser;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年6月29日
 */

public class BatchOperations
{

    private List<OperationInfo> operations;

    private TransactionPolicy transactionPolicy;

    public BatchOperations(List<OperationInfo> operations,TransactionPolicy transactionPolicy)
    {
        this.operations = operations;
        this.transactionPolicy=transactionPolicy;
    }
    
    
    public List<OperationInfo> getOperations() {
        return operations;
    }

    
    public TransactionPolicy getTransactionPolicy() {
        return transactionPolicy;
    }

    public static class Parser extends BaseXmlNodeParser<BatchOperations>{

        @Override
        public BatchOperations parse(XMLNode node, XmlParserContext context) {
            List<XMLNode> nodes = node.evalNodes("fetch|add|remove|update|custom");
            if(nodes==null||nodes.size()==0){
                return null;
            }
            String transPolicy= node.getStringAttribute("transactionPolicy");
            TransactionPolicy transactionPolicy=null;
            if(transPolicy!=null){
                transactionPolicy=TransactionPolicy.fromValue(transPolicy);
            }else{
                //所有更改错字加入事物处理
                transactionPolicy=TransactionPolicy.ANY_CHANGE;
            }
            List<OperationInfo> operations = new ArrayList<OperationInfo>(nodes.size());
            for(XMLNode n:nodes){
                OperationInfo.Parser p = new OperationInfo.Parser(true);
                OperationInfo oi=  p.parse(n, context);
                operations.add(oi);
            }
            return new BatchOperations(operations, transactionPolicy);
        }
    }
}
