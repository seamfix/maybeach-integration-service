package com.seamfix.nimc.maybeach.services.payment;

import com.seamfix.nimc.maybeach.configs.AppConfig;
import com.seamfix.nimc.maybeach.dto.CbsPaymentRequest;
import com.seamfix.nimc.maybeach.dto.CbsPaymentStatusRequest;
import com.seamfix.nimc.maybeach.dto.MayBeachResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CbsPaymentServiceTest {

    @Autowired
    private MayBeachPaymentService underTest;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AppConfig appConfig;

    private String esaCode = "NM0093";

//    @Test
    void consumePayment_shouldThrowException_whenRequiredCredentialsAreNotPassed() {
        assertThrows(HttpClientErrorException.class, () -> underTest.consumePayment(new CbsPaymentRequest()));
    }

//    @Test
    void getPaymentStatus_shouldReturnSuccess() {
        var response = underTest.getPaymentStatus(esaCode, "12345", "MANTRA-911573953260076");
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }

//    @Test
    void getPaymentStatus_shouldReturnException_whenDeviceIdNotFound() {
        assertThrows(HttpClientErrorException.class, () -> underTest.getPaymentStatus(esaCode, "12345", "X0-X0-X0-X0-X0"));
    }

//    @Test
    void getPaymentStatusV2_shouldFailWhenNoServiceTypeIsPassed() {
        MayBeachResponse response = underTest.getPaymentStatusV2(new CbsPaymentStatusRequest());
        assertNotNull(response);
        assertEquals(-1, response.getCode());
    }
}