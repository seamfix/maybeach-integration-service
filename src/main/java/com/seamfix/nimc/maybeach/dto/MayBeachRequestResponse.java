/**
 * 
 */
package com.seamfix.nimc.maybeach.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Athanasius
 *
 */
@Getter
@Setter
public class MayBeachRequestResponse extends MayBeachResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5088453035314880845L;

	
	private Object data;

	public MayBeachRequestResponse() {
	}

	public MayBeachRequestResponse(int code, String message) {
		super(code, message);
	}
}
