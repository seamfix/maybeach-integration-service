package com.seamfix.nimc.maybeach.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class GraphQLUtility {

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
        payload.put("device_id", request.getDeviceId());
        payload.put("username", request.getLoginId());
        payload.put("password", request.getPassword());
        String mutation = "mutation ($metadata: String) {\n" +
                "  deviceUserLogin(input: [{\n" +
                "    metadata: $metadata\n" +
                "  }]) {\n" +
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

        log.info("Response from MayBeach: {}", requestEntity);

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
        payload.put("device_id", deviceId);
        String mutation = "mutation ($metadata: String) {\n" +
                "  deviceCertificationRequest(input: [{\n" +
                "    metadata: $metadata\n" +
                "  }]) {\n" +
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

        log.info("Response from MayBeach: {}", requestEntity);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            response = objectMapper.readValue(responseBody, Map.class);
        }
        else {
            response = extractErrorData(responseEntity, response);
        }
        return response;
    }

    public Map deviceActivationRequest(CbsDeviceCertificationRequest deviceCertificationRequest, String url) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers = generateMayBeachHeaders(headers);
        Map<String, Object> payload = new ConcurrentHashMap();
        payload.put("device_id", deviceCertificationRequest.getDeviceId());
        payload.put("agent_id", deviceCertificationRequest.getCertifierLoginId());
        payload.put("longitude", deviceCertificationRequest.getCurrentLocationLongitude());
        payload.put("latitude", deviceCertificationRequest.getCurrentLocationLatitude());
        String mutation = "mutation ($metadata: String) {\n" +
                "  deviceActivationRequest(input: [{\n" +
                "    metadata: $metadata\n" +
                "  }]) {\n" +
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

        log.info("Response from MayBeach: {}", requestEntity);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            response = objectMapper.readValue(responseBody, Map.class);
        }
        else {
            response = extractErrorData(responseEntity, response);
        }
        return response;
    }

    public Map fetchActivationData(String deviceId, String requestId, String url) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers = generateMayBeachHeaders(headers);
        Map<String, Object> payload = new ConcurrentHashMap();
        payload.put("device_id", deviceId);
        payload.put("requestId", requestId);

        String mutation = "mutation ($metadata: String) {\n" +
                "  fetchActivationData(input: [{\n" +
                "    metadata: $metadata\n" +
                "  }]) {\n" +
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

        log.info("Response from MayBeach: {}", requestEntity);

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
