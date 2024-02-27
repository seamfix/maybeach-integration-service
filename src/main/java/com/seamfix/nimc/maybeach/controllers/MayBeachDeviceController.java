package com.seamfix.nimc.maybeach.controllers;

import com.seamfix.nimc.maybeach.dto.CbsDeviceActivationRequest;
import com.seamfix.nimc.maybeach.dto.CbsDeviceCertificationRequest;
import com.seamfix.nimc.maybeach.dto.CbsDeviceUserLoginRequest;
import com.seamfix.nimc.maybeach.dto.MayBeachResponse;
import com.seamfix.nimc.maybeach.services.device.MayBeachDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping("/device")
public class MayBeachDeviceController {
	
	@Autowired
	private MayBeachDeviceService mayBeachResponse;

	@PostMapping("/request-activation")
	public MayBeachResponse deviceActivationRequest(@RequestBody CbsDeviceActivationRequest cbsDeviceActivationRequest){
		return mayBeachResponse.sendDeviceOnboardingRequest(cbsDeviceActivationRequest);
	}

	@GetMapping("/onboarding-request-status/{deviceId}")
	public MayBeachResponse onboardingRequestStatus(@PathVariable("deviceId") String deviceId){
		return mayBeachResponse.sendDeviceOnboardingRequestStatus(deviceId);
	}

	@PostMapping("/request-certification")
	public MayBeachResponse deviceCertificationRequest(@RequestBody CbsDeviceCertificationRequest deviceCertificationRequest){
		return mayBeachResponse.sendDeviceActivationRequest(deviceCertificationRequest) ;
	}

	@GetMapping("/activation-data/{deviceId}/{requestId}")
	public MayBeachResponse fetchActivationDataRequest(@PathVariable("deviceId") String deviceId, @PathVariable("requestId") String requestId){
		return mayBeachResponse.sendFetchActivationDataRequest(deviceId, requestId) ;
	}

	@PostMapping("/login")
	public MayBeachResponse deviceUserLoginRequest(@RequestBody CbsDeviceUserLoginRequest userLoginRequest){
		log.info("deviceUserLoginRequest: {}", userLoginRequest);
		return mayBeachResponse.sendDeviceUserLoginRequest(userLoginRequest) ;
	}

	@GetMapping("/ping")
	public String ping() {
		return "MayBeach middleware service is up and running!";
	}

}
