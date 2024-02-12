package com.seamfix.nimc.maybeach.dto;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Athanasius
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class CbsPreEnrollmentCheckRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5773819040019695219L;

	@NotNull(message = "Please provide the fingers")
	private List<Fingers> fingers;

	private String requestTimestamp;

	@NotBlank(message = "Please provide the request transaction ref")
	private String requestTransactionRef;

	@NotBlank(message = "Please provide the device ID")
	private String deviceId;

	private String trackingId;

	private String nimcSearchType;

	private String firstName;

	private String lastName;

	private String dob;

	private String gender;

	private String searchFieldValue;

	public boolean validateDemographicsParams() {
		return StringUtils.isAnyEmpty(firstName, lastName, gender, dob, deviceId);
	}

	public boolean validatePhoneParams() {
		return StringUtils.isAnyEmpty(searchFieldValue, deviceId);
	}
}
