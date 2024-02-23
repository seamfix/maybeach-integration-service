package com.seamfix.nimc.maybeach.services.device;

import com.google.gson.Gson;
import com.seamfix.nimc.maybeach.dto.*;
import com.seamfix.nimc.maybeach.enums.RequestTypeEnum;
import com.seamfix.nimc.maybeach.enums.ResponseCodeEnum;
import com.seamfix.nimc.maybeach.enums.SettingsEnum;
import com.seamfix.nimc.maybeach.services.SettingService;
import com.seamfix.nimc.maybeach.services.payment.MayBeachService;
import com.seamfix.nimc.maybeach.utils.Constants;
import com.seamfix.nimc.maybeach.utils.EncryptionKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@SuppressWarnings("PMD.GuardLogStatement")
public class MayBeachDeviceMock extends MayBeachService {

	private static final String MAYBEACH_RESPONSE_STATUS = "Maybeach response status:: {}";

	@Autowired
	private EncryptionKeyUtil encryptionKeyUtil;

	@Autowired
	private Gson gson;

	@Autowired
	private SettingService settingsService;

	public MayBeachResponse handleRequest(RequestTypeEnum requestType) {

		String url = "Mock";
		log.info("{} Url: {}", requestType.name(), url);

		MayBeachResponse mayBeachResponse = new MayBeachResponse();
		try {
			mayBeachResponse = switch (requestType) {
				case DEVICE_ACTIVATION -> callDeviceActivationService(url);
				case DEVICE_CERTIFICATION -> callDeviceCertificationService(url);
				case FETCH_ACTIVATION_DATA -> callFetchActivationDataService(url);
				case DEVICE_USER_LOGIN -> callDeviceUserLoginService(url);
				case DEVICE_ONBOARDING -> callOnboardingDeviceRequest(url);
				case DEVICE_ONBOARDING_STATUS -> callOnboardingRequestStatus(url);
			};
			log.info("After processing: {}", mayBeachResponse);
		} catch (Exception ex) {
			log.error("Error handling {} request", requestType.name(), ex);
			buildMayBeachResponse(mayBeachResponse, ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getDescription(), ResponseCodeEnum.UNABLE_TO_REACH_MAYBEACH.getCode());
		}

		return mayBeachResponse;
	}


	private MayBeachResponse callOnboardingDeviceRequest(String url) {
		log.info("callOnboardingDeviceRequest url: {}", url);

		MayBeachResponse mayBeachResponse = new MayBeachResponse();
		mayBeachResponse.setStatus(HttpStatus.OK.value());

		mayBeachResponse.setMessage("draft");
		mayBeachResponse.setCode(Constants.MAYBEACH_SUCCESS_CODE);
		mayBeachResponse.setData(new HashMap<>(){{put("status", "draft");}});
		log.info(MAYBEACH_RESPONSE_STATUS, "draft");

		return mayBeachResponse;
	}

	private MayBeachResponse callOnboardingRequestStatus(String url) {
		log.info("callOnboardingDeviceRequest url: {}", url);

		MayBeachResponse mayBeachResponse = new MayBeachResponse();
		mayBeachResponse.setStatus(HttpStatus.OK.value());

		mayBeachResponse.setMessage("draft");
		mayBeachResponse.setCode(Constants.MAYBEACH_SUCCESS_CODE);
		mayBeachResponse.setData(new HashMap<>(){{put("status", "processed");}});
		log.info(MAYBEACH_RESPONSE_STATUS, "processed");

		return mayBeachResponse;
	}
	private MayBeachResponse callDeviceActivationService(String url) {
		log.info("Device Activation Url: {}", url);

		MayBeachResponse mayBeachResponse = new MayBeachResponse();

		mayBeachResponse.setStatus(HttpStatus.OK.value());
		mayBeachResponse.setMessage("Activation Request Received");
		mayBeachResponse.setCode(Constants.MAYBEACH_SUCCESS_CODE);
		return mayBeachResponse;
	}

	private MayBeachResponse callDeviceCertificationService(String url) {
		log.info("Device Certification Url: {}", url);
		MayBeachClientAppUserResponse mayBeachResponse = new MayBeachClientAppUserResponse();

		mayBeachResponse.setStatus(HttpStatus.OK.value());
		Map<String, Object> data = new HashMap<>();
		data.put("code", 540);
		data.put("message", "Activation Request Received");
		buildMayBeachResponse(mayBeachResponse, "Activation Request Received", Constants.MAYBEACH_SUCCESS_CODE, data);
		return mayBeachResponse;
	}

	private MayBeachResponse callFetchActivationDataService(String url) {
		log.info("Fetch Activation Data Url: {}", url);

		MayBeachRequestResponse mayBeachResponse = new MayBeachRequestResponse();

		String encryptedData = "9ANyQapeliCHsqB7q4rIPgzFWWVWvTnEFRcLa6+TCV2acO9W2zGUIPIe3noopVz354ITKgkRb5ByZuQkKAdsbjn6UFQJ29qEKXqtoe4mloJ2NeHYAzqZikmSfy9HDkkL+zs4oWv7z6XfNrjQ/daM4rTaqepRNOZmZXylGMVY/weFUd6mqzxxMr4GPiCTkc6+ndUHYfXxRnBUEzSJwZyBQQbXbgA+MbdVdUBZBbBVGkXd/t4Jm2/sBnJTXEtj/BhLTuM/Nh28wjBMoG40cnfzU5+jn/9LSNaNPgA4Of8RK70EdNL2VDPOycFjEkB3KB9mNpFIesl1Wpps6K91v9KOk6bA+MvdhU2V5JiGYTXNCiIQXtBYk9zLI8OS2XUK8qQQBrTgq4NpkXm9mArS+Yd0ipFC+ky+Ai5guRg4bGZQfQ5WKBU8ORvdhOq7pyMZf4WJJ/LKiKPAEwlcjMWNQfAJbdQ60lt5cE4Z0Xk6Y0SSne9nk1izNu28+KqABJ2ucl+S2JrppSkdyrQ52sWjGvaCnmPirVdYdPk+X5P45vBjn0PUK8T79/j2Plx6jILXFvpb8mX8M6r/8KaFE/gMHuz90qnfjqnYm+RzGpWYc2bALhSoOtK9kgyi7sNKjdhh/Vz7FzCKjMIJHzWWppZ7oUUxkSc95+JqQV+fOQtPKOipmSUvU/yY71eLHzxFi9V+pUqhItRxanX/i4W6biYBiCtZzucaQDo8NtyreyPlqGhp4mY=";
		String token = settingsService.getSettingValue(SettingsEnum.MAYBEACH_TOKEN);
		token = token.substring(Math.max(token.length() - 16, 0));

		DeviceActivationResultPojo decryptedData = gson.fromJson(EncryptionKeyUtil.decryptData(encryptedData, token), DeviceActivationResultPojo.class);
		buildMayBeachResponse(mayBeachResponse, Constants.SUCCESS, Constants.MAYBEACH_SUCCESS_CODE, null != decryptedData?decryptedData.getResult():null);


		mayBeachResponse.setMessage("Fetched successfully");
		return mayBeachResponse;
	}

	private MayBeachResponse callDeviceUserLoginService(String url) {
		log.info("Url: {}", url);

		MayBeachClientAppUserResponse mayBeachResponse = new MayBeachClientAppUserResponse();

		CbsClientAppUserData cbsClientAppUserData = new CbsClientAppUserData("0", "Athanasius",
				"Lawrence", "lathanasius@a.com", new ArrayList<>(){{ add("enrollment_admin");}});

		Map<String, Object> cbsResponseData = new HashMap<>();
		cbsResponseData.put("CbsClientAppUserData", cbsClientAppUserData);
		cbsResponseData.put("failedLoginAttempt", 0);

		buildMayBeachResponse(mayBeachResponse, Constants.SUCCESS, Constants.MAYBEACH_SUCCESS_CODE, cbsResponseData);
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


}
