package com.seamfix.nimc.maybeach.services.device;

import com.seamfix.nimc.maybeach.configs.AppConfig;
import com.seamfix.nimc.maybeach.dto.CbsDeviceActivationRequest;
import com.seamfix.nimc.maybeach.dto.CbsDeviceCertificationRequest;
import com.seamfix.nimc.maybeach.dto.CbsDeviceUserLoginRequest;
import com.seamfix.nimc.maybeach.dto.MayBeachRequestResponse;
import com.seamfix.nimc.maybeach.dto.MayBeachResponse;
import com.seamfix.nimc.maybeach.dto.DeviceActivationDataPojo;
import com.seamfix.nimc.maybeach.services.jms.JmsSender;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class CbsDeviceTest {

    @Mock
    private MayBeachDeviceService target;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JmsSender jmsSender;

    @Mock
    private AppConfig appConfig;


    @BeforeEach
    public void init(){
        target = new MayBeachDeviceService();
        target.setRestTemplate(restTemplate);
        target.setAppConfig(appConfig);
        jmsSender = Mockito.mock(JmsSender.class);
        target.setJmsSender(jmsSender);
    }

//    @Test
    public void sendDeviceActivationRequest_ForDuplicateRequestId_ShouldReturnConflict() {
        CbsDeviceActivationRequest deviceActivationRequest = new CbsDeviceActivationRequest();
        deviceActivationRequest.setMachineTag("DROID-S120-NNEOMS-" + System.currentTimeMillis());
        String deviceId = "INFINIX-88888" + System.currentTimeMillis();
        deviceActivationRequest.setProviderDeviceIdentifier(deviceId);
        deviceActivationRequest.setActivationLocationLongitude(3.47182494);
        deviceActivationRequest.setActivationLocationLatitude(6.4380415);
        deviceActivationRequest.setRequesterLastname("Nwachukwu");
        deviceActivationRequest.setRequesterFirstname("Nneoma");
        deviceActivationRequest.setRequesterEmail("nneoma@yopmail.com");
        deviceActivationRequest.setRequesterPhoneNumber("08099887766");
        deviceActivationRequest.setRequesterNin("11111111111");
        deviceActivationRequest.setEsaName("SEAMFIX");
        deviceActivationRequest.setEsaCode("NM0093");
        deviceActivationRequest.setRequestId(deviceId);
        deviceActivationRequest.setLocation("LEKKI");

        MayBeachRequestResponse response = (MayBeachRequestResponse) target.sendDeviceOnboardingRequest(deviceActivationRequest);

        assertNotNull(response);
        assertEquals(409, response.getCode());
        assertEquals("Duplicate request Id " + deviceId, response.getMessage());
    }

//    @Test
    public void sendDeviceActivationRequest_ForValidRequest_ShouldReturnSuccess() {
        CbsDeviceActivationRequest deviceActivationRequest = new CbsDeviceActivationRequest();
        deviceActivationRequest.setMachineTag("DROID-S120-NNEOMS-" + System.currentTimeMillis());
        String deviceId = "INFINIX-88888" + System.currentTimeMillis();
        deviceActivationRequest.setProviderDeviceIdentifier(deviceId);
        deviceActivationRequest.setActivationLocationLongitude(3.47182494);
        deviceActivationRequest.setActivationLocationLatitude(6.4380415);
        deviceActivationRequest.setRequesterLastname("Nwachukwu");
        deviceActivationRequest.setRequesterFirstname("Nneoma");
        deviceActivationRequest.setRequesterEmail("nneoma@yopmail.com");
        deviceActivationRequest.setRequesterPhoneNumber("08099887766");
        deviceActivationRequest.setRequesterNin("11111111111");
        deviceActivationRequest.setEsaName("SEAMFIX");
        deviceActivationRequest.setEsaCode("NM0093");
        deviceActivationRequest.setRequestId(deviceId);
        deviceActivationRequest.setLocation("LEKKI");

        MayBeachRequestResponse response = (MayBeachRequestResponse) target.sendDeviceOnboardingRequest(deviceActivationRequest);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Successful", response.getMessage());
    }

//    @Test
    public void sendDeviceCertificationRequest_ForUnknownDevice_ShouldReturnNotFound() {
        CbsDeviceCertificationRequest deviceCertificationRequest = new CbsDeviceCertificationRequest();

        deviceCertificationRequest.setDeviceId("TEST-UNKNOWN-DEVICE");
        String certifierLoginId = "12345678995";
        deviceCertificationRequest.setCertifierLoginId(certifierLoginId);
        deviceCertificationRequest.setCurrentLocationLatitude(9.133649);
        deviceCertificationRequest.setCurrentLocationLongitude(7.351158);
        deviceCertificationRequest.setRequestedByLastName("Nwachukwu");
        deviceCertificationRequest.setRequestedByFirstName("Nneoma");

        MayBeachRequestResponse response = (MayBeachRequestResponse) target.sendDeviceCertificationRequest(deviceCertificationRequest);

        assertNotNull(response);
        assertEquals(400, response.getCode());
        assertEquals("Device with id TEST-UNKNOWN-DEVICE not found for provider Seamfix ", response.getMessage());
    }

//    @Test
    public void sendFetchActivationDataRequest_ForUnactivatedRequest_ShouldReturnPending() {

        String deviceId = "X0-X0-X0-X0-X0";
        String requestId = "J001";

        MayBeachRequestResponse response = (MayBeachRequestResponse) target.sendFetchActivationDataRequest(deviceId,requestId);

        assertNotNull(response);
        assertEquals(202, response.getCode());
        assertEquals("Activation Request Status is Pending", response.getMessage());
    }

//    @Test
    public void sendFetchActivationDataRequest_ForActivatedRequest_ShouldReturnSuccess() {

        String deviceId = "MANTRA-911573953260076";
        String requestId = "MANTRA-911573953260076-1651044318649";

        MayBeachRequestResponse response = (MayBeachRequestResponse) target.sendFetchActivationDataRequest(deviceId,requestId);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Request Successful", response.getMessage());
    }

//    @Test
    public void sendDeviceUserLoginRequest_ForInvalidRequest_ShouldReturnUnauthorized() {
        CbsDeviceUserLoginRequest userLoginRequest = new CbsDeviceUserLoginRequest();

        userLoginRequest.setDeviceId("SAMSUNG-352231116003570");
        userLoginRequest.setLoginId("UNKNOWN_USER");
        userLoginRequest.setPassword("password");

        MayBeachRequestResponse response = (MayBeachRequestResponse) target.sendDeviceUserLoginRequest(userLoginRequest);

        assertNotNull(response);
        assertEquals(401, response.getCode());
        assertEquals("Invalid login details!", response.getMessage());
    }

//    @Test
    public void sendDeviceUserLoginRequest_ForValidRequest_ShouldReturnSuccess() {
        CbsDeviceUserLoginRequest userLoginRequest = new CbsDeviceUserLoginRequest();

        userLoginRequest.setDeviceId("SAMSUNG-352231116003570");
        userLoginRequest.setLoginId("12345678995");
        userLoginRequest.setPassword("P@ssw0rd!");

        MayBeachRequestResponse response = (MayBeachRequestResponse) target.sendDeviceUserLoginRequest(userLoginRequest);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Request was successful!", response.getMessage());
        assertNotNull(response.getData());
    }

//    @Test
    public void sendDeviceCertificationRequest_ForNullDeviceId_ShouldReturnViolationMessage() {
        CbsDeviceCertificationRequest deviceCertificationRequest = new CbsDeviceCertificationRequest();

        deviceCertificationRequest.setDeviceId(null);
        deviceCertificationRequest.setCertifierLoginId("12345678995");
        deviceCertificationRequest.setCurrentLocationLatitude(-9.133649);
        deviceCertificationRequest.setCurrentLocationLongitude(7.351158);
        deviceCertificationRequest.setRequestedByLastName("Test");
        deviceCertificationRequest.setRequestedByFirstName("Test");

        MayBeachResponse response = target.sendDeviceCertificationRequest(deviceCertificationRequest);

        assertNotNull(response);
        assertEquals(-1, response.getCode());
        assertEquals("Please provide the device ID", response.getMessage());
    }
//    @Test
    public void sendDeviceCertificationRequest_ForNullCertifierID_ShouldReturnViolationMessage() {
        CbsDeviceCertificationRequest deviceCertificationRequest = new CbsDeviceCertificationRequest();

        deviceCertificationRequest.setDeviceId("X0-X0-X0-X0-X0");
        String certifierLoginId = null;
        deviceCertificationRequest.setCertifierLoginId(certifierLoginId);
        deviceCertificationRequest.setCurrentLocationLatitude(-9.133649);
        deviceCertificationRequest.setCurrentLocationLongitude(7.351158);
        deviceCertificationRequest.setRequestedByLastName("Test");
        deviceCertificationRequest.setRequestedByFirstName("Test");

        MayBeachResponse response = target.sendDeviceCertificationRequest(deviceCertificationRequest);

        assertNotNull(response);
        assertEquals(-1, response.getCode());
        assertEquals("Please provide the certifier login ID", response.getMessage());
    }
//    @Test
    public void sendDeviceCertificationRequest_ForNullCoordinates_ShouldReturnSuccessMessage() {
        CbsDeviceCertificationRequest deviceCertificationRequest = new CbsDeviceCertificationRequest();

        deviceCertificationRequest.setDeviceId("SAMSUNG-352231116003570");
        String certifierLoginId = "12345678995";
        deviceCertificationRequest.setCertifierLoginId(certifierLoginId);
        deviceCertificationRequest.setCurrentLocationLongitude(7.351158);
        deviceCertificationRequest.setRequestedByLastName("Test");
        deviceCertificationRequest.setRequestedByFirstName("Test");

        MayBeachResponse response = target.sendDeviceCertificationRequest(deviceCertificationRequest);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Request Successful", response.getMessage());
    }

//    @Test
    public void sendFetchActivationDataRequest_ForActivatedDevice_ShouldReturnJurisdiction() {

        String deviceId = "MANTRA-911573953260076";
        String requestId = "MANTRA-911573953260076-1651044318649";

        MayBeachRequestResponse response = (MayBeachRequestResponse) target.sendFetchActivationDataRequest(deviceId,requestId);
        DeviceActivationDataPojo data = (DeviceActivationDataPojo) response.getData();

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(data);
        assertNotNull(data.getFep());
        assertNotNull(data.getFep().getJurisdiction());

    }
}