
package org.solmix.datax.wmix;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.call.DSCall;
import org.solmix.datax.call.DSCallFactory;
import org.solmix.datax.model.MergedType;
import org.solmix.datax.model.OperationType;
import org.solmix.datax.model.TransactionPolicy;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.invoker.Invoker;

public class DataxInvoker implements Invoker
{
    @Override
    public Object invoke(Exchange exchange, Object o) {
        DataServiceManager manager = exchange.get(DataServiceManager.class);
        DSCallFactory factory = manager.getDSCallFactory();
        try {
            if (o instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<DSRequest> requests = (List<DSRequest>) o;
                TransactionPolicy policy = exchange.get(TransactionPolicy.class);
                MergedType merged = exchange.get(MergedType.class);
                if (policy == null) {
                    policy = TransactionPolicy.ANY_CHANGE;
                }
                if (merged == null) {
                    merged = MergedType.SIMPLE;
                }
                DSCall dsc = factory.createDSCall(policy);
                dsc.setExceptionBroken(false);
                switch (merged) {
                    case SIMPLE:
                    case WRAPPED:

                        for (DSRequest request : requests) {
                            dsc.execute(request);
                        }
                        return dsc.getMergedResponse(merged);
                    case ARRAY:
                        List<DSResponse> respons = new ArrayList<DSResponse>();
                        for (DSRequest request : requests) {
                            respons.add(dsc.execute(request));
                        }
                        return respons;
                    case MAPED:
                        Map<String, DSResponse> map = new LinkedHashMap<String, DSResponse>();
                        for (DSRequest request : requests) {
                            map.put(request.getOperationId(), dsc.execute(request));
                        }
                        return map;
                }

            } else if (o instanceof DSRequest) {
                DSRequest req = ((DSRequest)o);
                DSResponse response= req.execute();
                //删除时只返回失败或者成功的结果
                if(req.getOperationInfo().getType()==OperationType.REMOVE){
                    response.setRawData(null);
                }
                return response;
            } else if (o != null) {
                throw new IllegalArgumentException("Illegal message type:" + o.getClass().getName());
            }
        } catch (DSCallException e) {
            throw new Fault("Invoke DSCall exception", e);
        }
        return null;
    }
}
