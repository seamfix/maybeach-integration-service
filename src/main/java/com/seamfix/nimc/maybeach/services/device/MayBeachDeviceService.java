package com.seamfix.nimc.maybeach.services.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.seamfix.nimc.maybeach.configs.AppConfig;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.seamfix.nimc.maybeach.services.payment.MayBeachService;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@SuppressWarnings("PMD.GuardLogStatement")
public class MayBeachDeviceService extends MayBeachService {

	private static final String MAYBEACH_RESPONSE_STATUS = "Maybeach response status:: {}";
	private static final String MESSAGE = "message";
	private static final String ERROR = "error";
	@Autowired
	MayBeachDeviceMock mayBeachDeviceMock;

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private EncryptionKeyUtil encryptionKeyUtil;

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private Gson gson;

	@Autowired
	private SettingService settingsService;

	@Autowired
	private GraphQLUtility graphQLUtility;

	public MayBeachResponse sendDeviceActivationRequest(CbsDeviceCertificationRequest cbsDeviceActivationRequest) {
		return handleRequest(cbsDeviceActivationRequest, RequestTypeEnum.DEVICE_ACTIVATION);
	}

	public MayBeachResponse sendDeviceCertificationRequest(CbsDeviceCertificationRequest cbsDeviceCertificationRequest) {
		return handleRequest(cbsDeviceCertificationRequest, RequestTypeEnum.DEVICE_CERTIFICATION);
	}

	public MayBeachResponse sendDeviceOnboardingRequest(CbsDeviceActivationRequest cbsDeviceCertificationRequest) {

		return handleRequest(cbsDeviceCertificationRequest, RequestTypeEnum.DEVICE_ONBOARDING);
	}

	public MayBeachResponse sendDeviceOnboardingRequestStatus(String deviceId) {

		return handleRequest(deviceId, RequestTypeEnum.DEVICE_ONBOARDING_STATUS);
	}

	public MayBeachResponse sendFetchActivationDataRequest(String deviceId, String requestId) {
		return handleRequest(new FetchActivationDataRequest(deviceId, requestId), RequestTypeEnum.FETCH_ACTIVATION_DATA);
	}

	public MayBeachResponse sendDeviceUserLoginRequest(CbsDeviceUserLoginRequest userLoginRequest) {
		return handleRequest(userLoginRequest, RequestTypeEnum.DEVICE_USER_LOGIN);
	}

	private MayBeachResponse handleRequest(Object request, RequestTypeEnum requestType) {
		if (Boolean.parseBoolean(settingsService.getSettingValue(SettingsEnum.MOCK_MAYBEACH))) {
			return mayBeachDeviceMock.handleRequest(requestType);
		}
		Date requestTime = new Date();
		String url = settingsService.getSettingValue(SettingsEnum.MAYBEACH_URL);
		log.info("{} Url: {}", requestType.name(), url);

		MayBeachResponse mayBeachResponse = new MayBeachResponse();
		try {
			mayBeachResponse = switch (requestType) {
				case DEVICE_ACTIVATION -> callDeviceActivationService((CbsDeviceCertificationRequest) request, requestTime, url);
				case DEVICE_CERTIFICATION -> callDeviceCertificationService((CbsDeviceCertificationRequest) request, requestTime, url);
				case FETCH_ACTIVATION_DATA -> callFetchActivationDataService((FetchActivationDataRequest) request, requestTime, url);
				case DEVICE_USER_LOGIN -> callDeviceUserLoginService((CbsDeviceUserLoginRequest) request, requestTime, url);
				case DEVICE_ONBOARDING -> callOnboardingDeviceRequest((CbsDeviceActivationRequest) request, requestTime, url);
				case DEVICE_ONBOARDING_STATUS -> callOnboardingRequestStatus((String) request, requestTime, url);
			};
		} catch (Exception ex) {
			log.error("Error handling {} request", requestType.name(), ex);
			buildMayBeachResponse(mayBeachResponse, ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getDescription(), ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getCode());
		}

		return mayBeachResponse;
	}


	private MayBeachResponse callOnboardingDeviceRequest(CbsDeviceActivationRequest cbsDeviceActivationRequest, Date requestTime, String url) {
		log.info("callOnboardingDeviceRequest url: {}", url);

		DeviceActivationResponse mayBeachResponse = new DeviceActivationResponse();

		String validationError = validateRequestParams(cbsDeviceActivationRequest);
		if (validationError != null && !validationError.isEmpty()) {
			mayBeachResponse = new DeviceActivationResponse(HttpStatus.BAD_REQUEST.value(), validationError);
			doPayloadBackup(cbsDeviceActivationRequest.getProviderDeviceIdentifier(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, new Date(), url, cbsDeviceActivationRequest , mayBeachResponse);
			return mayBeachResponse;
		}

		try{
			MaybeachDeviceActivationResponse onboardingDeviceResponse = graphQLUtility.onboardingDeviceRequest(cbsDeviceActivationRequest, url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());

			log.info("callOnboardingDeviceRequest Response :: {}", onboardingDeviceResponse);
			if(null == onboardingDeviceResponse) {
				return mayBeachResponse;
			}
			if(null != onboardingDeviceResponse.getError()){
				buildMayBeachResponse(mayBeachResponse, onboardingDeviceResponse.getError(), Constants.MAYBEACH_ERROR_CODE);
				return mayBeachResponse;
			}
			log.info("callOnboardingDeviceRequest Response Status :: {}", onboardingDeviceResponse.getStatus());

			OnboardingDeviceRequest data = onboardingDeviceResponse.getDeviceActivationData().getOnboardingDeviceRequestList().get(0);
			mayBeachResponse.setMessage(data.getStatus());
			mayBeachResponse.setCode(Constants.MAYBEACH_SUCCESS_CODE);
			mayBeachResponse.setData(onboardingDeviceResponse.getDeviceActivationData().getOnboardingDeviceRequestList());
			log.info(MAYBEACH_RESPONSE_STATUS, data.getStatus());

		}catch (HttpStatusCodeException ex){
			doPayloadBackup(cbsDeviceActivationRequest.getProviderDeviceIdentifier(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, new Date(), url, cbsDeviceActivationRequest , mayBeachResponse);
			if(!getCodes().contains(String.valueOf(ex.getRawStatusCode()))){
				mayBeachResponse.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getDescription());
			}
			return  mayBeachResponse;
		}
		doPayloadBackup(cbsDeviceActivationRequest.getProviderDeviceIdentifier(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, new Date(), url, cbsDeviceActivationRequest , mayBeachResponse);
		return mayBeachResponse;
	}

	private MayBeachResponse callOnboardingRequestStatus(String deviceId, Date requestTime, String url) {
		MayBeachRequestResponse mayBeachResponse = new MayBeachRequestResponse();
		log.info("callOnboardingRequestStatus: {}", deviceId);

		try{
			Map<String, Object> callOnboardingRequestStatusResponse = graphQLUtility.callOnboardingRequestStatus(deviceId, url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());

			log.info("callOnboardingRequestStatus Response :: {}", callOnboardingRequestStatusResponse);
			if(null != callOnboardingRequestStatusResponse) {
				if(null != callOnboardingRequestStatusResponse.get(ERROR)){
					buildMayBeachResponse(mayBeachResponse, (String) callOnboardingRequestStatusResponse.get(ERROR), Constants.MAYBEACH_ERROR_CODE);
					return mayBeachResponse;
				}
				log.info("callOnboardingRequestStatus Response Status :: {}", callOnboardingRequestStatusResponse.get(DATA));
				Map<String, Object> responseObject = objectMapper.convertValue(callOnboardingRequestStatusResponse.get(DATA), Map.class);
				List<Map<String, Object>> data = (List<Map<String, Object>>)responseObject.get("deviceStatus");
				mayBeachResponse.setMessage((String) data.get(0).get("status"));
				mayBeachResponse.setCode(Constants.MAYBEACH_SUCCESS_CODE);
				mayBeachResponse.setData(data.get(0));
				log.info(MAYBEACH_RESPONSE_STATUS, data.get(0).get("status_msg"));
			}
		}catch (HttpStatusCodeException ex){
			mayBeachResponse = handleJsonParseException(ex);
			if(!getCodes().contains(String.valueOf(ex.getRawStatusCode()))){
				mayBeachResponse.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getDescription());
			}
			doPayloadBackup(deviceId, RequestTypeEnum.DEVICE_ONBOARDING.name(), requestTime, new Date(), url, deviceId , mayBeachResponse);
			return  mayBeachResponse;
		}
		doPayloadBackup(deviceId, RequestTypeEnum.DEVICE_ONBOARDING.name(), requestTime, new Date(), url, deviceId , mayBeachResponse);
		return mayBeachResponse;
	}
	private MayBeachResponse callDeviceActivationService(CbsDeviceCertificationRequest request, Date requestTime, String url) {
		log.info("Device Activation Url: {}", url);

		MayBeachRequestResponse mayBeachResponse = new MayBeachRequestResponse();

		String validationError = validateRequestParams(request);
		if (validationError != null && !validationError.isEmpty()) {
			mayBeachResponse = new MayBeachRequestResponse(HttpStatus.BAD_REQUEST.value(), validationError);
			doPayloadBackup(request.getDeviceId(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, new Date(), url, request , mayBeachResponse);
			return mayBeachResponse;
		}

		String requestJson = mapToJsonString(convertObjectToMap(request));
		log.info("Device Activation Request: {}", requestJson);

		try{
			Map<String, Object> activationResponse = graphQLUtility.deviceActivationRequest(request, url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());

			log.info("callDeviceActivationService Response :: {}", activationResponse);
			if(null != activationResponse) {
				if(null != activationResponse.get(ERROR)){
					buildMayBeachResponse(mayBeachResponse, (String) activationResponse.get(ERROR), Constants.MAYBEACH_ERROR_CODE);
					return mayBeachResponse;
				}
				log.info("callDeviceActivationService Response Status :: {}", activationResponse.get(DATA));
				Map<String, Object> responseObject = objectMapper.convertValue(activationResponse.get(DATA), Map.class);
				List<Map<String, Object>> data = (List<Map<String, Object>>)responseObject.get("deviceActivationRequest");
				mayBeachResponse.setMessage((String) data.get(0).get(MESSAGE));
				mayBeachResponse.setCode(Constants.MAYBEACH_SUCCESS_CODE);
				mayBeachResponse.setData(data.get(0));
				log.info(MAYBEACH_RESPONSE_STATUS, data.get(0));
			}

		}catch (HttpStatusCodeException ex){
			mayBeachResponse = handleJsonParseException(ex);
			doPayloadBackup(request.getDeviceId(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, new Date(), url, request , mayBeachResponse);
			if(!getCodes().contains(String.valueOf(ex.getRawStatusCode()))){
				mayBeachResponse.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getDescription());
			}
			return  mayBeachResponse;
		}
		doPayloadBackup(request.getDeviceId(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, new Date(), url, request , mayBeachResponse);
		return mayBeachResponse;
	}

	private MayBeachResponse callDeviceCertificationService(CbsDeviceCertificationRequest request, Date requestTime, String url) {
		log.info("Device Certification Url: {}", url);

		String validationError = validateRequestParams(request);
		if (validationError != null && !validationError.isEmpty()) {
			return processValidationError(request, validationError, requestTime, url);
		}

		MayBeachClientAppUserResponse mayBeachResponse = new MayBeachClientAppUserResponse();

		try{
			Map<String, Object> certificationResponse = graphQLUtility.deviceCertificationRequest(request.getDeviceId(), url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());

			log.info("certificationResponse Response :: {}", certificationResponse);
			if(null != certificationResponse) {
				if(null != certificationResponse.get(ERROR)){
					buildMayBeachResponse(mayBeachResponse, (String) certificationResponse.get(ERROR), Constants.MAYBEACH_ERROR_CODE, null);
					return mayBeachResponse;
				}
				log.info("callDeviceCertificationService Response Status :: {}", certificationResponse.get(DATA));
				Map<String, Object> responseObject = objectMapper.convertValue(certificationResponse.get(DATA), Map.class);
				List<Map<String, Object>> data = (List<Map<String, Object>>)responseObject.get("deviceCertificationRequest");
				buildMayBeachResponse(mayBeachResponse, (String) data.get(0).get(MESSAGE), Constants.MAYBEACH_SUCCESS_CODE, data.get(0));
			}

		}catch (HttpStatusCodeException ex){
			log.error("Error calling callDeviceCertificationService", ex);
			MayBeachResponse mayBeachResponseException = handleJsonParseException(ex);
			doPayloadBackup(request.getDeviceId(), RequestTypeEnum.DEVICE_CERTIFICATION.name(), requestTime, new Date(), url, request , mayBeachResponseException);
			if(!getCodes().contains(String.valueOf(ex.getStatusCode().value()))){
				mayBeachResponseException.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getDescription());
			}
			return  mayBeachResponseException;
		}
		catch (IOException e){
			log.error("Exception occurred: {}", e.getMessage());
		}
		doPayloadBackup(request.getDeviceId(), RequestTypeEnum.DEVICE_CERTIFICATION.name(), requestTime, new Date(), url, request , mayBeachResponse);

		return mayBeachResponse;
	}

	private MayBeachResponse callFetchActivationDataService(FetchActivationDataRequest request, Date requestTime, String url) {
		log.info("Fetch Activation Data Url: {}", url);

		MayBeachRequestResponse mayBeachResponse = new MayBeachRequestResponse();

		Date responseTime;
		FetchActivationResponse certificationResponse = new FetchActivationResponse();

		try{
			certificationResponse = graphQLUtility.fetchActivationData(request.getDeviceId(), request.getRequestId(), url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());

			log.info("callFetchActivationDataService Response :: {}", certificationResponse);
			if(null == certificationResponse) {
				buildMayBeachResponse(mayBeachResponse, ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getDescription(), ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getCode(), null);
				return mayBeachResponse;
			}
			if(null != certificationResponse.getErrors()){
				buildMayBeachResponse(mayBeachResponse, ResponseCodeEnum.INVALID_DEVICE_ID.getDescription(), ResponseCodeEnum.INVALID_DEVICE_ID.getCode(), null);
				return mayBeachResponse;
			}

			responseTime = new Date();
		}catch (HttpStatusCodeException ex){
			log.info("Fetch Activation Data Response Body: {}", ex.getResponseBodyAsString());
			responseTime = new Date();
			mayBeachResponse = handleJsonParseException(ex);
			doPayloadBackup(request.getDeviceId(), RequestTypeEnum.FETCH_ACTIVATION_DATA.name(), requestTime, responseTime, url, null , mayBeachResponse);
			if(!getCodes().contains(String.valueOf(ex.getRawStatusCode()))){
				mayBeachResponse.setMessage(ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getDescription());
			}
			return  mayBeachResponse;
		}
		catch (IOException e){
			log.error("Exception occurred:: {}", e.getMessage());
			responseTime = new Date();
		}

		if(mayBeachResponse.getStatus() == HttpStatus.OK.value()) {
			if(null == certificationResponse.getActivationData() || null == certificationResponse.getActivationData().getActivationDataList() || certificationResponse.getActivationData().getActivationDataList().isEmpty()){
				return new MayBeachResponse(ResponseCodeEnum.INVALID_ACTIVATION_DATA_RESPONSE.getCode(), ResponseCodeEnum.INVALID_ACTIVATION_DATA_RESPONSE.getDescription());
			}
			String encryptedData = certificationResponse.getActivationData().getActivationDataList().get(0).getData();
			String token = settingsService.getSettingValue(SettingsEnum.MAYBEACH_TOKEN);
			token = token.substring(Math.max(token.length() - 16, 0));

			DeviceActivationResultPojo decryptedData = gson.fromJson(EncryptionKeyUtil.decryptData(encryptedData, token), DeviceActivationResultPojo.class);
			buildMayBeachResponse(mayBeachResponse, Constants.SUCCESS, Constants.MAYBEACH_SUCCESS_CODE, null != decryptedData?decryptedData.getResult():null);
		}

		doPayloadBackup(request.getDeviceId(), RequestTypeEnum.FETCH_ACTIVATION_DATA.name(), requestTime, responseTime, url, request.getRequestId() , mayBeachResponse);
		return mayBeachResponse;
	}

	private MayBeachResponse callDeviceUserLoginService(CbsDeviceUserLoginRequest userLoginRequest, Date requestTime, String url) {

		MayBeachClientAppUserResponse mayBeachResponse = new MayBeachClientAppUserResponse();
		try{
			MaybeachLoginResponse loginResponse = graphQLUtility.login(userLoginRequest, url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());

			log.info("deviceUserLogin Response :: {}", loginResponse);
			if(null == loginResponse || loginResponse.getData() == null || loginResponse.getData().getDeviceUserLogin() == null || loginResponse.getData().getDeviceUserLogin().isEmpty() ) {
				buildMayBeachResponse(mayBeachResponse, Constants.SUCCESS, Constants.MAYBEACH_SUCCESS_CODE, null);
				return mayBeachResponse;
			}

			if(null != loginResponse.getError()){
				buildMayBeachResponse(mayBeachResponse, loginResponse.getError(), Constants.MAYBEACH_ERROR_CODE, null);
				return mayBeachResponse;
			}
			log.info("callDeviceUserLoginService Response Status :: {}", loginResponse.getData());

			DeviceUserLogin data = loginResponse.getData().getDeviceUserLogin().get(0);
			CbsClientAppUserData cbsClientAppUserData = new CbsClientAppUserData(data.getId(),
					data.getAgentFirstName(),
					data.getAgentLastName(),
					data.getAgentEmail(),
					getRolesFromPermissions(data.getPermission()));

			Map<String, Object> cbsResponseData = new HashMap<>();
			cbsResponseData.put("clientAppUser", cbsClientAppUserData);
			cbsResponseData.put("failedLoginAttempt", 0);

			buildMayBeachResponse(mayBeachResponse, Constants.SUCCESS, Constants.MAYBEACH_SUCCESS_CODE, cbsResponseData);

		}catch (HttpStatusCodeException ex){
			log.error("Error calling CBS DeviceUserLoginService", ex);
			MayBeachResponse mayBeachResponseException = handleJsonParseException(ex);
			doPayloadBackup(userLoginRequest.getDeviceId(), RequestTypeEnum.DEVICE_USER_LOGIN.name(), requestTime, new Date(), url, userLoginRequest , mayBeachResponseException);
			if(!getCodes().contains(String.valueOf(ex.getRawStatusCode()))){
				buildMayBeachResponse(mayBeachResponse, ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getDescription(), ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getCode(), null);
			}
		}catch (IOException e){
			log.error("Error calling CBS DeviceUserLoginService", e);
			buildMayBeachResponse(mayBeachResponse, ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getDescription(), ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getCode(), null);
		}
//		doPayloadBackup(userLoginRequest.getDeviceId(), RequestTypeEnum.DEVICE_USER_LOGIN.name(), requestTime, new Date(), url, userLoginRequest , mayBeachResponse);

		return mayBeachResponse;
	}

	private List<String> getRolesFromPermissions(String permissions){
		if(null != permissions){
			return List.of(permissions.split(":::"));
		}
		return new ArrayList<>();
	}

	private static void buildMayBeachResponse(MayBeachResponse mayBeachResponse, String message, int code) {
		mayBeachResponse.setMessage(message);
		mayBeachResponse.setCode(code);
		log.info(MAYBEACH_RESPONSE_STATUS, message);
	}

	private static void buildMayBeachResponse(MayBeachClientAppUserResponse mayBeachResponse, String message, int code, Object data) {
		mayBeachResponse.setMessage(message);
		mayBeachResponse.setCode(code);
		mayBeachResponse.setData(data);
		log.info(MAYBEACH_RESPONSE_STATUS, message);
	}

	private static void buildMayBeachResponse(MayBeachRequestResponse mayBeachResponse, String message, int code, Object data) {
		mayBeachResponse.setMessage(message);
		mayBeachResponse.setCode(code);
		mayBeachResponse.setData(data);
		log.info(MAYBEACH_RESPONSE_STATUS, message);
	}

	private MayBeachClientAppUserResponse processValidationError(CbsDeviceCertificationRequest userLoginRequest, String validationError, Date requestTime, String url) {
		MayBeachClientAppUserResponse mayBeachResponse;
		Date responseTime;
		mayBeachResponse = new MayBeachClientAppUserResponse(HttpStatus.BAD_REQUEST.value(), validationError, ResponseCodeEnum.VALIDATION_ERROR.getCode());
		responseTime = new Date();
		doPayloadBackup(userLoginRequest.getDeviceId(), RequestTypeEnum.DEVICE_USER_LOGIN.name(), requestTime, responseTime, url, userLoginRequest, mayBeachResponse);
		return mayBeachResponse;
	}

}
