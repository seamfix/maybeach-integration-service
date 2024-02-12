package com.seamfix.nimc.maybeach.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author Athanasius
 */

@Getter
@Setter
public class CbsPaymentStatusRequestV3 implements Serializable {

    private static final long serialVersionUID = 6471048543157193189L;

    private List<String> paymentReferences;

    private String esaCode;

    private List<String> serviceTypes;

}
