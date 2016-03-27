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

package org.solmix.datax.attachment;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年7月15日
 */

public class PagedBean implements Pageable
{

    private Integer endRow;

    private Integer startRow;

    private Integer totalRow;

    private Integer batchSize;

    public PagedBean()
    {

    }

    public PagedBean(Integer startRow, Integer endRow)
    {
        this(startRow, endRow, null);
    }

    public PagedBean(Integer startRow, Integer endRow, Integer batchSize)
    {
        this.startRow = startRow;
        this.endRow = endRow;
        this.batchSize = batchSize;
    }

    @Override
    public Integer getEndRow() {
        return endRow;
    }

    @Override
    public void setEndRow(Integer endRow) {
        this.endRow = endRow;
    }

    @Override
    public Integer getStartRow() {
        return startRow;
    }

    @Override
    public void setStartRow(Integer startRow) {
        this.startRow = startRow;
    }

    @Override
    public Integer getTotalRow() {
        return totalRow;
    }

    @Override
    public void setTotalRow(Integer totalRow) {
        this.totalRow = totalRow;
    }

    @Override
    public Integer getBatchSize() {
        return batchSize == null ? endRow - startRow : batchSize;
    }

    @Override
    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

}
