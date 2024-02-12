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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    public HttpHeaders generateMayBeachHeaders(HttpHeaders headers){
        headers.set(HttpHeaders.AUTHORIZATION, settingsService.getSettingValue(SettingsEnum.MAYBEACH_AUTHORIZATION));
        headers.set(NWP_TOKEN, settingsService.getSettingValue(SettingsEnum.MAYBEACH_TOKEN));
        return headers;
    }

    public Map login(CbsDeviceUserLoginRequest request, String url) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers = generateMayBeachHeaders(headers);
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
        headers = generateMayBeachHeaders(headers);
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

    public Map deviceActivationRequest(CbsDeviceActivationRequest deviceCertificationRequest, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers = generateMayBeachHeaders(headers);
        Map<String, Object> payload = new ConcurrentHashMap();
        payload.put(DEVICE_ID, deviceCertificationRequest.getProviderDeviceIdentifier());
        payload.put("agent_id", deviceCertificationRequest.getEsaCode());
        payload.put("longitude", deviceCertificationRequest.getActivationLocationLongitude());
        payload.put("latitude", deviceCertificationRequest.getActivationLocationLatitude());
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

    public Map fetchActivationData(String deviceId, String requestId, String url) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers = generateMayBeachHeaders(headers);
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
    private static HttpEntity<Map<String, Object>> buildGraphQLRequest(String mutation, String query, HttpHeaders headers) {

        Map<String, Object> variables = new ConcurrentHashMap<>();
        variables.put("metadata", query);

        Map<String, Object> requestBody = new ConcurrentHashMap<>();
        requestBody.put("query", mutation);
        requestBody.put("variables", variables);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        return requestEntity;
    }

}
