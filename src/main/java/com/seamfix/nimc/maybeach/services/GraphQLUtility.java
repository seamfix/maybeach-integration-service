package com.seamfix.nimc.maybeach.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seamfix.nimc.maybeach.dto.CbsDeviceActivationRequest;
import com.seamfix.nimc.maybeach.dto.CbsDeviceCertificationRequest;
import com.seamfix.nimc.maybeach.dto.CbsDeviceUserLoginRequest;
import com.seamfix.nimc.maybeach.enums.SettingsEnum;
import com.seamfix.nimc.maybeach.utils.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class GraphQLUtility {

    @SuppressWarnings("PMD.FieldNamingConventions")
    private static final String RESPONSE_FROM_MAY_BEACH = "Response from MayBeach: {}";
    @SuppressWarnings("PMD.FieldNamingConventions")
    private static final String META_DATA = "mutation {\n";
    @SuppressWarnings("PMD.FieldNamingConventions")
    private static final String DEVICE_ID = "device_id";

    private final SettingService settingsService;
    private final RestTemplate restTemplate;
    private static final String NWP_TOKEN = "Nwp-Token";
    private final ObjectMapper objectMapper;

    public void generateMayBeachHeaders(HttpHeaders headers){
        log.info("graphql - onboardingDeviceRequest generateMayBeachHeader");
        headers.set(HttpHeaders.AUTHORIZATION, settingsService.getSettingValue(SettingsEnum.MAYBEACH_AUTHORIZATION));
        headers.set(NWP_TOKEN, settingsService.getSettingValue(SettingsEnum.MAYBEACH_TOKEN));
        log.info("graphql - onboardingDeviceRequest generateMayBeachHeader");
    }

    public Map<String, Object> login(CbsDeviceUserLoginRequest request, String url) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        generateMayBeachHeaders(headers);
        Map<String, Object> payload = new ConcurrentHashMap<>();
        payload.put(DEVICE_ID, request.getDeviceId());
        payload.put("username", request.getLoginId());
        payload.put("password", request.getPassword());

        try {
            String payloadJSon = convertToJsonString(payload);
            String mutation = META_DATA +
                    "  deviceUserLogin(input:  " + payloadJSon + " ) {\n" +
                    "    id\n" +
                    "    agentfirstname\n" +
                    "    agentlastname\n" +
                    "    agentemail\n" +
                    "  }\n" +
                    "}";

            Map<String, Object> response = new ConcurrentHashMap<>();
            HttpEntity<Map<String, Object>> requestEntity = buildGraphQLRequest(mutation, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info(RESPONSE_FROM_MAY_BEACH, requestEntity);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String responseBody = responseEntity.getBody();
                if (null != responseBody && !responseBody.contains("id")){
                    return new HashMap<>();
                }
                response = objectMapper.readValue(responseBody, Map.class);
            } else {
                response = extractErrorData(responseEntity, response);
            }
            return response;
        }
        catch (JsonProcessingException e){
            log.error("Exception caught while processing request");
        }
        return new HashMap<>();
    }

    public Map<String, Object> deviceCertificationRequest(String deviceId, String url) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        generateMayBeachHeaders(headers);
        Map<String, Object> payload = new ConcurrentHashMap();
        payload.put(DEVICE_ID, deviceId);
        String mutation = META_DATA +
                "  deviceCertificationRequest(input:  " + convertToJsonString(payload) + " ) {\n" +
                "    id\n" +
                "    code\n" +
                "    message\n" +
                "  }\n" +
                "}";

        Map<String, Object> response = new ConcurrentHashMap<>();
        HttpEntity<Map<String, Object>> requestEntity = buildGraphQLRequest(mutation, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        log.info(RESPONSE_FROM_MAY_BEACH, requestEntity);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            if(!responseBody.contains("id")){
                return new HashMap<>();
            }
            response = objectMapper.readValue(responseBody, Map.class);
        }
        else {
            response = extractErrorData(responseEntity, response);
        }
        return response;
    }

    public Map deviceActivationRequest(CbsDeviceCertificationRequest deviceCertificationRequest, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        generateMayBeachHeaders(headers);
        Map<String, Object> payload = new ConcurrentHashMap();
        payload.put(DEVICE_ID, deviceCertificationRequest.getDeviceId());
        payload.put("agent_id", deviceCertificationRequest.getRequestedByProviderIdentifier());
        payload.put("longitude", deviceCertificationRequest.getCurrentLocationLongitude());
        payload.put("latitude", deviceCertificationRequest.getCurrentLocationLatitude());
        try {
        String mutation = META_DATA +
                "  deviceActivationRequest(input:  " + convertToJsonString(payload) + " ) {\n"+
                "    code\n" +
                "    message\n" +
                "  }\n" +
                "}";

        Map response = new ConcurrentHashMap();

            HttpEntity<Map<String, Object>> requestEntity = buildGraphQLRequest(mutation, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info(RESPONSE_FROM_MAY_BEACH, requestEntity);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String responseBody = responseEntity.getBody();
                if(!responseBody.contains("code")){
                    return new HashMap<>();
                }
                response = objectMapper.readValue(responseBody, Map.class);
            } else {
                response = extractErrorData(responseEntity, response);
            }
            return response;
        }catch (IOException e){
            Utility.logError("Exception occurred:: deviceActivationRequest: {}", e.getMessage());
        }
        return new HashMap<>();
    }

    public Map onboardingDeviceRequest(CbsDeviceActivationRequest deviceCertificationRequest, String url) {
        log.info("graphql - onboardingDeviceRequest: {}", deviceCertificationRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        generateMayBeachHeaders(headers);
        Map<String, Object> payload = getOnboardingObjectMap(deviceCertificationRequest);

        try {
            String payloadJSon = convertToJsonString(payload);

            String mutation = "mutation {\n" +
                    "  onboardingDeviceRequest(input:  " + payloadJSon + " ) {\n" +
                    "    status\n" +
                    "  }\n" +
                    "}";

            Map response = new ConcurrentHashMap();

            HttpEntity<Map<String, Object>> requestEntity = buildGraphQLRequest(mutation, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info(RESPONSE_FROM_MAY_BEACH, requestEntity);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String responseBody = responseEntity.getBody();
                if (!responseBody.contains("status")) {
                    Utility.logError("onboardingDeviceRequest Response error:: {}", responseEntity.getBody());
                    return new HashMap<>();
                }
                response = objectMapper.readValue(responseBody, Map.class);
            } else {
                response = extractErrorData(responseEntity, response);
            }
            return response;
        }catch (IOException e){
            log.error("Exception occurred:: deviceActivationRequest: {}", new HashMap<>());
        }
        return new HashMap();
    }

    private static Map<String, Object> getOnboardingObjectMap(CbsDeviceActivationRequest deviceCertificationRequest) {
        Map<String, Object> payload = new ConcurrentHashMap<>();
        payload.put("date", getCurrentTimestamp());
        payload.put("type", "mobile");
        payload.put("model", deviceCertificationRequest.getModel());
        payload.put("dev_id", deviceCertificationRequest.getProviderDeviceIdentifier());
        payload.put("partner", deviceCertificationRequest.getEsaCode());
        payload.put("fep_agent_nin", deviceCertificationRequest.getRequesterNin());
        payload.put("location", deviceCertificationRequest.getLocation());

        payload.put("imei", deviceCertificationRequest.getImei());
        payload.put("machine_tag", deviceCertificationRequest.getMachineTag());
        payload.put("first_name", deviceCertificationRequest.getRequesterFirstname());
        payload.put("last_name", deviceCertificationRequest.getRequesterLastname());
        payload.put("email", deviceCertificationRequest.getRequesterEmail());

        payload.put("phone_number", deviceCertificationRequest.getRequesterPhoneNumber());
        payload.put("esa_name", deviceCertificationRequest.getEsaName());
        payload.put("requestid", deviceCertificationRequest.getRequestId());

        payload.put("longitude", deviceCertificationRequest.getActivationLocationLongitude());
        payload.put("latitude", deviceCertificationRequest.getActivationLocationLatitude());
        return payload;
    }

    private static int getCurrentTimestamp() {
        Instant instant = Instant.now();
        long timestampSeconds = instant.getEpochSecond();
        return Math.toIntExact(timestampSeconds);
    }
    public Map callOnboardingRequestStatus(String deviceId, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        generateMayBeachHeaders(headers);
        Map<String, Object> payload = new ConcurrentHashMap();
        payload.put("dev_id", deviceId);
        try {
        String mutation = META_DATA +
                "  deviceStatus(input:  " + convertToJsonString(payload) + " ) {\n" +
                "    status\n" +
                "    sub_status\n" +
                "    status_msg\n" +
                "  }\n" +
                "}";

        Map response = new ConcurrentHashMap();

            HttpEntity<Map<String, Object>> requestEntity = buildGraphQLRequest(mutation, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info(RESPONSE_FROM_MAY_BEACH, requestEntity);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String responseBody = responseEntity.getBody();
                if(!responseBody.contains("status")){
                    return new HashMap();
                }
                response = objectMapper.readValue(responseBody, Map.class);
            } else {
                response = extractErrorData(responseEntity, response);
            }
            return response;
        }catch (IOException e){
            Utility.logError("Exception occurred:: callOnboardingRequestStatus: {}", e.getMessage());
        }
        return new HashMap<>();
    }

    public Map demographicsDataPreEnrolmentVerificationRequest(CbsDeviceActivationRequest deviceCertificationRequest, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        generateMayBeachHeaders(headers);
        Map<String, Object> payload = new ConcurrentHashMap();
        payload.put(DEVICE_ID, deviceCertificationRequest.getProviderDeviceIdentifier());
        payload.put("date", Timestamp.from(Calendar.getInstance().toInstant()));
        payload.put("partner", deviceCertificationRequest.getEsaCode());
        payload.put("fep_agent_nin", deviceCertificationRequest.getRequesterNin());
        payload.put("machine_tag", deviceCertificationRequest.getMachineTag());
        payload.put("first_name", deviceCertificationRequest.getRequesterFirstname());
        payload.put("last_name", deviceCertificationRequest.getRequesterLastname());
        payload.put("email", deviceCertificationRequest.getRequesterEmail());
        payload.put("phone_number", deviceCertificationRequest.getRequesterPhoneNumber());
        payload.put("esa_name", deviceCertificationRequest.getEsaName());
        payload.put("requestid", deviceCertificationRequest.getRequestId());
        payload.put("longitude", deviceCertificationRequest.getActivationLocationLongitude());
        payload.put("latitude", deviceCertificationRequest.getActivationLocationLatitude());
        try {
            String mutation = META_DATA +
                    "  demographicsDataPreEnrolmentVerification(input:  " + convertToJsonString(payload) + " ) {\n" +
                    "    code\n" +
                    "    message\n" +
                    "  }\n" +
                    "}";

            Map response = new ConcurrentHashMap();

            HttpEntity<Map<String, Object>> requestEntity = buildGraphQLRequest(mutation, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info(RESPONSE_FROM_MAY_BEACH, requestEntity);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String responseBody = responseEntity.getBody();
                if (!responseBody.contains("message")) {
                    return new HashMap<>();
                }
                response = objectMapper.readValue(responseBody, Map.class);
            } else {
                response = extractErrorData(responseEntity, response);
            }
            return response;
        }catch (IOException e){
            Utility.logError("Exception occurred:: demographicsDataPreEnrolmentVerificationRequest: {}", e.getMessage());
        }
        return new HashMap<>();
    }

    public Map fetchActivationData(String deviceId, String requestId, String url) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        generateMayBeachHeaders(headers);
        Map<String, Object> payload = new ConcurrentHashMap<>();
        payload.put(DEVICE_ID, deviceId);
        payload.put("requestId", requestId);

        String mutation = META_DATA +
                "  fetchActivationData(input:  " + convertToJsonString(payload) + " ) {\n"+
                "    id\n" +
                "    code\n" +
                "    message\n" +
                "  }\n" +
                "}";

        Map<String, Object> response = new ConcurrentHashMap<>();
        HttpEntity<Map<String, Object>> requestEntity = buildGraphQLRequest(mutation, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        log.info(RESPONSE_FROM_MAY_BEACH, requestEntity);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            if(null != responseBody && !responseBody.contains("message")){
                return new HashMap<>();
            }
            response = objectMapper.readValue(responseBody, Map.class);
        }
        else {
            response = extractErrorData(responseEntity, response);
        }
        return response;
    }

    private Map<String, Object> extractErrorData(ResponseEntity<String> responseEntity, Map<String, Object> response){
        try {
            String responseBody = responseEntity.getBody();
            return objectMapper.readValue(responseBody, Map.class);
        }catch (JsonProcessingException e){
            return response;
        }
    }

    @NotNull
    private static HttpEntity<Map<String, Object>> buildGraphQLRequest(String mutation, HttpHeaders headers) {

        Map<String, Object> requestBody = new ConcurrentHashMap<>();
        requestBody.put("query", mutation);

        log.info("buildGraphQLRequest requestBody: {}", requestBody);
        return new HttpEntity<>(requestBody, headers);

    }

    private static String convertToJsonString(Map<String, Object> payload) throws JsonProcessingException {
        String payloadString = payload.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + (entry.getValue() instanceof String ? "\"" + entry.getValue() + "\"" : entry.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));

        log.info("Payload string: {}", payloadString);
        return payloadString;
    }

}
