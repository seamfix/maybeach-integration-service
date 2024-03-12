package com.seamfix.nimc.maybeach.dto;

import java.io.Serializable;


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
public class CbsDeviceActivationRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5773819040019695219L;

	private String machineTag;

	private String providerDeviceIdentifier;

	private Double activationLocationLatitude;

	private Double activationLocationLongitude;

	private String requesterEmail;

	private String requesterLastname;

	private String requesterFirstname;

	private String requesterPhoneNumber;

	private String requesterNin;

	private String esaCode;

	private String esaName;

	private String requestId;

	private String location="";
	private String imei="";
	private String model="";

}
