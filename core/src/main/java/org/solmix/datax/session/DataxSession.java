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
package org.solmix.datax.session;

import java.util.List;

import org.solmix.commons.annotation.NotThreadSafe;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.Pageable;
import org.solmix.datax.model.TransactionPolicy;


/**
 * DataService 模板
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月22日
 */
@NotThreadSafe
public interface DataxSession
{
    
    <T> T fetchOne(String statement);
    
    <T> T fetchOne(String statement, Object parameter);
    
    <E> List<E> fetchList(String statement);
    
    <E> List<E> fetchList(String statement, Object parameter);

    <E> List<E> fetchList(String statement, Object parameter,Pageable page);
    
    
    /**
     * 提供一种简单的事物实现机制：调用begin后，后续操作根据事物策略加入DSCall中，如果执行
     * 过程中抛错，自动回滚。不允许嵌套调用，在没抛错或者调用end()前不能再调用begin，
     * 如果要嵌套使用参考{@link #execute(List, TransactionPolicy)}<br>
     * <code>
     * begin();<br>
     * add();<br>
     * update();<br>
     * .<br>
     * .<br>
     * end();<br>
     * </code>
     * begin()和 end()必须成对出现
     */
    void begin(TransactionPolicy  policy);
    
    /**
     * begin()和 end()必须成对出现
     */
    void end();
    
    List<DSResponse> execute(List<DSRequest> requests);
    List<DSResponse> executeXA(List<DSRequest> requests,TransactionPolicy policy);
    
    DataServiceManager getDataServiceManager();
}
