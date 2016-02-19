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

package org.solmix.datax.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.solmix.commons.annotation.NotThreadSafe;
import org.solmix.commons.xml.XMLNode;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.model.DataServiceInfo;
import org.solmix.datax.model.OperationInfo;
import org.solmix.datax.model.TransformerInfo;
import org.solmix.datax.model.ValidatorInfo;
import org.solmix.datax.repository.builder.ReferenceResolver;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年6月24日
 */
@NotThreadSafe
public class DefaultRepository implements RepositoryService
{

    protected final Set<String> loadedResources = new HashSet<String>();

    protected Map<String,DataServiceInfo> services= new StringMap<DataServiceInfo>("DataServiceInfo Cache");
    
    protected Map<String,ValidatorInfo> validtors= new StringMap<ValidatorInfo>("ValidatorInfo Cache");
    
    protected Map<String,OperationInfo> operations= new StringMap<OperationInfo>("OperationInfo Cache");

    protected Map<String,TransformerInfo> transformers= new StringMap<TransformerInfo>("TransformerInfo Cache");

    protected ConcurrentHashMap<String,DataServiceInfo> drived=  new ConcurrentHashMap<String, DataServiceInfo>();
        
    protected Map<String,XMLNode> fields= new StringMap<XMLNode>("Fields XMLNode Cache");
    
    protected Collection<ReferenceResolver>  referenceResolvers= new LinkedList<ReferenceResolver>();
    private DataServiceManager dataServiceManager;
    /**
     * @param defaultDataServiceManager
     */
    public DefaultRepository(DataServiceManager dataServiceManager)
    {
        this.dataServiceManager=dataServiceManager;
    }

    
    public DataServiceManager getDataServiceManager() {
        return dataServiceManager;
    }

    @Override
    public DataServiceInfo getDataService(String name) {
        return services.get(name);
    }
    
    public void addDataService(DataServiceInfo info){
        services.put(info.getId(), info);
    }

    public void addLoadedResource(String resource) {
        loadedResources.add(resource);
    }

    public boolean isResourceLoaded(String resource) {
        return loadedResources.contains(resource);
    }
    
    protected static class StringMap<V> extends HashMap<String, V> {
        private static final long serialVersionUID = 3241511356840215296L;
        protected static class Ambiguity {
            private String subject;

            public Ambiguity(String subject) {
              this.subject = subject;
            }

            public String getSubject() {
              return subject;
            }
          }
        private String name;

        public StringMap(String name, int initialCapacity, float loadFactor) {
          super(initialCapacity, loadFactor);
          this.name = name;
        }

        public StringMap(String name, int initialCapacity) {
          super(initialCapacity);
          this.name = name;
        }

        public StringMap(String name) {
          super();
          this.name = name;
        }

        public StringMap(String name, Map<String, ? extends V> m) {
          super(m);
          this.name = name;
        }

        @Override
        @SuppressWarnings("unchecked")
        public V put(String key, V value) {
          if (containsKey(key))
            throw new IllegalArgumentException(name + " already contains value for " + key);
          if (key.contains(".")) {
            final String shortKey = getShortName(key);
            if (super.get(shortKey) == null) {
              super.put(shortKey, value);
            } else {
              super.put(shortKey, (V) new Ambiguity(shortKey));
            }
          }
          return super.put(key, value);
        }

        @Override
        public V get(Object key) {
          V value = super.get(key);
          if (value == null) {
            throw new IllegalArgumentException(name + " does not contain value for " + key);
          }
          if (value instanceof Ambiguity) {
            throw new IllegalArgumentException(((Ambiguity) value).getSubject() + " is ambiguous in " + name
                + " (try using the full name including the namespace, or rename one of the entries)");
          }
          return value;
        }

        private String getShortName(String key) {
          final String[] keyparts = key.split("\\.");
          final String shortKey = keyparts[keyparts.length - 1];
          return shortKey;
        }

    }

    public void addInclude(String id, XMLNode node) {
        fields.put(id, node);
    }
    
    public Collection<ReferenceResolver> getReferenceResolvers() {
        return referenceResolvers;
    }
    
    public void addReferenceResolver(ReferenceResolver resolver){
        referenceResolvers.add(resolver);
    }

    public XMLNode getIncludeXMLNode(String includeId) {
        
        return fields.get(includeId);
    }


    /**
     * @param refid
     */
    public ValidatorInfo getValidatorInfo(String refid) {
       return validtors.get(refid);
    }
    public void addValidatorInfo(ValidatorInfo validator) {
         validtors.put(validator.getId(), validator);
     }

    public void addOperationInfo(OperationInfo operation) {
        operations.put(operation.getId(), operation);
    }
    /**
     * @param refid
     * @return
     */
    public OperationInfo getOperationInfo(String refid) {
        return operations.get(refid);
    }

    public void addTransformerInfo(TransformerInfo ti) {
        transformers.put(ti.getId(), ti);
    }

    public TransformerInfo getTransformerInfo(String refid) {
        return transformers.get(refid);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.repository.RepositoryService#getDerivedDataService(java.lang.String)
     */
    @Override
    public DataServiceInfo getDerivedDataService(String name) {
        return drived.get(name);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.datax.repository.RepositoryService#addDerivedDataService(java.lang.String, org.solmix.datax.model.DataServiceInfo)
     */
    @Override
    public DataServiceInfo addDerivedDataService(DataServiceInfo info) {
        String id = info.getId();
        if (!drived.contains(id)) {
            return drived.putIfAbsent(id, info);
        } else {
            return drived.replace(id, info);
        }
    }


    @Override
    public Set<String> getServiceKeys() {
        return services.keySet();
    }
}
