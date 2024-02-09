package com.seamfix.nimc.maybeach.services.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seamfix.nimc.maybeach.dto.*;
import com.seamfix.nimc.maybeach.enums.RequestTypeEnum;
import com.seamfix.nimc.maybeach.enums.ResponseCodeEnum;
import com.seamfix.nimc.maybeach.enums.SettingsEnum;
import com.seamfix.nimc.maybeach.services.GraphQLUtility;
import com.seamfix.nimc.maybeach.services.SettingService;
import com.seamfix.nimc.maybeach.utils.EncryptionKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.seamfix.nimc.maybeach.services.payment.MayBeachService;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
@SuppressWarnings("PMD.GuardLogStatement")
public class MayBeachDeviceService extends MayBeachService {

	@Autowired
	private EncryptionKeyUtil encryptionKeyUtil;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	private SettingService settingsService;
	@Autowired
	GraphQLUtility graphQLUtility;

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public MayBeachResponse sendDeviceActivationRequest(CbsDeviceActivationRequest cbsDeviceActivationRequest) {
		if(!appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
		}
		return callDeviceActivationService(cbsDeviceActivationRequest);
		
	}

	public MayBeachResponse sendDeviceCertificationRequest(CbsDeviceCertificationRequest cbsDeviceCertificationRequest) {
		if(!appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
		}
		return callDeviceCertificationService(cbsDeviceCertificationRequest);

	}

	public MayBeachResponse sendFetchActivationDataRequest(String deviceId, String requestId) {
		if(!appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
		}
		return callFetchActivationDataService(deviceId, requestId);

	}

	public MayBeachResponse sendDeviceUserLoginRequest(CbsDeviceUserLoginRequest userLoginRequest) {
		if(!appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
		}
		return callDeviceUserLoginService(userLoginRequest);
	}

	public MayBeachResponse sendHeartBeats(CbsHeartBeatsRequest userLoginRequest) {
		if(!appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
		}
		return callHeartBeats(userLoginRequest);
	}

	public MayBeachResponse callDeviceActivationService(CbsDeviceActivationRequest cbsDeviceActivationRequest) {
		Date requestTime = new Date();

		String url = appConfig.getCbsDeviceActivationUri();
		log.debug("Device Activation Url: {}", url);

		MayBeachResponse cbsResponse;

		Date responseTime;

		String validationError = validateRequestParams(cbsDeviceActivationRequest);
		if (validationError != null && !validationError.isEmpty()) {
			cbsResponse = new MayBeachResponse(HttpStatus.BAD_REQUEST.value(), validationError, ResponseCodeEnum.VALIDATION_ERROR.getCode(), null);
			responseTime = new Date();
			doPayloadBackup(cbsDeviceActivationRequest.getProviderDeviceIdentifier(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, responseTime, url, cbsDeviceActivationRequest , cbsResponse);
			return cbsResponse;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String requestJson = mapToJsonString(convertObjectToMap(cbsDeviceActivationRequest));
		log.debug("Device Activation Request: {}", requestJson);
		setAccountIdSignatureHeaderParams(headers, requestJson);

		HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

		ResponseEntity<String> response;
		try{
			response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			responseTime = new Date();
		}catch (HttpStatusCodeException ex){
			responseTime = new Date();
			cbsResponse = handleJsonParseException(ex);
			doPayloadBackup(cbsDeviceActivationRequest.getProviderDeviceIdentifier(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, responseTime, url, cbsDeviceActivationRequest , cbsResponse);
			if(!getCodes().contains(String.valueOf(ex.getRawStatusCode()))){
				cbsResponse.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getDescription());
			}
			return  cbsResponse;
		}
		cbsResponse = objectMapper.convertValue(response.getBody(), MayBeachRequestResponse.class);
		doPayloadBackup(cbsDeviceActivationRequest.getProviderDeviceIdentifier(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, responseTime, url, cbsDeviceActivationRequest , cbsResponse);
		return cbsResponse;
	}

	public MayBeachResponse callDeviceCertificationService(CbsDeviceCertificationRequest deviceCertificationRequest) {
		Date requestTime = new Date();

		String url = settingsService.getSettingValue(SettingsEnum.MAYBEACH_URL);
		log.debug("Device Certification Url: {}", url);

		MayBeachResponse mayBeachResponse = new MayBeachResponse();
		Date responseTime;

		String validationError = validateRequestParams(deviceCertificationRequest);
		if (validationError != null && !validationError.isEmpty()) {
			mayBeachResponse = new MayBeachResponse(HttpStatus.BAD_REQUEST.value(), validationError, ResponseCodeEnum.VALIDATION_ERROR.getCode(), null);
			responseTime = new Date();
			doPayloadBackup(deviceCertificationRequest.getDeviceId(), RequestTypeEnum.DEVICE_CERTIFICATION.name(), requestTime, responseTime, url, deviceCertificationRequest , mayBeachResponse);
			return mayBeachResponse;
		}

		try{
			Map<String, Object> certificationResponse = graphQLUtility.deviceCertification(deviceCertificationRequest, url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());
			mayBeachResponse.setMessage("Success");

			MayBeachClientAppUserData mayBeachClientAppUserData = new MayBeachClientAppUserData();
			mayBeachClientAppUserData.setEmail((String) certificationResponse.get("code"));
			mayBeachClientAppUserData.setLastname((String) certificationResponse.get("message"));

			mayBeachResponse.setData(mayBeachClientAppUserData);
			mayBeachResponse.setCode(HttpStatus.OK.value());

			responseTime = new Date();
		}catch (HttpStatusCodeException ex){
			log.error("Error calling callDeviceCertificationService", ex);
			responseTime = new Date();
			mayBeachResponse = handleJsonParseException(ex);
			doPayloadBackup(deviceCertificationRequest.getDeviceId(), RequestTypeEnum.DEVICE_CERTIFICATION.name(), requestTime, responseTime, url, deviceCertificationRequest , mayBeachResponse);
			if(!getCodes().contains(String.valueOf(ex.getStatusCode().value()))){
				mayBeachResponse.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getDescription());
			}
			return  mayBeachResponse;
		}
		catch (IOException e){
			log.error("Exception occurred: {}", e.getMessage());
			responseTime = new Date();
		}
		doPayloadBackup(deviceCertificationRequest.getDeviceId(), RequestTypeEnum.DEVICE_CERTIFICATION.name(), requestTime, responseTime, url, deviceCertificationRequest , mayBeachResponse);

		return mayBeachResponse;
	}

	public MayBeachResponse callFetchActivationDataService(String deviceId, String requestId) {
		Date requestTime = new Date();
		String url = settingsService.getSettingValue(SettingsEnum.MAYBEACH_URL);
		log.debug("Fetch Activation Data Url: {}", url);

		MayBeachRequestResponse mayBeachResponse = new MayBeachRequestResponse();

		Date responseTime;
		String algorithm = "";

		try{
			Map<String, Object> certificationResponse = graphQLUtility.fetchActivationData(deviceId, requestId, url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());
			mayBeachResponse.setMessage("Success");

			MayBeachClientAppUserData mayBeachClientAppUserData = new MayBeachClientAppUserData();
			mayBeachClientAppUserData.setEmail((String) certificationResponse.get("code"));
			mayBeachClientAppUserData.setLastname((String) certificationResponse.get("message"));

			mayBeachResponse.setData(mayBeachClientAppUserData);
			mayBeachResponse.setCode(HttpStatus.OK.value());
			algorithm = (String) certificationResponse.get("algorithm");

			responseTime = new Date();
		}catch (HttpStatusCodeException ex){
			log.debug("Fetch Activation Data Response Body: {}", ex.getResponseBodyAsString());
			responseTime = new Date();
			mayBeachResponse = handleJsonParseException(ex);
			doPayloadBackup(deviceId, RequestTypeEnum.FETCH_ACTIVATION_DATA.name(), requestTime, responseTime, url, null , mayBeachResponse);
			if(!getCodes().contains(String.valueOf(ex.getRawStatusCode()))){
				mayBeachResponse.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getDescription());
			}
			return  mayBeachResponse;
		}
		catch (IOException e){
			log.error("Exception occured:: {}", e.getMessage());
			responseTime = new Date();
		}

		if(mayBeachResponse.getCode() == HttpStatus.OK.value()) {
			String encryptedData = (String) mayBeachResponse.getData();

			DeviceActivationDataPojo decryptedData = objectMapper.convertValue(encryptionKeyUtil.decrypt(appConfig.getCbsApiKey(), encryptedData, algorithm), DeviceActivationDataPojo.class);

			mayBeachResponse.setData(decryptedData);
		}

		doPayloadBackup(deviceId, RequestTypeEnum.FETCH_ACTIVATION_DATA.name(), requestTime, responseTime, url, deviceId , mayBeachResponse);
		return mayBeachResponse;
	}

	@SuppressWarnings({"PMD.NcssCount", "PMD.AvoidCatchingGenericException"})
	public MayBeachResponse callDeviceUserLoginService(CbsDeviceUserLoginRequest userLoginRequest) {
		Date requestTime = new Date();

		String url = settingsService.getSettingValue(SettingsEnum.MAYBEACH_URL);
		log.debug("Device User Login Url: {}", url);

		Date responseTime;
		MayBeachResponse mayBeachResponse = new MayBeachResponse();
		String validationError = validateRequestParams(userLoginRequest);
		if (validationError != null && !validationError.isEmpty()) {
			mayBeachResponse = new MayBeachResponse(HttpStatus.BAD_REQUEST.value(), validationError, ResponseCodeEnum.VALIDATION_ERROR.getCode(), null);
			responseTime = new Date();
			doPayloadBackup(userLoginRequest.getDeviceId(), RequestTypeEnum.DEVICE_USER_LOGIN.name(), requestTime, responseTime, url, userLoginRequest , mayBeachResponse);
			return mayBeachResponse;
		}
		try{
			Map<String, Object> loginResponse = graphQLUtility.login(userLoginRequest, url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());
			mayBeachResponse.setMessage("Success");

			MayBeachClientAppUserData mayBeachClientAppUserData = new MayBeachClientAppUserData();
			mayBeachClientAppUserData.setEmail((String) loginResponse.get("agentemail"));
			mayBeachClientAppUserData.setLastname((String) loginResponse.get("agentlastname"));
			mayBeachClientAppUserData.setFirstname((String) loginResponse.get("agentfirstname"));
			mayBeachClientAppUserData.setRoles(objectMapper.convertValue(loginResponse.get("permission"), ArrayList.class));
			mayBeachClientAppUserData.setLoginId((String) loginResponse.get("id"));

			mayBeachResponse.setData(mayBeachClientAppUserData);
			mayBeachResponse.setCode(HttpStatus.OK.value());
			responseTime = new Date();
		}catch (HttpStatusCodeException ex){
			log.error("Error calling CBS DeviceUserLoginService", ex);
			responseTime = new Date();
			mayBeachResponse = handleJsonParseException(ex);
			doPayloadBackup(userLoginRequest.getDeviceId(), RequestTypeEnum.DEVICE_USER_LOGIN.name(), requestTime, responseTime, url, userLoginRequest , mayBeachResponse);
			if(!getCodes().contains(String.valueOf(ex.getRawStatusCode()))){
				mayBeachResponse.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getDescription());
				mayBeachResponse.setCode(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getCode());
			}
			return mayBeachResponse;
		}catch (Exception e){
			log.error("Error calling CBS DeviceUserLoginService", e);
			mayBeachResponse = new MayBeachRequestResponse();
			mayBeachResponse.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getDescription());
			mayBeachResponse.setCode(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getCode());
			return mayBeachResponse;
		}
		doPayloadBackup(userLoginRequest.getDeviceId(), RequestTypeEnum.DEVICE_USER_LOGIN.name(), requestTime, responseTime, url, userLoginRequest , mayBeachResponse);

		return mayBeachResponse;
	}

	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	public MayBeachResponse callHeartBeats(CbsHeartBeatsRequest heartBeatsRequest) {
		Date requestTime = new Date();
		MayBeachResponse cbsResponse;

		String url = appConfig.getCbsHeartbeatsUri();
		log.debug("Heartbeats Url: {}", url);

		Date responseTime;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		if(StringUtils.isBlank(heartBeatsRequest.getDeviceTime())) {
			heartBeatsRequest.setDeviceTime(dateFormat.format(requestTime));
		}
		String requestJson = mapToJsonString(convertObjectToMap(heartBeatsRequest));
		setAccountIdDeviceIdSignatureHeaderParams(headers, heartBeatsRequest.getDeviceId(), requestJson);

		HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

		ResponseEntity<String> response;
		try{
			response = restTemplate.postForEntity(url, entity, String.class);
			responseTime = new Date();
			String responseBody = response.getBody();
			int responseCode = response.getStatusCodeValue();
			log.debug("cbs heartbeats response : {}. response code: {}", responseBody, responseCode);
			cbsResponse = new MayBeachResponse(responseCode, responseBody, ResponseCodeEnum.VALIDATION_ERROR.getCode(), null);
			cbsResponse.setCode(responseCode);
		}catch (HttpStatusCodeException ex){
			log.error("Error calling CBS Heartbeats Service", ex);
			responseTime = new Date();
			cbsResponse = objectMapper.convertValue(ex.getResponseBodyAsString(), MayBeachRequestResponse.class);
		}catch (Exception e) {
			log.error("Error calling CBS Heartbeats Service", e);
			responseTime = new Date();
			cbsResponse = new MayBeachResponse();
			cbsResponse.setMessage(e.getMessage());
			doPayloadBackup(heartBeatsRequest.getDeviceId(), RequestTypeEnum.HEARTBEAT.name(), requestTime, responseTime, url, heartBeatsRequest , cbsResponse);
			cbsResponse.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getDescription());
			return cbsResponse;
		}
		doPayloadBackup(heartBeatsRequest.getDeviceId(), RequestTypeEnum.HEARTBEAT.name(), requestTime, responseTime, url, heartBeatsRequest , cbsResponse);
		return cbsResponse;
	}
}
