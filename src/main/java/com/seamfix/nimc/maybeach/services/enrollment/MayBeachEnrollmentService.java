package com.seamfix.nimc.maybeach.services.enrollment;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seamfix.nimc.maybeach.dto.CbsPaymentStatusResponse;
import com.seamfix.nimc.maybeach.dto.CbsPreEnrollmentCheckRequest;
import com.seamfix.nimc.maybeach.dto.CbsPreEnrollmentVerificationResponse;
import com.seamfix.nimc.maybeach.enums.RequestTypeEnum;
import com.seamfix.nimc.maybeach.enums.ResponseCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.seamfix.nimc.maybeach.dto.CbsDeviceUpdateNotification;
import com.seamfix.nimc.maybeach.dto.CbsEnrollmentNotificationRequest;
import com.seamfix.nimc.maybeach.dto.CbsNewDeviceNotification;
import com.seamfix.nimc.maybeach.dto.MayBeachRequestResponse;
import com.seamfix.nimc.maybeach.dto.MayBeachResponse;
import com.seamfix.nimc.maybeach.services.payment.MayBeachService;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.TooManyMethods", "PMD.GodClass"})
public class MayBeachEnrollmentService extends MayBeachService {

	private static final String EMPTY_CBS_BODY = "cbs response doesn't contain body";
	private static final String PHONE_PRE_ENROLLMENT_ERROR_MESSAGE = "searchFieldValue is required";
	private static final String DEMOGRAPHICS_PRE_ENROLLMENT_ERROR_MESSAGE = "firstName, lastName, gender, dob are required";
	@Autowired
	ObjectMapper objectMapper;

	public MayBeachResponse sendEnrollmentNotificationService(CbsEnrollmentNotificationRequest cbsEnrollmentNotificationRequest) {
		if(!appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
		}
		return callEnrollmentNotificationService(cbsEnrollmentNotificationRequest);
		
	}
	
	public MayBeachResponse sendNewDeviceNotification(CbsNewDeviceNotification cbsNewDeviceNotification) {
		if(!appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
		}
		return callNewDeviceNotificationService(cbsNewDeviceNotification);
		
	}

	public MayBeachResponse sendDeviceUpdateNotification(CbsDeviceUpdateNotification cbsDeviceUpdateNotification, String deviceId) {
		if(!appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
		}
		return callDeviceUpdateNotification(cbsDeviceUpdateNotification, deviceId);
		
	}

	public MayBeachResponse fetchEnrollmentCenters(String deviceId, String fepCode) {
		if(!appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
		}
		return callFetchEnrollmentCenters(deviceId, fepCode);
		
	}
	
	public MayBeachResponse getEntityStatus(String entityType, String entityIdentifier, String deviceId) {
		if(!appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
		}
		return callGetEntityStatus(entityType, entityIdentifier, deviceId);
		
	}

	public MayBeachResponse doPreEnrollmentCheck(CbsPreEnrollmentCheckRequest preEnrollmentCheckRequest) {
		if(!appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
		}
		return callPreEnrollmentCheck(preEnrollmentCheckRequest);

	}

	public CbsPreEnrollmentVerificationResponse doPhonePreEnrollmentCheck(CbsPreEnrollmentCheckRequest preEnrollmentCheckRequest) {
		if(!appConfig.isMayBeachIntegrationEnabled()) {
			CbsPaymentStatusResponse mockResponse = getMockResponse();
			return new CbsPreEnrollmentVerificationResponse(mockResponse.getStatus(), mockResponse.getMessage());
		}
		return callPhonePreEnrollmentCheck(preEnrollmentCheckRequest);
	}

	public CbsPreEnrollmentVerificationResponse doDemographicPreEnrollmentCheck(CbsPreEnrollmentCheckRequest preEnrollmentCheckRequest) {
		if(!appConfig.isMayBeachIntegrationEnabled()) {
			CbsPaymentStatusResponse mockResponse = getMockResponse();
			return new CbsPreEnrollmentVerificationResponse(mockResponse.getStatus(), mockResponse.getMessage());
		}
		return callDemographicPreEnrollmentCheck(preEnrollmentCheckRequest);
	}

	private MayBeachResponse callEnrollmentNotificationService(CbsEnrollmentNotificationRequest cbsEnrollmentNotificationRequest) {
		Date requestTime = new Date();

		String enrollmentNotificationServiceUrl = appConfig.getCbsEnrolmentNotificationUri();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String requestJson = mapToJsonString(convertObjectToMap(cbsEnrollmentNotificationRequest));
		setAccountIdDeviceIdSignatureHeaderParams(headers, cbsEnrollmentNotificationRequest.getDeviceId(), requestJson);

		MayBeachResponse cbsResponse;

		HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
		Date responseTime;

		ResponseEntity<String> response;
		try{
			response = restTemplate.exchange(enrollmentNotificationServiceUrl, HttpMethod.POST, entity, String.class);
			responseTime = new Date();
		}catch (HttpClientErrorException ex){
			responseTime = new Date();
			cbsResponse = handleJsonParseException(ex);
			doPayloadBackup(cbsEnrollmentNotificationRequest.getDeviceId(), RequestTypeEnum.ENROLLMENT_NOTIFICATION.name(), requestTime, responseTime, enrollmentNotificationServiceUrl, cbsEnrollmentNotificationRequest , cbsResponse);
			return  cbsResponse;
		}
		cbsResponse = objectMapper.convertValue(response.getBody(), MayBeachRequestResponse.class);
		doPayloadBackup(cbsEnrollmentNotificationRequest.getDeviceId(), RequestTypeEnum.ENROLLMENT_NOTIFICATION.name(), requestTime, responseTime, enrollmentNotificationServiceUrl, cbsEnrollmentNotificationRequest , cbsResponse);
		return cbsResponse;
	}
	
	private MayBeachRequestResponse callNewDeviceNotificationService(CbsNewDeviceNotification cbsNewDeviceNotification) {
		String newDeviceNotificationServiceUrl = appConfig.getCbsNewDeviceNotificationUri();

		MayBeachRequestResponse cbsResponse;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String requestJson = mapToJsonString(convertObjectToMap(cbsNewDeviceNotification));
		setAccountIdSignatureHeaderParams(headers, requestJson);

		HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

		ResponseEntity<String> response;
		try{
			response = restTemplate.exchange(newDeviceNotificationServiceUrl, HttpMethod.POST, entity, String.class);
		}catch (HttpClientErrorException ex){
			cbsResponse = handleJsonParseException(ex);
			return  cbsResponse;
		}
		cbsResponse = objectMapper.convertValue(response.getBody(), MayBeachRequestResponse.class);
		
		return cbsResponse;
	}

	private MayBeachResponse callDeviceUpdateNotification(CbsDeviceUpdateNotification cbsDeviceUpdateNotification, String deviceId) {
		String deviceUpdateNotificationServiceUrl = appConfig.getCbsDeviceUpdateNotificationUri().replace("{deviceId}", safeString(deviceId));

		log.info(deviceUpdateNotificationServiceUrl);

		MayBeachRequestResponse cbsResponse;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String requestJson = mapToJsonString(convertObjectToMap(cbsDeviceUpdateNotification));
		setAccountIdSignatureHeaderParams(headers, requestJson);

		HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
		ResponseEntity<String> response;

		try {
	    	response = restTemplate.exchange(deviceUpdateNotificationServiceUrl, HttpMethod.PUT, entity, String.class);
		}catch (HttpClientErrorException ex){
			cbsResponse = handleJsonParseException(ex);
			return  cbsResponse;
		}
		cbsResponse = objectMapper.convertValue(response.getBody(), MayBeachRequestResponse.class);
		
		return cbsResponse;
	}

	private MayBeachResponse callFetchEnrollmentCenters(String deviceId, String fepCode) {
		Date requestTime = new Date();
		String fetchEnrollmentServiceUrl = appConfig.getCbsFetchEnrolmentNotificationUri().replace("{fepCode}", safeString(fepCode));

		log.info(fetchEnrollmentServiceUrl);
		HttpHeaders headers = new HttpHeaders();
		setAccountIdDeviceIdSignatureHeaderParams(headers, deviceId, null);

		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<Object> response;
		MayBeachRequestResponse cbsResponse = new MayBeachRequestResponse();
		try {
			response = restTemplate.exchange(fetchEnrollmentServiceUrl, HttpMethod.GET, entity, Object.class);
		}catch (RestClientException ex){
			Date responseTime = new Date();
			cbsResponse.setCode(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getCode());
			cbsResponse.setMessage(ex.getMessage());
			doPayloadBackup(deviceId, RequestTypeEnum.FETCH_ENROLLMENT_CENTERS.name(), requestTime, responseTime, fetchEnrollmentServiceUrl, null , cbsResponse);
			cbsResponse.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getDescription());
			return cbsResponse;
		}

		Date responseTime = new Date();

		Object cbsResponseMap = response.getBody();
		if(null != cbsResponseMap && !response.getStatusCode().is2xxSuccessful()) {
			cbsResponse.setStatus(Integer.parseInt(String.valueOf(((HttpHeaders) cbsResponseMap).get(CODE_STR))));
			cbsResponse.setMessage(String.valueOf(((HttpHeaders) cbsResponseMap).get(MESSAGE)));
			cbsResponse.setData(mapToObject(((Map)((HttpHeaders) cbsResponseMap).get("data")), Object.class));
		}
		if(null != cbsResponseMap && response.getStatusCode().is2xxSuccessful()) {
			cbsResponse.setStatus(response.getStatusCodeValue());
			cbsResponse.setMessage(response.getStatusCode().name());
			cbsResponse.setData(cbsResponseMap);
		}

		doPayloadBackup(deviceId, RequestTypeEnum.FETCH_ENROLLMENT_CENTERS.name(), requestTime, responseTime, fetchEnrollmentServiceUrl, null , cbsResponse);

		return cbsResponse;
	}
	
	private MayBeachResponse callGetEntityStatus(String entityType, String entityIdentifier, String deviceId) {
		Date requestTime = new Date();
		String url = appConfig.getCbsEntityStatusUri().replace("{entityType}", safeString(entityType)).replace("{entityIdentifier}", safeString(entityIdentifier));

		MayBeachRequestResponse cbsResponse;
		log.info(url);
		HttpHeaders headers = new HttpHeaders();
		setAccountIdDeviceIdSignatureHeaderParams(headers, deviceId, null);

		HttpEntity<String> entity = new HttpEntity<>(headers);

		Date responseTime;
		ResponseEntity<String> response;
		try {
			response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			responseTime = new Date();
		}catch (HttpStatusCodeException ex){
			log.error("Error calling CBS callGetEntityStatus", ex);
			responseTime = new Date();
			cbsResponse = handleJsonParseException(ex);
			doPayloadBackup(deviceId, RequestTypeEnum.ENTITY_STATUS.name(), requestTime, responseTime, url, null , cbsResponse);
			if(!getCodes().contains(String.valueOf(ex.getStatusCode().value()))){
				cbsResponse.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getDescription());
			}
			return  cbsResponse;
		}
		cbsResponse = objectMapper.convertValue(response.getBody(), MayBeachRequestResponse.class);

		doPayloadBackup(deviceId, RequestTypeEnum.ENTITY_STATUS.name(), requestTime, responseTime, url, null , cbsResponse);

		return cbsResponse;
	}

	private MayBeachResponse callPreEnrollmentCheck(CbsPreEnrollmentCheckRequest preEnrollmentCheckRequest) {
		Date requestTime = new Date();

		String url = appConfig.getCbsPreEnrolmentCheckUri();
		log.debug("Pre enrolment verification URL: {}", url);

		MayBeachResponse cbsResponse;

		String validationError = validateRequestParams(preEnrollmentCheckRequest);
		if (validationError != null && !validationError.isEmpty()) {
			cbsResponse = new MayBeachResponse(HttpStatus.BAD_REQUEST.value(), validationError);
			doPayloadBackup(preEnrollmentCheckRequest.getDeviceId(), RequestTypeEnum.PRE_ENROLLMENT_CHECK.name(), requestTime, new Date(), url, preEnrollmentCheckRequest , cbsResponse);
			return cbsResponse;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String requestJson = mapToJsonString(convertObjectToMap(preEnrollmentCheckRequest));
		log.debug("Pre enrolment verification Request: {}", requestJson);
		setAccountIdDeviceIdSignatureHeaderParams(headers, preEnrollmentCheckRequest.getDeviceId(), requestJson);

		HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
		Date responseTime;

		ResponseEntity<String> response;
		try{
			response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			responseTime = new Date();
		}catch (HttpStatusCodeException ex){
			log.error("Exception occurred trying to send callPreEnrollmentCheck request to url {}", url, ex);
			responseTime = new Date();
			cbsResponse = handleJsonParseException(ex);
			doPayloadBackup(preEnrollmentCheckRequest.getDeviceId(), RequestTypeEnum.PRE_ENROLLMENT_CHECK.name(), requestTime, responseTime, url, preEnrollmentCheckRequest , cbsResponse);
			return  cbsResponse;
		}

		if (response.getBody() == null){
			log.error(EMPTY_CBS_BODY);
			cbsResponse = new MayBeachResponse();
		} else {
			cbsResponse = objectMapper.convertValue(response.getBody(), MayBeachRequestResponse.class);
			updateCbsResponse(cbsResponse);
		}
		doPayloadBackup(preEnrollmentCheckRequest.getDeviceId(), RequestTypeEnum.PRE_ENROLLMENT_CHECK.name(), requestTime, responseTime, url, preEnrollmentCheckRequest , cbsResponse);
		return cbsResponse;
	}

	private CbsPreEnrollmentVerificationResponse callPhonePreEnrollmentCheck(CbsPreEnrollmentCheckRequest preEnrollmentCheckRequest) {
		Date requestTime = new Date();
		CbsPreEnrollmentVerificationResponse cbsResponse;
		String url = appConfig.getCbsPhonePreEnrolmentCheckUri() + "?phoneNumber="+preEnrollmentCheckRequest.getSearchFieldValue();
		log.debug("Phone pre-enrolment verification URL: {}", url);

		if(preEnrollmentCheckRequest.validatePhoneParams()) {
			cbsResponse = new CbsPreEnrollmentVerificationResponse(HttpStatus.BAD_REQUEST.value(), PHONE_PRE_ENROLLMENT_ERROR_MESSAGE);
			doPayloadBackup(preEnrollmentCheckRequest.getDeviceId(), RequestTypeEnum.PRE_ENROLLMENT_CHECK.name(), requestTime, new Date(), url, preEnrollmentCheckRequest , cbsResponse);
			return cbsResponse;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		setAccountIdDeviceIdSignatureHeaderParams(headers, preEnrollmentCheckRequest.getDeviceId(), null);

		HttpEntity<String> entity = new HttpEntity<>(headers);
		Date responseTime;

		ResponseEntity<String> response;
		try{
			response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			responseTime = new Date();
		}catch (HttpStatusCodeException ex){
			log.error("Exception occurred during phone pre enrollment check. url {}", url, ex);
			responseTime = new Date();
			MayBeachRequestResponse exceptionResponse = handleJsonParseException(ex);
			cbsResponse = new CbsPreEnrollmentVerificationResponse(exceptionResponse.getStatus(), exceptionResponse.getMessage());
			doPayloadBackup(preEnrollmentCheckRequest.getDeviceId(), RequestTypeEnum.PRE_ENROLLMENT_CHECK.name(), requestTime, responseTime, url, preEnrollmentCheckRequest , cbsResponse);
			return cbsResponse;
		}catch (Exception e) {
			log.error("Exception occurred during phone pre enrollment check. url {}", url, e);
			responseTime = new Date();
			cbsResponse = new CbsPreEnrollmentVerificationResponse(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getCode(), ResponseCodeEnum.UNABLE_TO_REACH_CBS.getDescription());
			doPayloadBackup(preEnrollmentCheckRequest.getDeviceId(), RequestTypeEnum.PRE_ENROLLMENT_CHECK.name(), requestTime, responseTime, url, preEnrollmentCheckRequest , cbsResponse);
			return cbsResponse;
		}

		cbsResponse = getResponseBody(response);
		doPayloadBackup(preEnrollmentCheckRequest.getDeviceId(), RequestTypeEnum.PRE_ENROLLMENT_CHECK.name(), requestTime, responseTime, url, preEnrollmentCheckRequest , cbsResponse);
		return cbsResponse;
	}

	private CbsPreEnrollmentVerificationResponse callDemographicPreEnrollmentCheck(CbsPreEnrollmentCheckRequest preEnrollmentCheckRequest) {
		Date requestTime = new Date();

		String url = appConfig.getCbsDemographicPreEnrolmentCheckUri() + "?firstName=" + preEnrollmentCheckRequest.getFirstName()
				+ "&lastName=" + preEnrollmentCheckRequest.getLastName() + "&dateOfBirth=" + preEnrollmentCheckRequest.getDob()
				+ "&gender=" + preEnrollmentCheckRequest.getGender();
		log.debug("demographic pre-enrolment verification URL: {}", url);

		CbsPreEnrollmentVerificationResponse cbsResponse;

		if(preEnrollmentCheckRequest.validateDemographicsParams()) {
			cbsResponse = new CbsPreEnrollmentVerificationResponse(HttpStatus.BAD_REQUEST.value(), DEMOGRAPHICS_PRE_ENROLLMENT_ERROR_MESSAGE);
			doPayloadBackup(preEnrollmentCheckRequest.getDeviceId(), RequestTypeEnum.PRE_ENROLLMENT_CHECK.name(), requestTime, new Date(), url, preEnrollmentCheckRequest , cbsResponse);
			return cbsResponse;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		setAccountIdDeviceIdSignatureHeaderParams(headers, preEnrollmentCheckRequest.getDeviceId(), null);

		HttpEntity<String> entity = new HttpEntity<>(headers);
		Date responseTime;

		ResponseEntity<String> response;
		try{
			response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
			responseTime = new Date();
		}catch (HttpStatusCodeException statusCodeException){
			log.error("Exception occurred during demographic pre enrollment check. url {}", url, statusCodeException);
			responseTime = new Date();
			MayBeachRequestResponse exceptionResponse = handleJsonParseException(statusCodeException);
			cbsResponse = new CbsPreEnrollmentVerificationResponse(exceptionResponse.getStatus(), exceptionResponse.getMessage());
			doPayloadBackup(preEnrollmentCheckRequest.getDeviceId(), RequestTypeEnum.PRE_ENROLLMENT_CHECK.name(), requestTime, responseTime, url, preEnrollmentCheckRequest , cbsResponse);
			return cbsResponse;
		}catch (Exception exception) {
			log.error("Exception occurred during demographic pre enrollment check. url {}", url, exception);
			responseTime = new Date();
			cbsResponse = new CbsPreEnrollmentVerificationResponse(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getCode(), ResponseCodeEnum.UNABLE_TO_REACH_CBS.getDescription());
			doPayloadBackup(preEnrollmentCheckRequest.getDeviceId(), RequestTypeEnum.PRE_ENROLLMENT_CHECK.name(), requestTime, responseTime, url, preEnrollmentCheckRequest , cbsResponse);
			return cbsResponse;
		}

		cbsResponse = getResponseBody(response);
		doPayloadBackup(preEnrollmentCheckRequest.getDeviceId(), RequestTypeEnum.PRE_ENROLLMENT_CHECK.name(), requestTime, responseTime, url, preEnrollmentCheckRequest , cbsResponse);
		return cbsResponse;
	}

	private CbsPreEnrollmentVerificationResponse getResponseBody(ResponseEntity<String> response) {
		CbsPreEnrollmentVerificationResponse cbsResponse;
		if (response.getBody() == null) {
			log.error(EMPTY_CBS_BODY);
			cbsResponse = new CbsPreEnrollmentVerificationResponse();
		} else {
			cbsResponse = objectMapper.convertValue(response.getBody(), CbsPreEnrollmentVerificationResponse.class);
			updateCbsResponse(cbsResponse);
		}
		return cbsResponse;
	}

	private void updateCbsResponse(MayBeachResponse cbsResponse) {
		cbsResponse.setMessage("Pre-enrollment Check");
		cbsResponse.setCode(0);
		cbsResponse.setStatus(0);
	}
}
