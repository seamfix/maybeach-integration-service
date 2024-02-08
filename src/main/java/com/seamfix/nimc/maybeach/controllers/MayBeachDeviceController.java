package com.seamfix.nimc.maybeach.controllers;

import com.seamfix.nimc.maybeach.dto.CbsDeviceActivationRequest;
import com.seamfix.nimc.maybeach.dto.CbsDeviceCertificationRequest;
import com.seamfix.nimc.maybeach.dto.CbsDeviceUserLoginRequest;
import com.seamfix.nimc.maybeach.dto.CbsHeartBeatsRequest;
import com.seamfix.nimc.maybeach.dto.MayBeachResponse;
import com.seamfix.nimc.maybeach.services.device.MayBeachDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author nnwachukwu
 *
 */
@RestController
@RequestMapping("/device")
public class MayBeachDeviceController {
	
	@Autowired
	private MayBeachDeviceService cbsDeviceService;

	@PostMapping("/request-activation")
	public MayBeachResponse deviceActivationRequest(@Valid @RequestBody CbsDeviceActivationRequest cbsDeviceActivationRequest){
		
		return cbsDeviceService.sendDeviceActivationRequest(cbsDeviceActivationRequest);

	}

	@PostMapping("/request-certification")
	public MayBeachResponse deviceCertificationRequest(@Valid @RequestBody CbsDeviceCertificationRequest deviceCertificationRequest){

		return cbsDeviceService.sendDeviceCertificationRequest(deviceCertificationRequest) ;

	}

	@GetMapping("/activation-data/{deviceId}/{requestId}")
	public MayBeachResponse fetchActivationDataRequest(@PathVariable("deviceId") String deviceId, @PathVariable("requestId") String requestId){

		return cbsDeviceService.sendFetchActivationDataRequest(deviceId, requestId) ;

	}

	@PostMapping("/login")
	public MayBeachResponse deviceUserLoginRequest(@Valid @RequestBody CbsDeviceUserLoginRequest userLoginRequest){

		return cbsDeviceService.sendDeviceUserLoginRequest(userLoginRequest) ;

	}

	@GetMapping("/ping")
	public String ping() {
		return "CBS middleware service is up and running!";
	}

	@PostMapping("/heartbeats")
	public MayBeachResponse sendHeartBeats(@Valid @RequestBody CbsHeartBeatsRequest heartBeatsRequest){
		return cbsDeviceService.sendHeartBeats(heartBeatsRequest) ;
	}
}
