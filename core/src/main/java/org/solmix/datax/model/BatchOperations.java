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
import org.solmix.datax.repository.builder.XmlNodeParserProvider;
import org.solmix.datax.repository.builder.XmlParserContext;
import org.solmix.datax.repository.builder.xml.BaseXmlNodeParser;
import org.solmix.runtime.transaction.TransactionPolicy;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年6月29日
 */

public class BatchOperations
{

    private List<OperationInfo> operations;

    private TransactionPolicy transactionPolicy;
    
    private MergedType mergedType;
    
    protected List<ForwardInfo> forwards;

    public BatchOperations(List<OperationInfo> operations,TransactionPolicy transactionPolicy,MergedType mergedType)
    {
        this.operations = operations;
        this.transactionPolicy=transactionPolicy;
        this.mergedType=mergedType;
    }
    
    
    public List<OperationInfo> getOperations() {
        return operations;
    }

    
    /**
     * 如果为空使用{@link TransactionPolicy.ANY_CHANGE}
     * @return
     */
    public TransactionPolicy getTransactionPolicy() {
        return transactionPolicy;
    }
    
    public MergedType getMergedType() {
        return mergedType;
    }

    public List<ForwardInfo> getForwards() {
        return forwards;
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
                transactionPolicy=TransactionPolicy.REQUIRED;
            }
            String merged= node.getStringAttribute("merged");
            MergedType mergedType=null;
            if(merged!=null){
                mergedType=MergedType.fromValue(merged);
            }
            List<OperationInfo> operations = new ArrayList<OperationInfo>(nodes.size());
            for(XMLNode n:nodes){
                OperationInfo.Parser p = new OperationInfo.Parser(true);
                OperationInfo oi=  p.parse(n, context);
                operations.add(oi);
            }
            BatchOperations batch= new BatchOperations(operations, transactionPolicy,mergedType);
            
            List<ForwardInfo> forwards=parseForwards(node.evalNodes("forward"), context);
            batch.forwards=forwards;
            return batch;
        }
        protected List<ForwardInfo> parseForwards(List<XMLNode> nodes, XmlParserContext context) {
            if(nodes==null||nodes.isEmpty()){
                return null;
            }
            List<ForwardInfo> forwards = new ArrayList<ForwardInfo>(nodes.size());
            for(XMLNode node :nodes){
                forwards.add(context.parseNode(XmlNodeParserProvider.FORWARD,node, ForwardInfo.class));
            }
            return forwards;
        }
    }
}
