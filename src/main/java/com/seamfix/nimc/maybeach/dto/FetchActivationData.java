package com.seamfix.nimc.maybeach.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class FetchActivationData implements Serializable {
    private static final long serialVersionUID = 8206454534370371838L;
    @JsonProperty("fetchActivationData")
    protected List<ActivationData> activationDataList;
}
