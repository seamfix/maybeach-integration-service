package com.seamfix.nimc.maybeach.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FetchActivationResponse extends MayBeachResponse {
    private static final long serialVersionUID = 8206454634770371938L;
    private List<Object> errors;
    @JsonProperty("data")
    private FetchActivationData activationData;

}


