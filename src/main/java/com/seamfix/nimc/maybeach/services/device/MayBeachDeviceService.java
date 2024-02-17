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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.seamfix.nimc.maybeach.services.payment.MayBeachService;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@SuppressWarnings("PMD.GuardLogStatement")
public class MayBeachDeviceService extends MayBeachService {

	private static final String MAYBEACH_RESPONSE_STATUS = "Maybeach response status:: {}";
	private static final String MESSAGE = "message";
	private static final String ERROR = "error";

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
		if (!appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
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
				default -> {
					log.warn("Unsupported request type: {}", requestType);
					yield getMockResponse();
				}
			};
		} catch (Exception ex) {
			log.error("Error handling {} request", requestType.name(), ex);
			buildMayBeachResponse(mayBeachResponse, ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getDescription(), ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getCode());
		}

		return mayBeachResponse;
	}


	private MayBeachResponse callOnboardingDeviceRequest(CbsDeviceActivationRequest cbsDeviceActivationRequest, Date requestTime, String url) {
		log.info("callOnboardingDeviceRequest url: {}", url);

		MayBeachResponse mayBeachResponse = new MayBeachResponse();

		String validationError = validateRequestParams(cbsDeviceActivationRequest);
		if (validationError != null && !validationError.isEmpty()) {
			mayBeachResponse = new MayBeachResponse(HttpStatus.BAD_REQUEST.value(), validationError);
			doPayloadBackup(cbsDeviceActivationRequest.getProviderDeviceIdentifier(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, new Date(), url, cbsDeviceActivationRequest , mayBeachResponse);
			return mayBeachResponse;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		try{
			Map<String, Object> onboardingDeviceResponse = graphQLUtility.onboardingDeviceRequest(cbsDeviceActivationRequest, url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());

			log.info("callOnboardingDeviceRequest Response :: {}", onboardingDeviceResponse);
			if(null != onboardingDeviceResponse) {
				if(null != onboardingDeviceResponse.get(ERROR)){
					buildMayBeachResponse(mayBeachResponse, (String) onboardingDeviceResponse.get(ERROR), Constants.MAYBEACH_ERROR_CODE);
					return mayBeachResponse;
				}
				log.info("callOnboardingDeviceRequest Response Status :: {}", onboardingDeviceResponse.get(DATA));
				Map<String, Object> responseObject = objectMapper.convertValue(onboardingDeviceResponse.get(DATA), Map.class);
				List<Map<String, Object>> data = (List<Map<String, Object>>)responseObject.get("onboardingDeviceRequest");
				mayBeachResponse.setMessage((String) data.get(0).get("status"));
				mayBeachResponse.setCode(Constants.MAYBEACH_SUCCESS_CODE);
				mayBeachResponse.setData(data.get(0));
				log.info(MAYBEACH_RESPONSE_STATUS, data.get(0).get("status"));
			}
		}catch (HttpStatusCodeException ex){
			mayBeachResponse = handleJsonParseException(ex);
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
		MayBeachResponse mayBeachResponse = new MayBeachResponse();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
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

		MayBeachResponse mayBeachResponse = new MayBeachResponse();

		String validationError = validateRequestParams(request);
		if (validationError != null && !validationError.isEmpty()) {
			mayBeachResponse = new MayBeachResponse(HttpStatus.BAD_REQUEST.value(), validationError);
			doPayloadBackup(request.getDeviceId(), RequestTypeEnum.DEVICE_ACTIVATION.name(), requestTime, new Date(), url, request , mayBeachResponse);
			return mayBeachResponse;
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
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
				log.info(MAYBEACH_RESPONSE_STATUS, data.get(0));
				mayBeachResponse.setData(data.get(0));
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

		try{
			Map<String, Object> certificationResponse = graphQLUtility.fetchActivationData(request.getDeviceId(), request.getRequestId(), url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());

			log.info("callFetchActivationDataService Response :: {}", certificationResponse);
			if(null != certificationResponse) {
				if(null != certificationResponse.get(ERROR)){
					buildMayBeachResponse(mayBeachResponse, (String) certificationResponse.get(ERROR), Constants.MAYBEACH_ERROR_CODE, null);
					return mayBeachResponse;
				}
				if(null != certificationResponse.get(DATA)) {
					log.info("callFetchActivationDataService Response Status :: {}", certificationResponse.get(DATA));
					Map<String, Object> responseObject = objectMapper.convertValue(certificationResponse.get(DATA), Map.class);
					List<Map<String, Object>> data = (List<Map<String, Object>>) responseObject.get("fetchActivationData");
					buildMayBeachResponse(mayBeachResponse, (String) data.get(0).get(MESSAGE), Constants.MAYBEACH_SUCCESS_CODE, data.get(0));
				}else if(null != certificationResponse.get(MESSAGE)){
					buildMayBeachResponse(mayBeachResponse, (String) certificationResponse.get(MESSAGE), Constants.MAYBEACH_ERROR_CODE, null);
					return mayBeachResponse;
				}
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

		if(mayBeachResponse.getCode() == HttpStatus.OK.value()) {
			String encryptedData = (String) mayBeachResponse.getData();
			String token = settingsService.getSettingValue(SettingsEnum.MAYBEACH_TOKEN);
			token = token.substring(token.length() - 16);

			DeviceActivationResultPojo decryptedData = gson.fromJson(EncryptionKeyUtil.decryptData(encryptedData, token), DeviceActivationResultPojo.class);
			mayBeachResponse.setData(decryptedData);
		}

		doPayloadBackup(request.getDeviceId(), RequestTypeEnum.FETCH_ACTIVATION_DATA.name(), requestTime, responseTime, url, request.getRequestId() , mayBeachResponse);
		return mayBeachResponse;
	}

	private MayBeachResponse callDeviceUserLoginService(CbsDeviceUserLoginRequest userLoginRequest, Date requestTime, String url) {

		MayBeachClientAppUserResponse mayBeachResponse = new MayBeachClientAppUserResponse();
		try{
			Map<String, Object> loginResponse = graphQLUtility.login(userLoginRequest, url);
			mayBeachResponse.setStatus(HttpStatus.OK.value());

			log.info("deviceUserLogin Response :: {}", loginResponse);
			if(null != loginResponse) {
				if(null != loginResponse.get(ERROR)){
					buildMayBeachResponse(mayBeachResponse, (String) loginResponse.get(ERROR), Constants.MAYBEACH_ERROR_CODE, null);
					return mayBeachResponse;
				}
				log.info("callDeviceUserLoginService Response Status :: {}", loginResponse.get(DATA));
				Map<String, Object> responseObject = objectMapper.convertValue(loginResponse.get(DATA), Map.class);
				List<Map<String, Object>> data = (List<Map<String, Object>>)responseObject.get("deviceUserLogin");
				buildMayBeachResponse(mayBeachResponse, (String) data.get(0).get("permission"), Constants.MAYBEACH_SUCCESS_CODE, data.get(0));
			}
			buildMayBeachResponse(mayBeachResponse, Constants.SUCCESS, Constants.MAYBEACH_SUCCESS_CODE, null);
		}catch (HttpStatusCodeException ex){
			log.error("Error calling CBS DeviceUserLoginService", ex);
			MayBeachResponse mayBeachResponseException = handleJsonParseException(ex);
			doPayloadBackup(userLoginRequest.getDeviceId(), RequestTypeEnum.DEVICE_USER_LOGIN.name(), requestTime, new Date(), url, userLoginRequest , mayBeachResponseException);
			if(!getCodes().contains(String.valueOf(ex.getRawStatusCode()))){
				buildMayBeachResponse(mayBeachResponse, ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getDescription(), ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getCode(), null);
			}
			return mayBeachResponse;
		}catch (IOException e){
			log.error("Error calling CBS DeviceUserLoginService", e);
			buildMayBeachResponse(mayBeachResponse, ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getDescription(), ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getCode(), null);
			return mayBeachResponse;
		}
		doPayloadBackup(userLoginRequest.getDeviceId(), RequestTypeEnum.DEVICE_USER_LOGIN.name(), requestTime, new Date(), url, userLoginRequest , mayBeachResponse);

		return mayBeachResponse;
	}

	private MayBeachResponse getMockResponse() {
		MayBeachResponse mayBeachResponse = new MayBeachResponse();
		buildMayBeachResponse(mayBeachResponse, "Mocked response", Constants.MAYBEACH_SUCCESS_CODE);
		return mayBeachResponse;
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
		mayBeachResponse = new MayBeachClientAppUserResponse(HttpStatus.BAD_REQUEST.value(), validationError, ResponseCodeEnum.VALIDATION_ERROR.getCode(), null);
		responseTime = new Date();
		doPayloadBackup(userLoginRequest.getDeviceId(), RequestTypeEnum.DEVICE_USER_LOGIN.name(), requestTime, responseTime, url, userLoginRequest, mayBeachResponse);
		return mayBeachResponse;
	}

	private MayBeachClientAppUserResponse processValidationError(CbsDeviceUserLoginRequest userLoginRequest, String validationError, Date requestTime, String url) {
		MayBeachClientAppUserResponse mayBeachResponse;
		Date responseTime;
		mayBeachResponse = new MayBeachClientAppUserResponse(HttpStatus.BAD_REQUEST.value(), validationError, ResponseCodeEnum.VALIDATION_ERROR.getCode(), null);
		responseTime = new Date();
		doPayloadBackup(userLoginRequest.getDeviceId(), RequestTypeEnum.DEVICE_USER_LOGIN.name(), requestTime, responseTime, url, userLoginRequest, mayBeachResponse);
		return mayBeachResponse;
	}
}
