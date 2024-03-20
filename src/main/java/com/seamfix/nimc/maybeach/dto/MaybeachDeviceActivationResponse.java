package com.seamfix.nimc.maybeach.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MaybeachDeviceActivationResponse extends MayBeachResponse {
    private static final long serialVersionUID = 8206454534770371838L;
    private String error;
    @JsonProperty("data")
    private OnboardingDeviceData deviceActivationData;
}
