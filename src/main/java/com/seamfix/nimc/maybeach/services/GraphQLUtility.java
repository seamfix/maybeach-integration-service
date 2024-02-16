package com.seamfix.nimc.maybeach.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seamfix.nimc.maybeach.dto.CbsDeviceActivationRequest;
import com.seamfix.nimc.maybeach.dto.CbsDeviceCertificationRequest;
import com.seamfix.nimc.maybeach.dto.CbsDeviceUserLoginRequest;
import com.seamfix.nimc.maybeach.enums.SettingsEnum;
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
import java.util.Calendar;
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
    private static final String META_DATA = "mutation ($metadata: String) {\n";
    @SuppressWarnings("PMD.FieldNamingConventions")
    private static final String DEVICE_ID = "device_id";

    @SuppressWarnings("PMD.FieldNamingConventions")
    private final String MUTATION_REQUEST = "(input: [{\n" +
        "    metadata: $metadata\n" +
                "  }]) {\n";

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

    public Map login(CbsDeviceUserLoginRequest request, String url) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        generateMayBeachHeaders(headers);
        Map<String, String> payload = new ConcurrentHashMap();
        payload.put(DEVICE_ID, request.getDeviceId());
        payload.put("username", request.getLoginId());
        payload.put("password", request.getPassword());
        String mutation = META_DATA +
                "  deviceUserLogin"+MUTATION_REQUEST+
                "    id\n" +
                "    agentfirstname\n" +
                "    agentlastname\n" +
                "    agentemail\n" +
                "  }\n" +
                "}";
        Map response = new ConcurrentHashMap();
        HttpEntity<Map<String, Object>> requestEntity = buildGraphQLRequest(mutation, objectMapper.writeValueAsString(payload), headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        log.info(RESPONSE_FROM_MAY_BEACH, requestEntity);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            response = objectMapper.readValue(responseBody, Map.class);
        }
        else {
            response = extractErrorData(responseEntity, response);
        }
        return response;
    }

    public Map deviceCertificationRequest(String deviceId, String url) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        generateMayBeachHeaders(headers);
        Map<String, Object> payload = new ConcurrentHashMap();
        payload.put(DEVICE_ID, deviceId);
        String mutation = META_DATA +
                "  deviceCertificationRequest"+ MUTATION_REQUEST +
                "    id\n" +
                "    code\n" +
                "    message\n" +
                "  }\n" +
                "}";

        Map response = new ConcurrentHashMap();
        HttpEntity<Map<String, Object>> requestEntity = buildGraphQLRequest(mutation, objectMapper.writeValueAsString(payload), headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        log.info(RESPONSE_FROM_MAY_BEACH, requestEntity);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
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
        String mutation = META_DATA +
                "  deviceActivationRequest"+ MUTATION_REQUEST + " {\n" +
                "    code\n" +
                "    message\n" +
                "  }\n" +
                "}";

        Map response = new ConcurrentHashMap();

        try {
            HttpEntity<Map<String, Object>> requestEntity = buildGraphQLRequest(mutation, objectMapper.writeValueAsString(payload), headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info(RESPONSE_FROM_MAY_BEACH, requestEntity);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String responseBody = responseEntity.getBody();
                response = objectMapper.readValue(responseBody, Map.class);
            } else {
                response = extractErrorData(responseEntity, response);
            }
        }catch (IOException e){
            log.error("Exception occurred:: deviceActivationRequest: {}", response);
        }
        return response;
    }

    public Map onboardingDeviceRequest(CbsDeviceActivationRequest deviceCertificationRequest, String url) {
        log.info("graphql - onboardingDeviceRequest: {}", deviceCertificationRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        generateMayBeachHeaders(headers);
        Map<String, Object> payload = new ConcurrentHashMap();
        try {
            payload.put("date", 1706800234);
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

            payload.put("longitude", 9.107925670606027);
            payload.put("latitude", 9.107925670606027);
        }catch (Exception e){
            log.error("Exception graphql - onboardingDeviceRequest - {}", e.getMessage());
        }

        String payloadJSon = "";
        try{
            payloadJSon = convertToJsonString(payload);
        }catch (JsonProcessingException e){
            log.info("ddjj");
        }

        String mutation = "mutation {\n" +
            "  onboardingDeviceRequest(input:  "+payloadJSon+" ) {\n" +
                    "    status\n" +
                    "  }\n" +
                    "}";
//        "mutation ($metadata: String) {\n" +
//                "  onboardingDeviceRequest(input: {\n" +
//                "    $metadata\n" +
//                "  }]) {\n" +
//                "    status\n" +
//                "  }\n" +
//                "}";

        Map response = new ConcurrentHashMap();

        try {
            HttpEntity<Map<String, Object>> requestEntity = buildGraphQLRequest(mutation, convertToJsonString(payload), headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info(RESPONSE_FROM_MAY_BEACH, requestEntity);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("onboardingDeviceRequest is successful - {}", requestEntity.getBody());
                String responseBody = responseEntity.getBody();
                response = objectMapper.readValue(responseBody, Map.class);
            } else {
                response = extractErrorData(responseEntity, response);
            }
        }catch (IOException e){
            log.error("Exception occurred:: deviceActivationRequest: {}", response);
        }
        return response;
    }

    public Map callOnboardingRequestStatus(String deviceId, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        generateMayBeachHeaders(headers);
        Map<String, Object> payload = new ConcurrentHashMap();
        payload.put("dev_id", deviceId);
        String mutation = META_DATA +
                "  deviceStatus"+ MUTATION_REQUEST + " {\n" +
                "    status\n" +
                "    sub_status\n" +
                "    status_msg\n" +
                "  }\n" +
                "}";

        Map response = new ConcurrentHashMap();

        try {
            HttpEntity<Map<String, Object>> requestEntity = buildGraphQLRequest(mutation, objectMapper.writeValueAsString(payload), headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info(RESPONSE_FROM_MAY_BEACH, requestEntity);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String responseBody = responseEntity.getBody();
                response = objectMapper.readValue(responseBody, Map.class);
            } else {
                response = extractErrorData(responseEntity, response);
            }
        }catch (IOException e){
            log.error("Exception occurred:: deviceActivationRequest: {}", response);
        }
        return response;
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
        String mutation = META_DATA +
                "  demographicsDataPreEnrolmentVerification"+ MUTATION_REQUEST + " {\n" +
                "    code\n" +
                "    message\n" +
                "  }\n" +
                "}";

        Map response = new ConcurrentHashMap();

        try {
            HttpEntity<Map<String, Object>> requestEntity = buildGraphQLRequest(mutation, objectMapper.writeValueAsString(payload), headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info(RESPONSE_FROM_MAY_BEACH, requestEntity);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String responseBody = responseEntity.getBody();
                response = objectMapper.readValue(responseBody, Map.class);
            } else {
                response = extractErrorData(responseEntity, response);
            }
        }catch (IOException e){
            log.error("Exception occurred:: deviceActivationRequest: {}", response);
        }
        return response;
    }

    public Map fetchActivationData(String deviceId, String requestId, String url) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        generateMayBeachHeaders(headers);
        Map<String, Object> payload = new ConcurrentHashMap();
        payload.put(DEVICE_ID, deviceId);
        payload.put("requestId", requestId);

        String mutation = META_DATA +
                "  fetchActivationData"+MUTATION_REQUEST+
                "    id\n" +
                "    code\n" +
                "    message\n" +
                "  }\n" +
                "}";

        Map response = new ConcurrentHashMap();
        HttpEntity<Map<String, Object>> requestEntity = buildGraphQLRequest(mutation, objectMapper.writeValueAsString(payload), headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        log.info(RESPONSE_FROM_MAY_BEACH, requestEntity);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            response = objectMapper.readValue(responseBody, Map.class);
        }
        else {
            response = extractErrorData(responseEntity, response);
        }
        return response;
    }

    private Map extractErrorData(ResponseEntity<String> responseEntity, Map response){
        try {
            String responseBody = responseEntity.getBody();
            return objectMapper.readValue(responseBody, Map.class);
        }catch (JsonProcessingException e){
            return response;
        }
    }

    @NotNull
    private static HttpEntity<Map<String, Object>> buildGraphQLRequest(String mutation, String payload, HttpHeaders headers) {

        log.info("buildGraphQLRequest_query: {}", payload);
//        Map<String, Object> variables = new ConcurrentHashMap<>();
//        variables.put("metadata", payload);


        mutation.replace("mutation1", payload);
        // Print the payload, mutation, and variables for debugging
        log.info("Payload: " + payload);
        log.info("Mutation: " + mutation);
//        log.info("Variables: " + variables);

        Map<String, Object> requestBody = new ConcurrentHashMap<>();
        requestBody.put("query", mutation);
//        requestBody.put("variables", variables);

        log.info("buildGraphQLRequest requestBody: {}", requestBody);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        return requestEntity;
    }

    private static String convertToJsonString(Map<String, Object> payload) throws JsonProcessingException {
        String payloadString = payload.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + (entry.getValue() instanceof String ? "\"" + entry.getValue() + "\"" : entry.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));

        log.info("Payload string: {}", payloadString);
        return payloadString;
    }

}
