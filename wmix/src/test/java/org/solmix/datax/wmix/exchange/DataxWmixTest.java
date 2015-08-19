/*
 * Copyright 2015 The Solmix Project
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

package org.solmix.datax.wmix.exchange;

import org.junit.Assert;
import org.junit.Test;
import org.solmix.datax.wmix.AbstractWmixTests;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年8月14日
/   / /*/

public class DataxWmixTest extends AbstractWmixTests
{

    @Test
    public void test() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("\"transactionNum\":2, ");
        sb.append("\"operations\":{");
        sb.append("\"elem\":[");
        sb.append("{");
        sb.append("\"appID\":\"builtinApplication\", ");
        sb.append("\"componentId\":\"ftdrmr_summary_tree\", ");
        sb.append("\"operationId\":\"rmr$RmrSummary_fetch\", ");
        sb.append("\"textMatchStyle\":\"exact\", ");
        sb.append("\"requestId\":\"rmr$RmrSummary_request1\",");
        sb.append("\"action\":\"datax.auth.User.fetch\",");
        sb.append("\"operationType\":\"fetch\", ");
        sb.append("\"values\":{");
        sb.append("\"date\":\"2015-08-16\",");
        sb.append("\"VIEW\":\"4\"");
        sb.append("}");
        sb.append("}");
        sb.append("]");
        sb.append("}");
        sb.append("}");
        prepareServlet("datax");
        Assert.assertNotNull(component);
        invokePost("/datax/datax/1?a=b&d=e",sb.toString());
        controller.service(request, response);
    }
}
