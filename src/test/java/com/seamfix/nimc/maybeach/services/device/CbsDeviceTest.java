package com.seamfix.nimc.maybeach.services.device;

import com.seamfix.nimc.maybeach.configs.AppConfig;
import com.seamfix.nimc.maybeach.dto.CbsDeviceActivationRequest;
import com.seamfix.nimc.maybeach.dto.CbsDeviceCertificationRequest;
import com.seamfix.nimc.maybeach.dto.CbsDeviceUserLoginRequest;
import com.seamfix.nimc.maybeach.dto.CbsHeartBeatsRequest;
import com.seamfix.nimc.maybeach.dto.CbsRequestResponse;
import com.seamfix.nimc.maybeach.dto.CbsResponse;
import com.seamfix.nimc.maybeach.dto.DeviceActivationDataPojo;
import com.seamfix.nimc.maybeach.dto.DeviceInfo;
import com.seamfix.nimc.maybeach.services.jms.JmsSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class CbsDeviceTest {

    @Autowired
    private CbsDeviceService target;

    @Autowired
    private RestTemplate restTemplate;

    private JmsSender jmsSender;

    @Autowired
    private AppConfig appConfig;


    @BeforeEach
    public void init(){
        target = new CbsDeviceService();
        target.setRestTemplate(restTemplate);
        target.setAppConfig(appConfig);
        jmsSender = Mockito.mock(JmsSender.class);
        target.setJmsSender(jmsSender);
    }

    @Test
    public void sendDeviceActivationRequest_ForDuplicateRequestId_ShouldReturnConflict() {
        CbsDeviceActivationRequest deviceActivationRequest = new CbsDeviceActivationRequest();
        deviceActivationRequest.setMachineTag("DROID-S120-NNEOMS-" + System.currentTimeMillis());
        String deviceId = "INFINIX-88888" + System.currentTimeMillis();
        deviceActivationRequest.setProviderDeviceIdentifier(deviceId);
        deviceActivationRequest.setActivationLocationLongitude("3.47182494");
        deviceActivationRequest.setActivationLocationLatitude("6.4380415");
        deviceActivationRequest.setRequesterLastname("Nwachukwu");
        deviceActivationRequest.setRequesterFirstname("Nneoma");
        deviceActivationRequest.setRequesterEmail("nneoma@yopmail.com");
        deviceActivationRequest.setRequesterPhoneNumber("08099887766");
        deviceActivationRequest.setRequesterNin("11111111111");
        deviceActivationRequest.setEsaName("SEAMFIX");
        deviceActivationRequest.setEsaCode("NM0093");
        deviceActivationRequest.setRequestId(deviceId);
        deviceActivationRequest.setLocation("LEKKI");

        target.sendDeviceActivationRequest(deviceActivationRequest);
        CbsRequestResponse response = (CbsRequestResponse) target.sendDeviceActivationRequest(deviceActivationRequest);

        assertNotNull(response);
        assertEquals(409, response.getCode());
        assertEquals("Duplicate request Id " + deviceId, response.getMessage());
    }

    @Test
    public void sendDeviceActivationRequest_ForValidRequest_ShouldReturnSuccess() {
        CbsDeviceActivationRequest deviceActivationRequest = new CbsDeviceActivationRequest();
        deviceActivationRequest.setMachineTag("DROID-S120-NNEOMS-" + System.currentTimeMillis());
        String deviceId = "INFINIX-88888" + System.currentTimeMillis();
        deviceActivationRequest.setProviderDeviceIdentifier(deviceId);
        deviceActivationRequest.setActivationLocationLongitude("3.47182494");
        deviceActivationRequest.setActivationLocationLatitude("6.4380415");
        deviceActivationRequest.setRequesterLastname("Nwachukwu");
        deviceActivationRequest.setRequesterFirstname("Nneoma");
        deviceActivationRequest.setRequesterEmail("nneoma@yopmail.com");
        deviceActivationRequest.setRequesterPhoneNumber("08099887766");
        deviceActivationRequest.setRequesterNin("11111111111");
        deviceActivationRequest.setEsaName("SEAMFIX");
        deviceActivationRequest.setEsaCode("NM0093");
        deviceActivationRequest.setRequestId(deviceId);
        deviceActivationRequest.setLocation("LEKKI");

        CbsRequestResponse response = (CbsRequestResponse) target.sendDeviceActivationRequest(deviceActivationRequest);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Successful", response.getMessage());
    }

    @Test
    public void sendDeviceCertificationRequest_ForUnknownDevice_ShouldReturnNotFound() {
        CbsDeviceCertificationRequest deviceCertificationRequest = new CbsDeviceCertificationRequest();

        deviceCertificationRequest.setDeviceId("TEST-UNKNOWN-DEVICE");
        String certifierLoginId = "12345678995";
        deviceCertificationRequest.setCertifierLoginId(certifierLoginId);
        deviceCertificationRequest.setCurrentLocationLatitude(9.133649);
        deviceCertificationRequest.setCurrentLocationLongitude(7.351158);
        deviceCertificationRequest.setRequestedByLastName("Nwachukwu");
        deviceCertificationRequest.setRequestedByFirstName("Nneoma");

        CbsRequestResponse response = (CbsRequestResponse) target.sendDeviceCertificationRequest(deviceCertificationRequest);

        assertNotNull(response);
        assertEquals(400, response.getCode());
        assertEquals("Device with id TEST-UNKNOWN-DEVICE not found for provider Seamfix ", response.getMessage());
    }

    @Test
    public void sendDeviceCertificationRequest_ForValidRequest_ShouldReturnSuccess() {
        CbsDeviceCertificationRequest deviceCertificationRequest = new CbsDeviceCertificationRequest();

        deviceCertificationRequest.setDeviceId("MANTRA-911573953260076");
        String certifierLoginId = "12345678995";
        deviceCertificationRequest.setCertifierLoginId(certifierLoginId);
        deviceCertificationRequest.setCurrentLocationLatitude(9.133649);
        deviceCertificationRequest.setCurrentLocationLongitude(7.351158);
        deviceCertificationRequest.setRequestedByLastName("Nwachukwu");
        deviceCertificationRequest.setRequestedByFirstName("Nneoma");

        CbsRequestResponse response = (CbsRequestResponse) target.sendDeviceCertificationRequest(deviceCertificationRequest);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Request Successful", response.getMessage());
    }

    @Test
    public void sendFetchActivationDataRequest_ForUnactivatedRequest_ShouldReturnPending() {

        String deviceId = "X0-X0-X0-X0-X0";
        String requestId = "J001";

        CbsRequestResponse response = (CbsRequestResponse) target.sendFetchActivationDataRequest(deviceId,requestId);

        assertNotNull(response);
        assertEquals(202, response.getCode());
        assertEquals("Activation Request Status is Pending", response.getMessage());
    }

    @Test
    public void sendFetchActivationDataRequest_ForActivatedRequest_ShouldReturnSuccess() {

        String deviceId = "MANTRA-911573953260076";
        String requestId = "MANTRA-911573953260076-1651044318649";

        CbsRequestResponse response = (CbsRequestResponse) target.sendFetchActivationDataRequest(deviceId,requestId);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Request Successful", response.getMessage());
    }

    @Test
    public void sendDeviceUserLoginRequest_ForInvalidRequest_ShouldReturnUnauthorized() {
        CbsDeviceUserLoginRequest userLoginRequest = new CbsDeviceUserLoginRequest();

        userLoginRequest.setDeviceId("SAMSUNG-352231116003570");
        userLoginRequest.setLoginId("UNKNOWN_USER");
        userLoginRequest.setPassword("password");

        CbsRequestResponse response = (CbsRequestResponse) target.sendDeviceUserLoginRequest(userLoginRequest);

        assertNotNull(response);
        assertEquals(401, response.getCode());
        assertEquals("Invalid login details!", response.getMessage());
    }

    @Test
    public void sendDeviceUserLoginRequest_ForValidRequest_ShouldReturnSuccess() {
        CbsDeviceUserLoginRequest userLoginRequest = new CbsDeviceUserLoginRequest();

        userLoginRequest.setDeviceId("SAMSUNG-352231116003570");
        userLoginRequest.setLoginId("12345678995");
        userLoginRequest.setPassword("P@ssw0rd!");

        CbsRequestResponse response = (CbsRequestResponse) target.sendDeviceUserLoginRequest(userLoginRequest);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Request was successful!", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    public void sendDeviceCertificationRequest_ForNullDeviceId_ShouldReturnViolationMessage() {
        CbsDeviceCertificationRequest deviceCertificationRequest = new CbsDeviceCertificationRequest();

        deviceCertificationRequest.setDeviceId(null);
        deviceCertificationRequest.setCertifierLoginId("12345678995");
        deviceCertificationRequest.setCurrentLocationLatitude(-9.133649);
        deviceCertificationRequest.setCurrentLocationLongitude(7.351158);
        deviceCertificationRequest.setRequestedByLastName("Test");
        deviceCertificationRequest.setRequestedByFirstName("Test");

        CbsResponse response = target.sendDeviceCertificationRequest(deviceCertificationRequest);

        assertNotNull(response);
        assertEquals(-1, response.getCode());
        assertEquals("Please provide the device ID", response.getMessage());
    }
    @Test
    public void sendDeviceCertificationRequest_ForNullCertifierID_ShouldReturnViolationMessage() {
        CbsDeviceCertificationRequest deviceCertificationRequest = new CbsDeviceCertificationRequest();

        deviceCertificationRequest.setDeviceId("X0-X0-X0-X0-X0");
        String certifierLoginId = null;
        deviceCertificationRequest.setCertifierLoginId(certifierLoginId);
        deviceCertificationRequest.setCurrentLocationLatitude(-9.133649);
        deviceCertificationRequest.setCurrentLocationLongitude(7.351158);
        deviceCertificationRequest.setRequestedByLastName("Test");
        deviceCertificationRequest.setRequestedByFirstName("Test");

        CbsResponse response = target.sendDeviceCertificationRequest(deviceCertificationRequest);

        assertNotNull(response);
        assertEquals(-1, response.getCode());
        assertEquals("Please provide the certifier login ID", response.getMessage());
    }
    @Test
    public void sendDeviceCertificationRequest_ForNullCoordinates_ShouldReturnSuccessMessage() {
        CbsDeviceCertificationRequest deviceCertificationRequest = new CbsDeviceCertificationRequest();

        deviceCertificationRequest.setDeviceId("SAMSUNG-352231116003570");
        String certifierLoginId = "12345678995";
        deviceCertificationRequest.setCertifierLoginId(certifierLoginId);
        deviceCertificationRequest.setCurrentLocationLongitude(7.351158);
        deviceCertificationRequest.setRequestedByLastName("Test");
        deviceCertificationRequest.setRequestedByFirstName("Test");

        CbsResponse response = target.sendDeviceCertificationRequest(deviceCertificationRequest);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Request Successful", response.getMessage());
    }

    @Test
    public void sendHeartBeats_ShouldReturn400_WhenInvalidBodyParametersIsPassed() {
        CbsHeartBeatsRequest heartBeatsRequest = new CbsHeartBeatsRequest();
        heartBeatsRequest.setDeviceId("SAMSUNG-352231116003570");
        CbsResponse response = target.sendHeartBeats(heartBeatsRequest);
        assertNotNull(response);
        assertEquals(400, response.getCode());
    }

    @Test
    public void sendHeartBeats_ShouldReturn404_WhenInvalidDeviceIdIsPassed() {
        CbsHeartBeatsRequest heartBeatsRequest = new CbsHeartBeatsRequest();
        heartBeatsRequest.setClientAppUserLoginIdentifier("11111111001");
        heartBeatsRequest.setClientAppVersion("1.30");
        heartBeatsRequest.setCurrentLocationLatitude(0.0);
        heartBeatsRequest.setCurrentLocationLongitude(0.0);
        heartBeatsRequest.setDeviceId("invalid-device-id");
        heartBeatsRequest.setEsaCode("NM0073");

        CbsResponse response = target.sendHeartBeats(heartBeatsRequest);
        assertNotNull(response);
        assertEquals(404, response.getCode());
    }

    @Test
    public void sendHeartBeats_ShouldReturn200_WhenValidBodyParametersIsPassed() {
        CbsHeartBeatsRequest heartBeatsRequest = new CbsHeartBeatsRequest();
        String nin = "11111111001";
        heartBeatsRequest.setClientAppUserLoginIdentifier(nin);
        heartBeatsRequest.setClientAppVersion("1.30");
        heartBeatsRequest.setCurrentLocationLatitude(0.0);
        heartBeatsRequest.setCurrentLocationLongitude(0.0);
        heartBeatsRequest.setDeviceId("SAMSUNG-352231116003570");
        heartBeatsRequest.setEsaCode("NM0073");
        heartBeatsRequest.setDeviceType("MOBILE");
        heartBeatsRequest.setOsType("ANDROID");
        heartBeatsRequest.setOsVersion("10");
        heartBeatsRequest.setProviderCode("NM0073");
        heartBeatsRequest.setDeviceDescription("ATMS Smart Client");
        heartBeatsRequest.setAgentNin(nin);
        heartBeatsRequest.setCountOfAllTimeEnrolments(3);
        heartBeatsRequest.setCountOfAllTimeEnrolmentsSentToNimcBackend(30);
        heartBeatsRequest.setLastTrackingId("0QVETGFSQ2YAPD3");
        heartBeatsRequest.setCountOfAllTimeIncompleteEnrollments(20);
        heartBeatsRequest.setVpnConnectionStatus(false);
        heartBeatsRequest.setCountOfAllTimeOutlierEnrollments(50);
        heartBeatsRequest.setCountOfAllTimeEnrollmentNotificationsSentToCbs(10);
        heartBeatsRequest.setConnectedDevices(Collections.singletonList(
                new DeviceInfo("FINGERPRINT_SCANNER")
        ));

        CbsResponse response = target.sendHeartBeats(heartBeatsRequest);
        assertNotNull(response);
        assertEquals(200, response.getCode());
    }
    @Test
    public void sendFetchActivationDataRequest_ForActivatedDevice_ShouldReturnJurisdiction() {

        String deviceId = "MANTRA-911573953260076";
        String requestId = "MANTRA-911573953260076-1651044318649";

        CbsRequestResponse response = (CbsRequestResponse) target.sendFetchActivationDataRequest(deviceId,requestId);
        DeviceActivationDataPojo data = (DeviceActivationDataPojo) response.getData();

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(data);
        assertNotNull(data.getFep());
        assertNotNull(data.getFep().getJurisdiction());

    }
}