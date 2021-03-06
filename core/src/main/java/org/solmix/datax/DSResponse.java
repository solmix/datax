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

package org.solmix.datax;

import java.util.List;
import java.util.Map;

/**
 * 代表一次DataService(DS)请求返回的结果。
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年6月18日
 */

public interface DSResponse
{

    /**
     * Define the return Status of Datasource.
     * 
     * @author solmix.f@gmail.com
     * @version 110035 2011-3-20
     */
    public enum Status
    {
        /**
       * 
       */
        STATUS_SUCCESS(0) ,
        UNSET(1) ,
        STATUS_FAILURE(-1) ,
        STATUS_AUTHORIZATION_FAILURE(-3) ,
        STATUS_VALIDATION_ERROR(-4) ,
        STATUS_LOGIN_INCORRECT(-5) ,
        STATUS_MAX_LOGIN_ATTEMPTS_EXCEEDED(-6) ,
        STATUS_LOGIN_REQUIRED(-7) ,
        STATUS_LOGIN_SUCCESS(-8) ,
        UPDATE_WITHOUT_PK(-9) ,
        STATUS_TRANSACTION_FAILED(-10);

        private final int value;

        Status(int value)
        {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Status fromValue(int v) {
            for (Status c : Status.values()) {
                if (c.value == v) {
                    return c;
                }
            }
            throw new IllegalArgumentException("illegal dsresponse status");
        }
    }

    
    void setAffectedRows(Integer affected);

    Integer getAffectedRows();

    /**
     * Return the DataSource
     * 
     * @return
     */
    DataService getDataService();

    /**
     * Set the DataService of current DataService response instance.
     * 
     * @param dataSource
     */
    void setDataService(DataService dataService);

    /**
     * filter data by {@link org.solmix.api.datasource.DataSource#getProperties(Object)} 
     * <li>if need original data ,use {@link #getRawData()}
     * <li>if need original  single data ,use {@link #getSingleResult(Class)}
     * <li>if need original list data use {@link #getResultList(Class)}
     * 
     * @return
     */
    Map<Object, Object> getSingleRecord();

    /**
     * filter data by {@link org.solmix.api.datasource.DataSource#getProperties(Object)} 
     * <li>if need original data ,use {@link #getRawData()}
     * <li>if need original  single data ,use {@link #getSingleResult(Class)}
     * <li>if need original list data use {@link #getResultList(Class)}
     * 
     * @return
     */
    List<Map<Object, Object>> getRecordList();

    Status getStatus();

    void setStatus(Status status);

    boolean isSuccess();
    
    /**
     * 当配置forward时，用于控制跳转方向。
     * @return
     */
    String getForward();
    
    void setForward(String forward);

    public Object getRawData();

    /**
     * Setting the raw data to this response.the raw data may be transformed by {@link #getSingleRecord()},
     * {@link #getRecordList()},{@link #getSingleResult(Class)},{@link #getRecordList()}.
     * 
     * @param rawData the rawData to set
     */
    public void setRawData(Object rawData);

    public <T> T getSingleResult(Class<T> type);

    public <T> List<T> getResultList(Class<T> type);

    /**
     * @return
     */
    Object[] getErrors();

    /**
     * @param errors
     */
    void setErrors(Object... errors);
    
    <T>void addAttachment(Class<T> classKey,T instance);
    
    <T> T getAttachment(Class<T> classKey);
    
    Object getAttribute(String name);
    
    /**
    * @param name name==null removed
    * @param value
    */
   void setAttribute(String name,Object value);
}
