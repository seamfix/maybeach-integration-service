package com.seamfix.nimc.maybeach.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class DeviceActivationResultPojo {
    private String status;

    private DeviceActivationDataPojo result;

}
