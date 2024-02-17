package com.seamfix.nimc.maybeach.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class DeviceActivationDataPojo {


    private CenterPojo center;

    private DevicePojo device;

    private FepPojo fep;

    private BackendDataCredential testBackendCredential;

    private BackendDataCredential prodBackendCredential;

    @Getter
    @Setter
    private class BackendDataCredential {
        private String backendWsUsername;
        private String backendWsPassword;
    }

}
