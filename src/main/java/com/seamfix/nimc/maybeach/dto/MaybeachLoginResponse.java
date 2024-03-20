package com.seamfix.nimc.maybeach.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MaybeachLoginResponse extends MayBeachResponse {
    private static final long serialVersionUID = 8206454644770371938L;
    private String error;
    private DeviceUserLoginData data;
}
