package com.seamfix.nimc.maybeach.services.payment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.seamfix.nimc.maybeach.dto.CbsPaymentRequest;
import com.seamfix.nimc.maybeach.dto.CbsPaymentStatusRequest;
import com.seamfix.nimc.maybeach.dto.CbsPaymentStatusRequestV3;
import com.seamfix.nimc.maybeach.dto.CbsPaymentStatusResponse;
import com.seamfix.nimc.maybeach.dto.MayBeachRequestResponse;
import com.seamfix.nimc.maybeach.dto.MayBeachResponse;
import com.seamfix.nimc.maybeach.dto.CbsResponseData;
import com.seamfix.nimc.maybeach.dto.PaymentStatusData;
import com.seamfix.nimc.maybeach.dto.PaymentStatusResponse;
import com.seamfix.nimc.maybeach.dto.ServiceType;
import com.seamfix.nimc.maybeach.enums.RequestTypeEnum;
import com.seamfix.nimc.maybeach.enums.ResponseCodeEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

@Service
@SuppressWarnings({"PMD.NcssCount","PMD.CyclomaticComplexity", "all"})
public class MayBeachPaymentService extends MayBeachService {
	
	private static final Logger logger = LoggerFactory.getLogger(MayBeachPaymentService.class);
	@Autowired
	Gson gson;
	
	public CbsResponseData consumePayment(CbsPaymentRequest cbsPaymentRequest) {
		return (CbsResponseData) callPaymentService(cbsPaymentRequest);
	}
	
	public CbsPaymentStatusResponse getPaymentStatus(String esaCode, String paymentReference, String deviceId) {
		if(appConfig.isMockPaymentVerificationResponse() || !appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
		}
		return callPaymentStatus(esaCode, paymentReference, deviceId);
	}
	
	public CbsPaymentStatusResponse getPaymentStatus(CbsPaymentStatusRequest request) {
		if(appConfig.isMockPaymentVerificationResponse() ||!appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
		}
		return callPaymentStatus(request);
	}

	public MayBeachResponse getPaymentStatusV2(CbsPaymentStatusRequest request) {
		if(appConfig.isMockPaymentVerificationResponse() ||!appConfig.isMayBeachIntegrationEnabled()) {
			return getMockResponse();
		}
		return callPaymentStatusV3(request); //call to v3 was done on purpose for legacy support
	}

	public MayBeachResponse getPaymentStatusV3(CbsPaymentStatusRequest request) {
		if(appConfig.isMockPaymentVerificationResponse() ||!appConfig.isMayBeachIntegrationEnabled()) {
			if (request.getPaymentReference() != null) {
				request.getPaymentReferences().add(request.getPaymentReference());
			}
			return getMockResponse(request.getPaymentReferences().toArray(new String[]{}));
		}
		return callPaymentStatusV3(request);
	}

	@Deprecated
	public MayBeachResponse callPaymentStatusV2(CbsPaymentStatusRequest request) {
		Date requestTime = new Date();
		logger.debug("request esa_Code {}",request.getEsaCode());
		logger.debug("request payment reference {}",request.getPaymentReference());

		String paymentStatusUrl = appConfig.getCbsPaymentStatusUri().replace("{esaCode}", safeString(request.getEsaCode())).replace("{paymentReference}", safeString(request.getPaymentReference()));
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0");
		headers.add(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.8");
		headers.add(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE);
		setAccountIdDeviceIdSignatureHeaderParams(headers, request.getDeviceId(), null);

		HttpEntity<?> entity = new HttpEntity<Object>(headers);
		ResponseEntity<Map> response = restTemplate.exchange(paymentStatusUrl, HttpMethod.GET, entity, Map.class);
		Date responseTime = new Date();
		PaymentStatusResponse cbsResponse = processCbsResponse(response, request);
		doPayloadBackup(request.getDeviceId(), RequestTypeEnum.PAYMENT_STATUS_CHECK.name(), requestTime, responseTime, paymentStatusUrl, request , cbsResponse);
		return cbsResponse;
	}

	private Object callPaymentService(CbsPaymentRequest cbsPaymentRequest) {
		String paymentConsumptionServiceUrl = appConfig.getCbsPaymentUri();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(X_DEVICE_ID, cbsPaymentRequest.getDeviceId());
		headers.add(X_ACCOUNT_ID, cbsPaymentRequest.getMappedHeaders().get("accountId"));
		headers.add(SIGNATURE, cbsPaymentRequest.getMappedHeaders().get("signature"));
		String requestJson = mapToJsonString(convertObjectToMap(cbsPaymentRequest));

		HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
		ResponseEntity<Object> response = restTemplate.postForEntity(paymentConsumptionServiceUrl, entity, Object.class);
		return response.getBody();

	}

	private CbsPaymentStatusResponse callPaymentStatus(String esaCode, String paymentReference, String deviceId) {
		String paymentStatusUrl = appConfig.getCbsPaymentStatusUri().replace("{esaCode}", safeString(esaCode)).replace("{paymentReference}", safeString(paymentReference));
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0");
		headers.add(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.8");
		headers.add(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE);
		setAccountIdDeviceIdSignatureHeaderParams(headers, deviceId, null);

		HttpEntity<?> entity = new HttpEntity<Object>(headers);
		HttpEntity<Map> response = restTemplate.exchange(paymentStatusUrl, HttpMethod.GET, entity, Map.class);
		CbsPaymentStatusResponse cbsResponse = new CbsPaymentStatusResponse();
		cbsResponse.setStatus(((ResponseEntity<Map>) response).getStatusCodeValue());
		Map cbsResponseMap = (null != response && null != response.getBody()) ? response.getBody() : null;
		if(null != cbsResponseMap) {
			cbsResponse.setStatus(Integer.parseInt(String.valueOf(cbsResponseMap.get(CODE_STR))));
			cbsResponse.setMessage(String.valueOf(cbsResponseMap.get(MESSAGE)));
			cbsResponse.setData(mapToObject(((Map)cbsResponseMap.get(DATA)), Object.class));
		}
		return cbsResponse;
	}

	private CbsPaymentStatusResponse callPaymentStatus(CbsPaymentStatusRequest request) {
		Date requestTime = new Date();
		logger.debug("request esa_Code {}",request.getEsaCode());
		logger.debug("request payment reference {}",request.getPaymentReference());

		logger.error("request esa_Code {}",request.getEsaCode());
		logger.error("request payment reference {}",request.getPaymentReference());
		String paymentStatusUrl = appConfig.getCbsPaymentStatusUri().replace("{esaCode}", safeString(request.getEsaCode())).replace("{paymentReference}", safeString(request.getPaymentReference()));
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0");
		headers.add(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.8");
		headers.add(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE);
		setAccountIdDeviceIdSignatureHeaderParams(headers, request.getDeviceId(), null);

		HttpEntity<?> entity = new HttpEntity<Object>(headers);
		HttpEntity<Map> response = restTemplate.exchange(paymentStatusUrl, HttpMethod.GET, entity, Map.class);
		Date responseTime = new Date();
		CbsPaymentStatusResponse cbsResponse = new CbsPaymentStatusResponse();
		Map cbsResponseMap = (null != response && null != response.getBody()) ? response.getBody() : null;
		if(null != cbsResponseMap) {
			int statusCode = Integer.parseInt(String.valueOf(cbsResponseMap.get(CODE_STR))) == 200 ? 0 : Integer.parseInt(String.valueOf(cbsResponseMap.get(CODE_STR)));
			cbsResponse.setStatus(statusCode);
			cbsResponse.setMessage(String.valueOf(cbsResponseMap.get(MESSAGE)));
			cbsResponse.setData(mapToObject(((Map)cbsResponseMap.get(DATA)), Object.class));
		}
		doPayloadBackup(request.getDeviceId(), RequestTypeEnum.PAYMENT_STATUS_CHECK.name(), requestTime, responseTime, paymentStatusUrl, request , cbsResponse);
		return cbsResponse;
	}

	private PaymentStatusResponse processCbsResponse(ResponseEntity<Map> response, CbsPaymentStatusRequest request){
		PaymentStatusResponse cbsResponse = new PaymentStatusResponse();
		Map cbsResponseMap = (null != response && null != response.getBody()) ? response.getBody() : null;
		if(null != cbsResponseMap) {
			PaymentStatusData paymentStatusData = cbsResponseMap.get(DATA) != null ? (PaymentStatusData) mapToObject(((Map) cbsResponseMap.get(DATA)), PaymentStatusData.class) : null;

			if(paymentStatusData != null && paymentStatusData.getServiceTypes() != null ){

				String cbsMessage = String.valueOf(cbsResponseMap.get(MESSAGE));

				List<String> paidServiceTypes = new ArrayList<>();
				List<String> missingServiceTypes = new ArrayList<>();
				List<String> unpaidServiceTypes = new ArrayList<>();

				boolean hasUnpaidServiceTypes = false;
				boolean useCaseMatched = false;
				ServiceType[] serviceTypes = paymentStatusData.getServiceTypes();
				List<String> serviceTypeNames = new ArrayList<>();

				for (ServiceType serviceType : serviceTypes) {
					serviceTypeNames.add(serviceType.getName());
					if (request.getServiceTypes() != null && request.getServiceTypes().contains(serviceType.getName())) {
						useCaseMatched = true;
					}else{
						missingServiceTypes.add(serviceType.getName());
					}
					paidServiceTypes.add(serviceType.getName());
				}
				for(String serviceType : request.getServiceTypes()) {
					if (!serviceTypeNames.contains(serviceType)) {
						unpaidServiceTypes.add(serviceType);
						hasUnpaidServiceTypes = true;
					}
				}
				cbsResponse.setPaidServiceTypes(paidServiceTypes);
				cbsResponse.setMissingServiceTypes(missingServiceTypes);
				cbsResponse.setUnpaidServiceTypes(unpaidServiceTypes);

				boolean notUsed = "NOT_USED".equalsIgnoreCase(cbsMessage);

				boolean hasMissingServiceTypes = !missingServiceTypes.isEmpty();

				ResponseCodeEnum responseCodeEnum = getResponseCodeEnum(useCaseMatched, notUsed, hasMissingServiceTypes, hasUnpaidServiceTypes);

				cbsResponse.setCode(responseCodeEnum.getCode());
				cbsResponse.setStatus(responseCodeEnum.getCode());

				String responseDescription = responseCodeEnum.getDescription();
				if(hasMissingServiceTypes){
					responseDescription = getMissingTypesDescription(responseCodeEnum, missingServiceTypes);
				}
				if(hasUnpaidServiceTypes){
					responseDescription = getMissingTypesDescription(responseCodeEnum, unpaidServiceTypes);
				}
				cbsResponse.setMessage(responseDescription);
				cbsResponse.setData(paymentStatusData);
			}else {

				cbsResponse.setCode((Integer) cbsResponseMap.get(CODE_STR));
				cbsResponse.setMessage((String) cbsResponseMap.get(MESSAGE));

			}
		}else if(response.getStatusCode() == HttpStatus.OK){

			cbsResponse.setStatus(ResponseCodeEnum.ERROR.getCode());
			cbsResponse.setCode(ResponseCodeEnum.ERROR.getCode());
			cbsResponse.setMessage("Invalid response from CBS. Please check that entered RRR is correct");

		}
		if(response.getStatusCode() == HttpStatus.NOT_FOUND){

			cbsResponse.setStatus(ResponseCodeEnum.ERROR.getCode());
			cbsResponse.setCode(ResponseCodeEnum.ERROR.getCode());
			cbsResponse.setMessage("Unable to reach CBS to validate RRR. Please contact admin");

		}
		return cbsResponse;
	}

	private MayBeachResponse callPaymentStatusV3(CbsPaymentStatusRequest request){
		MayBeachRequestResponse response = new MayBeachRequestResponse();
		response.setStatus(ResponseCodeEnum.ERROR.getCode());
		response.setCode(ResponseCodeEnum.ERROR.getCode());
		response.setMessage(ResponseCodeEnum.ERROR.getDescription());
		Date requestTime = new Date();
		logger.debug("About confirming payment status from CBS. ESA code: {}", request.getEsaCode());

		//validate service types in request
		if(request.getServiceTypes() == null || request.getServiceTypes().isEmpty()){
			response.setMessage("Please select one or more service types.");
			return  response;
		}

		//construct CBS URL
		String cbsPaymentStatusUrl = appConfig.getCbsPaymentStatusV2Uri();
		logger.debug("===CBS Payment Status URL: {}", cbsPaymentStatusUrl);

		//construct body
		CbsPaymentStatusRequestV3 cbsRequest = new CbsPaymentStatusRequestV3();
		List<String> paymentReferences = new ArrayList<>();
		if(null != request.getPaymentReferences()){
			paymentReferences = request.getPaymentReferences(); //this is reserved for future use
		}else{
			paymentReferences.add(request.getPaymentReference());
		}
		logger.debug("No. of payment references sent to CBS: {}", paymentReferences.size());
		cbsRequest.setPaymentReferences(paymentReferences);
		cbsRequest.setEsaCode(request.getEsaCode());
		cbsRequest.setServiceTypes(request.getServiceTypes());

		//construct headers
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE);
		String requestJson = gson.toJson(cbsRequest);
		setAccountIdDeviceIdSignatureHeaderParams(headers, request.getDeviceId(), requestJson);

		//call CBS
		HttpEntity<?> entity = new HttpEntity<Object>(cbsRequest, headers);
		logger.debug("Payment status request: \n {}", gson.toJson(entity));
		ResponseEntity<Map> cbsResponse = null;
		try {
			cbsResponse = restTemplate.exchange(cbsPaymentStatusUrl, HttpMethod.POST, entity, Map.class);
			logger.debug("Payment status response code: \n {}", cbsResponse.getStatusCode());

			//process response
			Date responseTime = new Date();
			response = processCbsResponseV3(cbsResponse);
			logger.debug("Payment response to client: \n {}", gson.toJson(response));
			doPayloadBackup(request.getDeviceId(), RequestTypeEnum.PAYMENT_STATUS_CHECK.name(), requestTime, responseTime, cbsPaymentStatusUrl, request, response);

		}catch(HttpStatusCodeException hsce){
			logger.error("Error calling CBS", hsce);
			String errorMessage;
			if(hsce.getResponseBodyAsString() != null){
				errorMessage = gson.fromJson(hsce.getResponseBodyAsString(), Map.class).get(MESSAGE).toString();
				logger.error("Error message from CBS: {}", errorMessage);
			}else{
				errorMessage = "Unknown error from CBS. Please contact an admin";
			}
			if(hsce.getStatusCode() == HttpStatus.OK){
				response.setStatus(ResponseCodeEnum.ERROR.getCode());
				response.setCode(ResponseCodeEnum.ERROR.getCode());
				response.setMessage("Invalid response from CBS. Please check that entered details are correct");
			}else if(hsce.getStatusCode() == HttpStatus.CONFLICT){
				response.setStatus(ResponseCodeEnum.LOCKED.getCode());
				response.setCode(ResponseCodeEnum.LOCKED.getCode());
				response.setMessage(errorMessage);
			}else{
				response.setStatus(ResponseCodeEnum.ERROR.getCode());
				response.setCode(ResponseCodeEnum.ERROR.getCode());
				response.setMessage(errorMessage);
			}
		}catch (Exception ex){
			logger.error("An error occured calling CBS", ex);
		}

		return response;
	}

	private MayBeachRequestResponse processCbsResponseV3(ResponseEntity<Map> response){
		MayBeachRequestResponse paymentStatusResponse = new MayBeachRequestResponse();
		Map cbsResponseMap = (null != response && null != response.getBody()) ? response.getBody() : null;
		if(null != cbsResponseMap) {
			//get the code and message from the response
			String cbsResponseMessage = String.valueOf(cbsResponseMap.get(MESSAGE));
			int cbsResponseCode = (Integer) cbsResponseMap.get(CODE_STR);

			//translate the response
			ResponseCodeEnum responseCodeEnum = getResponseCodeEnumV3(cbsResponseMessage);
			paymentStatusResponse.setStatus(responseCodeEnum.getCode());
			paymentStatusResponse.setCode(responseCodeEnum.getCode());
			paymentStatusResponse.setMessage(cbsResponseMessage);
			
			if (cbsResponseCode == 200) {
				paymentStatusResponse.setData(cbsResponseMap.get("data"));
			}

		}

		return paymentStatusResponse;
	}

	private ResponseCodeEnum getResponseCodeEnumV3(String cbsResponseMessage){
		ResponseCodeEnum responseCodeEnum;
		switch (cbsResponseMessage) {
			case "NOT_USED":
				responseCodeEnum = ResponseCodeEnum.PROCEED;
				break;
			case "USED":
				responseCodeEnum = ResponseCodeEnum.USED;
				break;
			case "LOCKED":
				responseCodeEnum = ResponseCodeEnum.LOCKED;
				break;
			default:
				responseCodeEnum = ResponseCodeEnum.UNKNOWN;
				break;
		}

		return responseCodeEnum;
	}

	private ResponseCodeEnum getResponseCodeEnum(boolean useCaseMatched, boolean notUsed, boolean hasMissingServiceTypes, boolean hasUnpaidServiceTypes) {
		if(!notUsed){
			return ResponseCodeEnum.USED;
		}
		if(!useCaseMatched){
			return ResponseCodeEnum.WRONG_USE_CASE;
		}
		if(hasUnpaidServiceTypes){
			return ResponseCodeEnum.UNPAID_SERVICE_TYPES;
		}
		if(hasMissingServiceTypes){
			return ResponseCodeEnum.PROCEED_BUT_MISSING_SERVICE_TYPES;
		}
		return ResponseCodeEnum.PROCEED;
	}

	private String getMissingTypesDescription(ResponseCodeEnum responseCodeEnum, List<String> missingServiceTypes) {
		return responseCodeEnum.getDescription().replace("{}", StringUtils.join(missingServiceTypes, ", ")).replace("-", " ");
	}

}
