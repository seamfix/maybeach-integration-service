package com.seamfix.nimc.maybeach.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Athanasius
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class CbsDeviceUserLoginRequest implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5773819000919695219L;

    @NotBlank(message = "Please provide the device ID")
    private String deviceId;

    @NotBlank(message = "Please provide the login ID")
    private String loginId;

    @NotBlank(message = "Please provide the password")
    private String password;
}
