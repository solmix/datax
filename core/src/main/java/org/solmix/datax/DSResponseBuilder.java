package org.solmix.datax;

import org.solmix.datax.DSResponse.Status;
import org.solmix.datax.support.DSResponseImpl;

public class DSResponseBuilder {

	private DSRequest request;
	private Status status;
	private Object rawData;
	
	public DSResponseBuilder(DSRequest request ,Status status){
		this.request=request;
		this.status=status;
	}
	public static DSResponseBuilder newBuilder(DSRequest request ,Status status){
		return new DSResponseBuilder(request,status);
	}
	
	public DSResponseBuilder setStatus(Status status){
		this.status=status;
		return this;
	}
	
	public DSResponseBuilder setRawData(Object rawData){
		this.rawData=rawData;
		return this;
	}
	
	
	public DSResponse build(){
		DSResponseImpl impl = new DSResponseImpl(request,status);
		impl.setRawData(rawData);
		return impl;
	}
}
