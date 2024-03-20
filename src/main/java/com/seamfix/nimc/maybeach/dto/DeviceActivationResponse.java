package com.seamfix.nimc.maybeach.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class DeviceActivationResponse extends MayBeachResponse {
    private static final long serialVersionUID = 8206454634770371838L;
    private List<Object> errors;
    private List data;

    public DeviceActivationResponse(int status, String message){
        super(status, message);
    }
}
