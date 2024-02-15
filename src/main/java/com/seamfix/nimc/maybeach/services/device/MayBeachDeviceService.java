package com.seamfix.nimc.maybeach.services.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seamfix.nimc.maybeach.dto.*;
import com.seamfix.nimc.maybeach.enums.RequestTypeEnum;
import com.seamfix.nimc.maybeach.enums.ResponseCodeEnum;
import com.seamfix.nimc.maybeach.enums.SettingsEnum;
import com.seamfix.nimc.maybeach.services.GraphQLUtility;
import com.seamfix.nimc.maybeach.services.SettingService;
import com.seamfix.nimc.maybeach.utils.Constants;
import com.seamfix.nimc.maybeach.utils.EncryptionKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import com.seamfix.nimc.maybeach.services.payment.MayBeachService;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
	private static String code = "code";

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public MayBeachResponse sendDeviceActivationRequest(CbsDeviceCertificationRequest cbsDeviceActivationRequest) {
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

	public MayBeachResponse callDeviceActivationService(CbsDeviceCertificationRequest cbsDeviceActivationRequest) {
		Date requestTime = new Date();

		String url = settingsService.getSettingValue(SettingsEnum.MAYBEACH_URL);
		log.info("Device Activation Url: {}", url);

		MayBeachResponse mayBeachResponse = new MayBeachResponse();

		Date responseTime;

		String validationError = validateRequestParams(cbsDeviceActivationRequest);
		if (validationError != null && !validationError.isEmpty()) {
			mayBeachResponse = new MayBeachResponse(HttpStatus.BAD_REQUEST.value(), validationError, ResponseCodeEnum.VALIDATION_ERROR.getCode());
			responseTime = new Date();
			doPayloadBackup(cbsDeviceActivationRequest.getDeviceId(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, responseTime, url, cbsDeviceActivationRequest , mayBeachResponse);
			return mayBeachResponse;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String requestJson = mapToJsonString(convertObjectToMap(cbsDeviceActivationRequest));
		log.info("Device Activation Request: {}", requestJson);

		try{
			Map<String, Object> activationResponse = graphQLUtility.deviceActivationRequest(cbsDeviceActivationRequest, url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());
			mayBeachResponse.setMessage(Constants.SUCCESS);

			mayBeachResponse.setCode((Integer) activationResponse.get(code));
			mayBeachResponse.setMessage((String) activationResponse.get("message"));
			responseTime = new Date();

		}catch (HttpStatusCodeException ex){
			responseTime = new Date();
			mayBeachResponse = handleJsonParseException(ex);
			doPayloadBackup(cbsDeviceActivationRequest.getDeviceId(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, responseTime, url, cbsDeviceActivationRequest , mayBeachResponse);
			if(!getCodes().contains(String.valueOf(ex.getRawStatusCode()))){
				mayBeachResponse.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getDescription());
			}
			return  mayBeachResponse;
		}
		doPayloadBackup(cbsDeviceActivationRequest.getDeviceId(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, responseTime, url, cbsDeviceActivationRequest , mayBeachResponse);
		return mayBeachResponse;
	}

	public MayBeachResponse callOnboardingDeviceRequest(CbsDeviceActivationRequest cbsDeviceActivationRequest) {
		log.info("callOnboardingDeviceRequest: {}", cbsDeviceActivationRequest.toString());
		if(!appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
		}
		Date requestTime = new Date();

		String url = settingsService.getSettingValue(SettingsEnum.MAYBEACH_URL);
		log.info("callOnboardingDeviceRequest url: {}", url);

		MayBeachResponse mayBeachResponse = new MayBeachResponse();

		Date responseTime;

		String validationError = validateRequestParams(cbsDeviceActivationRequest);
		if (validationError != null && !validationError.isEmpty()) {
			mayBeachResponse = new MayBeachResponse(HttpStatus.BAD_REQUEST.value(), validationError, ResponseCodeEnum.VALIDATION_ERROR.getCode());
			responseTime = new Date();
			doPayloadBackup(cbsDeviceActivationRequest.getProviderDeviceIdentifier(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, responseTime, url, cbsDeviceActivationRequest , mayBeachResponse);
			return mayBeachResponse;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String requestJson = mapToJsonString(convertObjectToMap(cbsDeviceActivationRequest));
		log.info("callOnboardingDeviceRequest - requestJson: {}", requestJson);

		try{
			Map<String, Object> onboardingDeviceResponse = graphQLUtility.onboardingDeviceRequest(cbsDeviceActivationRequest, url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());
			mayBeachResponse.setMessage(Constants.SUCCESS);

			mayBeachResponse.setCode((Integer) onboardingDeviceResponse.get(code));
			mayBeachResponse.setMessage((String) onboardingDeviceResponse.get("status"));
			responseTime = new Date();

		}catch (HttpStatusCodeException ex){
			responseTime = new Date();
			mayBeachResponse = handleJsonParseException(ex);
			doPayloadBackup(cbsDeviceActivationRequest.getProviderDeviceIdentifier(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, responseTime, url, cbsDeviceActivationRequest , mayBeachResponse);
			if(!getCodes().contains(String.valueOf(ex.getRawStatusCode()))){
				mayBeachResponse.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getDescription());
			}
			return  mayBeachResponse;
		}
		doPayloadBackup(cbsDeviceActivationRequest.getProviderDeviceIdentifier(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, responseTime, url, cbsDeviceActivationRequest , mayBeachResponse);
		return mayBeachResponse;
	}

	public MayBeachResponse callOnboardingRequestStatus(String deviceId) {
		if(!appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
		}

		String url = settingsService.getSettingValue(SettingsEnum.MAYBEACH_URL);
		log.info("Device Activation Url: {}", url);

		MayBeachResponse mayBeachResponse = new MayBeachResponse();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		log.info("callOnboardingRequestStatus: {}", deviceId);

		try{
			Map<String, Object> onboardingDeviceResponse = graphQLUtility.callOnboardingRequestStatus(deviceId, url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());
			mayBeachResponse.setMessage(Constants.SUCCESS);

			mayBeachResponse.setCode((Integer) onboardingDeviceResponse.get(code));
			mayBeachResponse.setMessage((String) onboardingDeviceResponse.get("status_msg"));

		}catch (HttpStatusCodeException ex){
			mayBeachResponse = handleJsonParseException(ex);
			if(!getCodes().contains(String.valueOf(ex.getRawStatusCode()))){
				mayBeachResponse.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getDescription());
			}
			return  mayBeachResponse;
		}
		return mayBeachResponse;
	}

	public MayBeachResponse callDeviceCertificationService(CbsDeviceCertificationRequest deviceCertificationRequest) {
		Date requestTime = new Date();

		String url = settingsService.getSettingValue(SettingsEnum.MAYBEACH_URL);
		log.info("Device Certification Url: {}", url);

		MayBeachClientAppUserResponse mayBeachResponse = new MayBeachClientAppUserResponse();
		Date responseTime;

		String validationError = validateRequestParams(deviceCertificationRequest);
		if (validationError != null && !validationError.isEmpty()) {
			mayBeachResponse = new MayBeachClientAppUserResponse(HttpStatus.BAD_REQUEST.value(), validationError, ResponseCodeEnum.VALIDATION_ERROR.getCode(), null);
			responseTime = new Date();
			doPayloadBackup(deviceCertificationRequest.getDeviceId(), RequestTypeEnum.DEVICE_CERTIFICATION.name(), requestTime, responseTime, url, deviceCertificationRequest , mayBeachResponse);
			return mayBeachResponse;
		}

		try{
			Map<String, Object> certificationResponse = graphQLUtility.deviceCertificationRequest(deviceCertificationRequest.getDeviceId(), url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());
			mayBeachResponse.setMessage(Constants.SUCCESS);

			MayBeachClientAppUserData mayBeachClientAppUserData = new MayBeachClientAppUserData();
			mayBeachClientAppUserData.setEmail((String) certificationResponse.get(code));
			mayBeachClientAppUserData.setLastname((String) certificationResponse.get("message"));

			mayBeachResponse.setData(mayBeachClientAppUserData);
			mayBeachResponse.setCode(HttpStatus.OK.value());

			responseTime = new Date();
		}catch (HttpStatusCodeException ex){
			log.error("Error calling callDeviceCertificationService", ex);
			responseTime = new Date();
			MayBeachResponse mayBeachResponseException = handleJsonParseException(ex);
			doPayloadBackup(deviceCertificationRequest.getDeviceId(), RequestTypeEnum.DEVICE_CERTIFICATION.name(), requestTime, responseTime, url, deviceCertificationRequest , mayBeachResponseException);
			if(!getCodes().contains(String.valueOf(ex.getStatusCode().value()))){
				mayBeachResponseException.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_CBS.getDescription());
			}
			return  mayBeachResponseException;
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
		log.info("Fetch Activation Data Url: {}", url);

		MayBeachRequestResponse mayBeachResponse = new MayBeachRequestResponse();

		Date responseTime;
		String algorithm = "";

		try{
			Map<String, Object> certificationResponse = graphQLUtility.fetchActivationData(deviceId, requestId, url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());
			mayBeachResponse.setMessage(Constants.SUCCESS);

			MayBeachClientAppUserData mayBeachClientAppUserData = new MayBeachClientAppUserData();
			mayBeachClientAppUserData.setEmail((String) certificationResponse.get("code"));
			mayBeachClientAppUserData.setLastname((String) certificationResponse.get("message"));

			mayBeachResponse.setData(mayBeachClientAppUserData);
			mayBeachResponse.setCode(HttpStatus.OK.value());
			algorithm = (String) certificationResponse.get("algorithm");

			responseTime = new Date();
		}catch (HttpStatusCodeException ex){
			log.info("Fetch Activation Data Response Body: {}", ex.getResponseBodyAsString());
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

	public MayBeachClientAppUserResponse callDeviceUserLoginService(CbsDeviceUserLoginRequest userLoginRequest) {

		String url = settingsService.getSettingValue(SettingsEnum.MAYBEACH_URL);
		log.info("Device User Login Url: {}", url);

		Date responseTime;
		MayBeachClientAppUserResponse mayBeachResponse = new MayBeachClientAppUserResponse();
		String validationError = validateRequestParams(userLoginRequest);
		Date requestTime = new Date();
		if (validationError != null && !validationError.isEmpty()) {
			mayBeachResponse = new MayBeachClientAppUserResponse(HttpStatus.BAD_REQUEST.value(), validationError, ResponseCodeEnum.VALIDATION_ERROR.getCode(), null);
			responseTime = new Date();
			doPayloadBackup(userLoginRequest.getDeviceId(), RequestTypeEnum.DEVICE_USER_LOGIN.name(), requestTime, responseTime, url, userLoginRequest , mayBeachResponse);
			return mayBeachResponse;
		}
		try{
			Map<String, Object> loginResponse = graphQLUtility.login(userLoginRequest, url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());

			MayBeachClientAppUserData mayBeachClientAppUserData = new MayBeachClientAppUserData();
			mayBeachClientAppUserData.setEmail((String) loginResponse.get("agentemail"));
			mayBeachClientAppUserData.setLastname((String) loginResponse.get("agentlastname"));
			mayBeachClientAppUserData.setFirstname((String) loginResponse.get("agentfirstname"));
			mayBeachClientAppUserData.setRoles(objectMapper.convertValue(loginResponse.get("permission"), List.class));
			mayBeachClientAppUserData.setLoginId((String) loginResponse.get("id"));

			mayBeachResponse.setData(mayBeachClientAppUserData);
			buildMayBeachResponse(mayBeachResponse, Constants.SUCCESS, HttpStatus.OK.value());
					responseTime = new Date();
		}catch (HttpStatusCodeException ex){
			log.error("Error calling CBS DeviceUserLoginService", ex);
			responseTime = new Date();
			MayBeachResponse mayBeachResponseException = handleJsonParseException(ex);
			doPayloadBackup(userLoginRequest.getDeviceId(), RequestTypeEnum.DEVICE_USER_LOGIN.name(), requestTime, responseTime, url, userLoginRequest , mayBeachResponseException);
			if(!getCodes().contains(String.valueOf(ex.getRawStatusCode()))){
				buildMayBeachResponse(mayBeachResponse, ResponseCodeEnum.UNABLE_TO_REACH_CBS.getDescription(), ResponseCodeEnum.UNABLE_TO_REACH_CBS.getCode());
			}
			return mayBeachResponse;
		}catch (IOException e){
			log.error("Error calling CBS DeviceUserLoginService", e);
			buildMayBeachResponse(mayBeachResponse, ResponseCodeEnum.UNABLE_TO_REACH_CBS.getDescription(), ResponseCodeEnum.UNABLE_TO_REACH_CBS.getCode());
			return mayBeachResponse;
		}
		doPayloadBackup(userLoginRequest.getDeviceId(), RequestTypeEnum.DEVICE_USER_LOGIN.name(), requestTime, responseTime, url, userLoginRequest , mayBeachResponse);

		return mayBeachResponse;
	}

	private void buildMayBeachResponse(MayBeachClientAppUserResponse mayBeachRequestResponse, String message,  int code){
		mayBeachRequestResponse.setMessage(message);
		mayBeachRequestResponse.setCode(code);
	}

}
