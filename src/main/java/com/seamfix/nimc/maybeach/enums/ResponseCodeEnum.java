package com.seamfix.nimc.maybeach.enums;

import lombok.Getter;

@Getter
public enum ResponseCodeEnum {
    ERROR(-1, "Error getting payment status"),
    PROCEED(1, "Proceed with enrollment"),
    VALIDATION_ERROR(40, "Validation Error"),
    WRONG_USE_CASE(2, "Payment reference is tied to a different use case"),
    USED(3, "Payment reference has been used"),
    PROCEED_BUT_MISSING_SERVICE_TYPES(4, "Proceed with enrollment. You have the following service types left: {}"),
    UNPAID_SERVICE_TYPES(5, "You have selected the following service types you did not pay for: {}"),
    LOCKED(6, "One or more payment references (RRRs) are already locked to a node"),
    UNKNOWN(7, "Unknown message from MAYBEACH"),
    UNABLE_TO_REACH_MAYBEACH(8, "We are unable to establish a connection to a remote service at this time, please try again later or contact support.");

    private int code;
    private String description;

    ResponseCodeEnum(int code, String description){
        this.code = code;
        this.description = description;
    }
}
