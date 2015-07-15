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
package org.solmix.datax.support;

import org.solmix.datax.Pageable;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年7月15日
 */

public class PagedBean implements Pageable
{
    private Integer endRow;

    private Integer startRow;
    
    private Integer totalRow;

    
    public Integer getEndRow() {
        return endRow;
    }

    
    public void setEndRow(Integer endRow) {
        this.endRow = endRow;
    }

    
    public Integer getStartRow() {
        return startRow;
    }

    
    public void setStartRow(Integer startRow) {
        this.startRow = startRow;
    }

    
    public Integer getTotalRow() {
        return totalRow;
    }

    
    public void setTotalRow(Integer totalRow) {
        this.totalRow = totalRow;
    }
    

}
