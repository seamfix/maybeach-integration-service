package com.seamfix.nimc.maybeach.dto;

import lombok.Data;

@Data
public class FetchActivationDataRequest {
    private String deviceId;
    private String requestId;

    public FetchActivationDataRequest(String deviceId, String requestId) {
        this.deviceId = deviceId;
        this.requestId = requestId;
    }
}

