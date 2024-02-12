package com.seamfix.nimc.maybeach.dto;

import java.io.Serializable;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;


/**
 * @author Athanasius
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Fingers implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7105658203525821952L;

	@NotBlank(message = "Please provide the center code")
	private String base64FingerWsq;
	
    @Min(value = 1, message = "finger position must be a positive value")
    @NotNull(message = "Please provide the finger position")
	private int fingerPosition;

}
