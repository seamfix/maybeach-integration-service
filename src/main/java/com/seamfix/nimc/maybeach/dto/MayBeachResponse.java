package com.seamfix.nimc.maybeach.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MayBeachResponse implements Serializable{

	private static final long serialVersionUID = 1898374484684010171L;
	private int status;
	private String message;
	private int code;

	public MayBeachResponse(int status, String message) {
		this.status = status;
		this.message = message;
	}

	public String getMessage() {
		return message==null? "Error processing MAYBEACH request":message;
	}
}
