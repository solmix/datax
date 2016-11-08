package org.solmix.datax;

import org.solmix.commons.pager.PageControl;
import org.solmix.commons.pager.PageList;
import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.support.DSResponseImpl;

public class DSResponseBuilder {

	private DSRequest request;
	private Status status;
	private Object rawData;
	private PageControl pageControl;
	
	public DSResponseBuilder(DSRequest request ,Status status){
		this.request=request;
		this.status=status;
	}
	
	public static DSResponseBuilder newBuilder(DSRequest request ,Status status){
		return new DSResponseBuilder(request,status);
	}
	
	public static DSResponseBuilder newBuilder(DSRequest request){
		return new DSResponseBuilder(request,Status.STATUS_SUCCESS);
	}
	
	public DSResponseBuilder setStatus(Status status){
		this.status=status;
		return this;
	}
	
	public DSResponseBuilder setRawData(Object rawData){
		this.rawData=rawData;
		return this;
	}
	
	public DSResponseBuilder setPageControl(PageControl control){
		this.pageControl= control;
		return this;
	}
	
	
	@SuppressWarnings("rawtypes")
	public DSResponse build(){
		DSResponseImpl impl = new DSResponseImpl(request,status);
		PageControl pc =request.getAttachment(PageControl.class);
		if(pageControl!=null){
			impl.addAttachment(PageControl.class, pageControl);
		}else{
			if(pc!=null){
				
				if(rawData instanceof PageList ){
					pc.setTotalSize(((PageList)rawData).getTotalSize());
				}
				impl.addAttachment(PageControl.class, pc);
			}
		}
		impl.setRawData(rawData);
		
		return impl;
	}
}
