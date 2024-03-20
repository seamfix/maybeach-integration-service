package com.seamfix.nimc.maybeach.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class OnboardingDeviceData implements Serializable {
    private static final long serialVersionUID = 8204454534770371838L;
    @JsonProperty("onboardingDeviceRequest")
    private List<OnboardingDeviceRequest> onboardingDeviceRequestList;
}
