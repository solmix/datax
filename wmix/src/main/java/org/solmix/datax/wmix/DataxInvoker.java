
package org.solmix.datax.wmix;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.datax.DSCallException;
import org.solmix.datax.DSRequest;
import org.solmix.datax.DSResponse;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.DataServiceManager;
import org.solmix.datax.NestedDSCallException;
import org.solmix.datax.model.MergedType;
import org.solmix.datax.model.OperationType;
import org.solmix.datax.util.DataTools;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.invoker.Invoker;
import org.solmix.runtime.Container;
import org.solmix.runtime.transaction.TransactionCallback;
import org.solmix.runtime.transaction.TransactionManager;
import org.solmix.runtime.transaction.TransactionPolicy;
import org.solmix.runtime.transaction.TransactionState;
import org.solmix.runtime.transaction.TransactionSupport;
import org.solmix.runtime.transaction.support.IllegalTransactionStateException;

public class DataxInvoker implements Invoker
{
	private static final Logger LOG = LoggerFactory.getLogger(DataxInvoker.class);
    @Override
    public Object invoke(Exchange exchange, Object o) {
        DataServiceManager manager = exchange.get(DataServiceManager.class);
        try {
            if (o instanceof DSRequest) {
                DSRequest req = ((DSRequest)o);
                DSResponse response= req.execute();
                //删除时只返回失败或者成功的结果
                if(req.getOperationInfo().getType()==OperationType.REMOVE){
                    response.setRawData(null);
                }
                return response;
            }else if (o instanceof List<?>) {
            	 @SuppressWarnings("unchecked")
                 List<DSRequest> requests = (List<DSRequest>) o;
                 //如果没有传入事务策略不使用事务
                 TransactionPolicy policy = exchange.get(TransactionPolicy.class);
                 MergedType merged = exchange.get(MergedType.class);
                 if (merged == null) {
                     merged = MergedType.SIMPLE;
                 }
                 exchange.getContainer();
                 switch (merged) {
                     case SIMPLE:
                     case WRAPPED:
                     	if(policy!=null){
                     		return excuteRequestsInTransaction(exchange,manager, requests, merged);
                     	}else{
                     		return excuteRequests(manager, requests, merged);
                     	}
                     	
                        
                     case ARRAY:
                     	if(policy!=null){
                     		return excuteArraysInTransaction(exchange,manager, requests, merged);
                     	}else{
                     		return excuteArrays(manager, requests, merged);
                     	}
                     case MAPED:
                     	if(policy!=null){
                     		return excuteMapedInTransaction(exchange,manager, requests, merged);
                     	}else{
                     		return excuteMaped(manager, requests, merged);
                     	}
                 }

            } else if (o != null) {
                throw new IllegalArgumentException("Illegal message type:" + o.getClass().getName());
            }
        } catch (DSCallException e) {
            throw new Fault("Invoke DSCall exception", e);
        }
        return null;
    }
    
    /**
     * 批量执行任务，如果不使用事务，抛错后可以继续执行后续任务
     * @param requests
     * @param usedTransaction
     * @return
     * @throws DSCallException
     */
    public DSResponse excuteRequests(DataServiceManager manager ,List<DSRequest> requests,MergedType merged) throws DSCallException{
		Map<DSRequest,DSResponse> responses = new LinkedHashMap<DSRequest,DSResponse>(requests.size());
		for(DSRequest req:requests){
				DSResponse res;
				try {
					res = req.execute();
				} catch (Exception e) {
					res=manager.createDsResponse(req);
					res.setStatus(Status.STATUS_FAILURE);
					res.setErrors(e.getMessage());
				}
				responses.put(req, res);
			
		}
		return DataTools.getMergedResponse(responses, merged);

    }
    public List<DSResponse> excuteArrays(DataServiceManager manager ,List<DSRequest> requests,MergedType merged) throws DSCallException{
    	List<DSResponse>  responses = new ArrayList<DSResponse>(requests.size());
		for(DSRequest req:requests){
				DSResponse res;
				try {
					res = req.execute();
				} catch (Exception e) {
					res=manager.createDsResponse(req);
					res.setStatus(Status.STATUS_FAILURE);
					res.setErrors(e.getMessage());
				}
				responses.add( res);
			
		}
		return responses;

    }
    public Map<String,DSResponse> excuteMaped(DataServiceManager manager ,List<DSRequest> requests,MergedType merged) throws DSCallException{
    	Map<String,DSResponse> responses = new LinkedHashMap<String,DSResponse>(requests.size());
		for(DSRequest req:requests){
				DSResponse res;
				try {
					res = req.execute();
				} catch (Exception e) {
					res=manager.createDsResponse(req);
					res.setStatus(Status.STATUS_FAILURE);
					res.setErrors(e.getMessage());
				}
				responses.put(req.getOperationId(), res);
			
		}
		return responses;

    }
    
    public DSResponse excuteRequestsInTransaction(Exchange exchange ,DataServiceManager manager ,final List<DSRequest> requests,final MergedType merged) throws DSCallException{
		Container c=exchange.getContainer();
		TransactionManager tm = c.getExtension(TransactionManager.class);
		if(tm!=null){
			LOG.debug("Used container dependency TransactionManager as request transaction manager");
			TransactionSupport txSupport  = new TransactionSupport(tm);
			return txSupport.execute(new TransactionCallback<DSResponse>() {

				@Override
				public DSResponse doInTransaction(TransactionState status) {
					Map<DSRequest,DSResponse> responses = new LinkedHashMap<DSRequest,DSResponse>(requests.size());
					try {
						for(DSRequest req:requests){
						DSResponse res = req.execute();
						responses.put(req, res);
						}
					} catch (DSCallException e) {
						throw new NestedDSCallException("transaction request error", e);
					}
					return DataTools.getMergedResponse(responses, merged);
				}
			});
		}else{
			throw new IllegalTransactionStateException("No transaction manager founded");
		}
    }
    
    public List<DSResponse> excuteArraysInTransaction(Exchange exchange ,DataServiceManager manager ,final List<DSRequest> requests,final MergedType merged) throws DSCallException{
		Container c=exchange.getContainer();
		TransactionManager tm = c.getExtension(TransactionManager.class);
		if(tm!=null){
			LOG.debug("Used container dependency TransactionManager as request transaction manager");
			TransactionSupport txSupport  = new TransactionSupport(tm);
			return txSupport.execute(new TransactionCallback<List<DSResponse>>() {

				@Override
				public List<DSResponse> doInTransaction(TransactionState status) {
					List<DSResponse>  responses = new ArrayList<DSResponse>(requests.size());
					try {
						for(DSRequest req:requests){
						DSResponse res = req.execute();
						responses.add( res);
						}
					} catch (DSCallException e) {
						throw new NestedDSCallException("transaction request error", e);
					}
					return responses;
				}
			});
		}else{
			throw new IllegalTransactionStateException("No transaction manager founded");
		}
    }
    public Map<String,DSResponse> excuteMapedInTransaction(Exchange exchange ,DataServiceManager manager ,final List<DSRequest> requests,final MergedType merged) throws DSCallException{
		Container c=exchange.getContainer();
		TransactionManager tm = c.getExtension(TransactionManager.class);
		if(tm!=null){
			LOG.debug("Used container dependency TransactionManager as request transaction manager");
			TransactionSupport txSupport  = new TransactionSupport(tm);
			return txSupport.execute(new TransactionCallback<Map<String,DSResponse>>() {

				@Override
				public Map<String,DSResponse> doInTransaction(TransactionState status) {
					Map<String,DSResponse> responses = new LinkedHashMap<String,DSResponse>(requests.size());
					try {
						for(DSRequest req:requests){
						DSResponse res = req.execute();
						responses.put(req.getOperationId(), res);
						}
					} catch (DSCallException e) {
						throw new NestedDSCallException("transaction request error", e);
					}
					return responses;
				}
			});
		}else{
			throw new IllegalTransactionStateException("No transaction manager founded");
		}
    }
    
}
