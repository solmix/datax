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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.solmix.commons.annotation.Immutable;
import org.solmix.commons.xml.XMLNode;
import org.solmix.datax.repository.builder.BuilderException;
import org.solmix.datax.repository.builder.ReferenceNoFoundException;
import org.solmix.datax.repository.builder.ReferenceResolver;
import org.solmix.datax.repository.builder.XmlNodeParserProvider;
import org.solmix.datax.repository.builder.XmlParserContext;
import org.solmix.datax.repository.builder.xml.BaseXmlNodeParser;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年6月26日
 */
@Immutable
public class OperationInfo
{
    protected String id;
    
    protected String localId;
    protected OperationType type;
    
    protected Boolean autoJoinTransactions;
    
    protected Boolean validate;
    
    protected BatchOperations batch;
    
    protected List<TransformerInfo> transformers;
    
    protected InvokerInfo invoker;
    
    protected String refid;
    protected static int COUNT=0;

    private Map<String, ParamInfo> params;
    
    protected XMLNode node;
    
    OperationInfo()
    {
    }

    /**
     * 在同一个DataService中id不能重复。
     * 
     * @param id
     */
    public OperationInfo(String id,String localId,OperationType type){
        if(id==null){
            id=this.getClass().getName()+"#"+(++COUNT);
        }
        this.id=id;
        this.localId=localId;
        this.type=type;
    }
    
    
    public String getLocalId() {
        return localId;
    }

    /**
     * @param oi
     */
    public OperationInfo(String id,OperationInfo oi)
    {
        copy(oi, this);
        this.id=id;
    }
    
    public Boolean getValidate() {
        return validate;
    }

    public String getId() {
        return id;
    }
    

    public String getRefid() {
        return refid;
    }
    public OperationType getType() {
        return type;
    }

    public Object getProperty(String key) {
        if(getXMLNode()!=null){
            return getXMLNode().getStringAttribute(key);
        }
        return null;
    }
   
    public XMLNode getXMLNode() {
        return node;
    }

    public Boolean getAutoJoinTransactions() {
        return autoJoinTransactions;
    }

    public BatchOperations getBatch() {
        return batch;
    }

    
    public List<TransformerInfo> getTransformers() {
        return transformers;
    }
    
    public InvokerInfo getInvoker() {
        return invoker;
    }

    private static void copy(OperationInfo source,OperationInfo target){
        target.type=source.type;
        target.autoJoinTransactions=source.autoJoinTransactions;
        target.validate=source.validate;
        target.params=source.params;
        target.invoker=source.invoker;
        target.transformers=source.transformers;
        target.node=source.node;
    }
    public Map<String, ParamInfo> getParams() {
        return params;
    }
    public static class OperationInfoResolver extends OperationInfo implements ReferenceResolver{

        private final String ref;
        private XmlParserContext context;
//        private String serviceid;
        public OperationInfoResolver(String id,String serviceid,OperationType type,String refid,XmlParserContext context){
            this.ref=refid;
            this.context=context;
            this.id=id;
            this.type=type;
        }
        
        @Override
        public void resolve() {
            OperationInfo vi= context.getOperationInfo(ref);
            if(vi.type!=type){
                throw new BuilderException("operation:"+id+" type is "+type+" but ref:"+vi.id+" type is "+vi.type);
            }
            refid=vi.getId();
            copy(vi, this);
            context.getRepositoryService().addOperationInfo(this);
        }
        
        @Override
        public String toString(){
            return new StringBuilder().append("Resolver Operation:").append(refid).toString();
        }

        
    }
    public static class Parser extends BaseXmlNodeParser<OperationInfo>{

        /**当operation中配置了batch时，需要将该值设为true*/
        private boolean batch;
        public Parser(){
            this(false);
        }
        /**
         * 当operation中配置了batch时，需要将该值设为true<br>
         * batch下的配置的operation不能直接被调用，只用通过<service><operations>...</operations></service>
         * 中配置的才能被外部调用
         * */
        public Parser(boolean batch){
            this.batch=batch;
        }
        @Override
        public OperationInfo parse(XMLNode node, XmlParserContext context) {
            String refid= node.getStringAttribute("refid");
            String localId= node.getStringAttribute("id");
            String strType = node.getName();
            //如果没有设置约定为类型名，约定大于配置。
            String id=null;
            if(!batch){
                if(localId==null){
                    localId=strType;
                }if(!validateId(localId)){
                    throw new BuilderException("Invalid Operation id  ("+localId+") at:"+node.getPath());
                }
                id=context.applyCurrentService(localId, false);
            }
            OperationType type = OperationType.fromValue(strType);
            
            OperationInfo oi= new OperationInfo(id,localId,type);
            if(refid!=null){
                OperationInfo refoi = null;
                try {
                    refid=parseRefid(refid, context);
                    refoi = context.getOperationInfo(refid);
                } catch (ReferenceNoFoundException e) {
                    OperationInfoResolver v = new OperationInfoResolver(id,context.getCurrentService(),type,refid, context);
                    context.getRepositoryService().addReferenceResolver(v);
                    return v;
                }
                if(refoi!=null){
                    //类型不一致
                    if(refoi.type!=oi.type){
                        throw new BuilderException("operation:"+oi.id+" type is "+oi.type+" but ref:"+refoi.id+" type is "+refoi.type);
                    }
                    oi.refid=refoi.getId();
                    copy(refoi, oi);
                    //batch中配置的不能被引用，不要加入引用列表。
                    if(!batch){
                        context.getRepositoryService().addOperationInfo(oi);
                    }
                    return oi;
                }else{
                    throw new BuilderException("Can't found operation ref:"+context.applyCurrentService(refid, true));
                }
            }
            
            
            Boolean autoJoinTransactions= node.getBooleanAttribute("autoJoinTransactions");
            Boolean validate= node.getBooleanAttribute("validate");
            Map<String ,ParamInfo> params = parseParams(node.evalNode("params"), context);
            List<TransformerInfo> transformers=parseTransformers(node.evalNodes("transformer"), context);
            BatchOperations batch= parseBatch(node.evalNode("batch"),context);
            InvokerInfo invoker = parseInvoker(node.evalNode("invoker"),context);
            oi.autoJoinTransactions=autoJoinTransactions;
            oi.node=node;
            oi.params=params;
            oi.batch=batch;
            oi.transformers=transformers;
            oi.invoker=invoker;
            oi.validate=validate;
            context.getRepositoryService().addOperationInfo(oi);
            return oi;
        }

        protected List<TransformerInfo> parseTransformers(List<XMLNode> nodes, XmlParserContext context) {
            if(nodes==null||nodes.isEmpty()){
                return null;
            }
            List<TransformerInfo> transformers = new ArrayList<TransformerInfo>(nodes.size());
            for(XMLNode node :nodes){
                transformers.add(context.parseNode(XmlNodeParserProvider.TRANSFORMER,node, TransformerInfo.class));
            }
            return transformers;
        }
        
        protected BatchOperations parseBatch(XMLNode node, XmlParserContext context) {
            if(node==null){
                return null;
            }
            return context.parseNode(XmlNodeParserProvider.BATCH,node, BatchOperations.class);
        }
        
        protected InvokerInfo parseInvoker(XMLNode node, XmlParserContext context) {
            if(node==null){
                return null;
            }
            return context.parseNode(XmlNodeParserProvider.INVOKER,node, InvokerInfo.class);
        }
        
        protected Map<String, ParamInfo> parseParams(XMLNode node, XmlParserContext context) {
            if(node==null){
                return null;
            }
            //默认请求中的参数优先，配置的参数不覆盖请求中的。
            Boolean override = node.getBooleanAttribute("isOverride");
            List<XMLNode> nodes= node.evalNodes("param");
            Map<String, ParamInfo> params = new LinkedHashMap<String, ParamInfo>(nodes.size());
            for(XMLNode n:nodes){
                ParamInfo  param= context.parseNode(XmlNodeParserProvider.PARAM,n, ParamInfo.class);
                if(param.getIsOverride()==null&&override!=null){
                    param = new ParamInfo(override,param,n);
                }
                params.put(param.getKey(), param);
            }
            return params;
        }
        
    }
   
}
