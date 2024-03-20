package com.seamfix.nimc.maybeach.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MayBeachClientAppUserResponse extends MayBeachResponse{

	private static final long serialVersionUID = 1898374484684010171L;
	private int status;
	private String message;
	private Object data;
	private int code;

	@Override
	public String getMessage() {
		return message == null? "Error processing MAYBEACH request": message;
	}

	public MayBeachClientAppUserResponse(int status, String message, int code) {
		super(status, message, code);
	}
}
