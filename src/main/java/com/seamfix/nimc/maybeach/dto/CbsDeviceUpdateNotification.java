/**
 * 
 */
package com.seamfix.nimc.maybeach.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Athanasius
 *
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CbsDeviceUpdateNotification {
	private double currentLocationLatitude;
	private double currentLocationLongitude;

	@NotBlank(message = "Please provide the centerCode")
	@NotNull(message = "Please provide the centerCode")
	private String centerCode;

	@NotBlank(message = "Please provide the esaCode")
	@NotNull(message = "Please provide the esaCode")
	private String esaCode;

	@NotBlank(message = "Please provide the requestId")
	@NotNull(message = "Please provide the requestId")
	private String requestId;

	@NotBlank(message = "Please provide the status")
	@NotNull(message = "Please provide the status")
	private String status; 
	
	private String dateModified; 
}
