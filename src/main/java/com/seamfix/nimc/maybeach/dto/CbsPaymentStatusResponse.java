package com.seamfix.nimc.maybeach.dto;

public class CbsPaymentStatusResponse extends MayBeachResponse {

	private static final long serialVersionUID = 8206456634770371838L;

	private Object data;
	

	@Override
	public Object getData() {
		return data;
	}

	@Override
	public void setData(Object data) {
		this.data = data;
	}
	
	

}
