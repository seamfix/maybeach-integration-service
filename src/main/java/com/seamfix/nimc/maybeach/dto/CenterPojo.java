package com.seamfix.nimc.maybeach.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CenterPojo {

    private String code;

    private Double latitude;

    private String name;

    private String mobile;

    private String status;

    private Double longitude;

    private Long geofencingRadiusInMeters;

}
