package com.seamfix.nimc.maybeach.services.payment;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seamfix.nimc.maybeach.configs.AppConfig;
import com.google.gson.JsonSyntaxException;
import com.seamfix.nimc.maybeach.dto.MayBeachRequestResponse;
import com.seamfix.nimc.maybeach.dto.MayBeachResponse;
import com.seamfix.nimc.maybeach.services.jms.JmsSender;
import com.seamfix.nimc.maybeach.utils.Sha512Impl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seamfix.nimc.maybeach.dto.CbsPaymentStatusResponse;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;

@Slf4j
@Component
@EnableAsync
public class MayBeachService {

	@Autowired
	protected RestTemplate restTemplate;

	@Autowired
	protected JmsSender jmsSender;

	@Autowired
	protected AppConfig appConfig;

	@Autowired
	private ObjectMapper objectMapper;
	protected static final String CODE_STR = "code";
	protected static final String DATA = "data";
	protected static final String X_ACCOUNT_ID = "X-ACCOUNT-ID";
	protected static final String SIGNATURE = "Signature";
	protected static final String MESSAGE = "message";
	protected static final String X_DEVICE_ID = "X-DEVICE-ID";
	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	public String validateRequestParams(Object request) {
		if(request == null) {
			return "Invalid Request";
		}
		Set<ConstraintViolation<Object>> violations = validator.validate(request, Default.class);
		if (!violations.isEmpty()) {
			return violations.iterator().next().getMessage();
		}
		return null;
	}
	
	public String mapToJsonString(Map<String, Object> inputMap) {
		String jsonString = "";
        try {
            jsonString = objectMapper.writeValueAsString(inputMap);
        } catch (IOException e) {
        	log.error("Error converting to Json String ", e);
        }
        
        return jsonString;
	}
	
	public Object mapToObject(Map<String, String> inputMap, Class clazz) {
		return objectMapper.convertValue(inputMap, clazz);
	}
	
	public Map<String, Object> convertObjectToMap (Object object) {
		return objectMapper.convertValue(object, Map.class);
	}
	
	protected CbsPaymentStatusResponse getMockResponse(String... rrr) {
		int status = 0;
		final List<String> paymentRefs = Arrays.asList(rrr);
		CbsPaymentStatusResponse response = new CbsPaymentStatusResponse();
		if (appConfig.isMockPaymentVerificationResponse()) {
			if (paymentRefs.contains(appConfig.getMockSuccessRRR())) {
				status = 1;
				response.setMessage("Success");
			}else if (paymentRefs.contains(appConfig.getMockOverPaidRRR())) {
				status = 4;
				response.setMessage("you overpaid for the service");
			} else if (paymentRefs.contains(appConfig.getMockServiceNotAvailableRRR())){
				status = -1;
				response.setMessage("service not available but you can proceed");
			}
		}
		response.setStatus(status);
		/*
		 * response.setConsumptionStatus("NOT_USED"); response.setCurrencyCode("NGN");
		 * response.setCustomerFullName("Test User"); response.setMessage("Successs");
		 * response.setPaymentProvider("Remita");
		 * response.setPaymentReference("REM100632343242342");
		 * 
		 * List<ServiceTypeResponse> serviceTypes = new ArrayList<>();
		 * 
		 * ServiceTypeResponse serviceType = new ServiceTypeResponse();
		 * serviceType.setAmountInMinorUnit("1500000"); serviceType.setCode("001001");
		 * serviceType.setName("adult-enrolment");
		 * 
		 * serviceTypes.add(serviceType);
		 * 
		 * ServiceTypeResponse serviceType2 = new ServiceTypeResponse();
		 * serviceType2.setAmountInMinorUnit("1000000"); serviceType2.setCode("001002");
		 * serviceType2.setName("child-enrolment");
		 * 
		 * serviceTypes.add(serviceType2); response.setServiceTypes(serviceTypes);
		 */
		return response;
		
		
	}

	@Async
	public void doPayloadBackup(String deviceId, String requestType, Date requestTime, Date responseTime, String requestPath, Object request, MayBeachResponse response) {
		jmsSender.callPayloadBackup(deviceId, requestType, requestTime, responseTime, requestPath, request, response);
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public void setJmsSender(JmsSender jmsSender) {
		this.jmsSender = jmsSender;
	}

	public void setAppConfig(AppConfig appConfig) {
		this.appConfig = appConfig;
	}

	public List<String> getCodes(){
		String acceptableCodes = appConfig.getCbsAcceptableCodes();
		if(StringUtils.isEmpty(acceptableCodes)){
			return Collections.emptyList();
		}
		String[] split = acceptableCodes.split(",");
		return Arrays.asList(split);
	}

	protected void setAccountIdDeviceIdSignatureHeaderParams(HttpHeaders headers, String deviceId, String requestJson) {
		String cbsAccountCode = appConfig.getCbsAccountCode();
		String cbsApiKey = appConfig.getCbsApiKey();
		headers.add(X_ACCOUNT_ID, cbsAccountCode);
		headers.add(X_DEVICE_ID, deviceId);
		if(requestJson == null){
			headers.add(SIGNATURE, Sha512Impl.getSHA512(cbsAccountCode + cbsApiKey).toUpperCase());
		}else {
			headers.add(SIGNATURE, Sha512Impl.getSHA512(requestJson + cbsAccountCode + cbsApiKey).toUpperCase());
		}
	}

	protected void setAccountIdSignatureHeaderParams(MultiValueMap<String, String> headers, String requestJson) {
		String cbsAccountCode = appConfig.getCbsAccountCode();
		headers.add(X_ACCOUNT_ID, cbsAccountCode);
		headers.add(SIGNATURE, Sha512Impl.getSHA512(requestJson + cbsAccountCode + appConfig.getCbsApiKey()).toUpperCase());
	}

	protected String safeString(String aString) {
		return aString == null ? "null" : aString;
	}

	protected MayBeachRequestResponse handleJsonParseException(HttpStatusCodeException exception) {
		MayBeachRequestResponse cbsResponse;
		try {
			cbsResponse = objectMapper.convertValue(exception.getResponseBodyAsString(), MayBeachRequestResponse.class);
		} catch (JsonSyntaxException e) {
			log.error("Error parsing cbs response", e);
			cbsResponse = new MayBeachRequestResponse(exception.getStatusCode().value(), exception.getResponseBodyAsString());
		}
		return cbsResponse;
	}
}
