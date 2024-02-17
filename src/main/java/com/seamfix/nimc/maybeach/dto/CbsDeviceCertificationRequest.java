package com.seamfix.nimc.maybeach.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class CbsDeviceCertificationRequest implements Serializable {

    private static final long serialVersionUID = -5773819000019695219L;

    @NotBlank(message = "Please provide the device ID")
    private String deviceId;

    private String certifierLoginId;

    private String requestedByLastName;

    private String requestedByFirstName;

    private String requestedByProviderIdentifier;

    private double currentLocationLatitude;

    private double currentLocationLongitude;
}
