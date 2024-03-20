package com.seamfix.nimc.maybeach.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class DeviceUserLoginData extends MayBeachResponse {
    private static final long serialVersionUID = 8206154634770371838L;
    private List<DeviceUserLogin> deviceUserLogin;
}
