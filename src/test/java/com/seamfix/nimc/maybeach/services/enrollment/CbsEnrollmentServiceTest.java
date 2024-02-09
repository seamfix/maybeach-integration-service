package com.seamfix.nimc.maybeach.services.enrollment;

import com.seamfix.nimc.maybeach.configs.AppConfig;
import com.seamfix.nimc.maybeach.dto.CbsDeviceUpdateNotification;
import com.seamfix.nimc.maybeach.dto.CbsEnrollmentNotificationRequest;
import com.seamfix.nimc.maybeach.dto.CbsNewDeviceNotification;
import com.seamfix.nimc.maybeach.dto.CbsPreEnrollmentCheckRequest;
import com.seamfix.nimc.maybeach.dto.CbsPreEnrollmentVerificationResponse;
import com.seamfix.nimc.maybeach.dto.MayBeachRequestResponse;
import com.seamfix.nimc.maybeach.dto.MayBeachResponse;
import com.seamfix.nimc.maybeach.dto.Fingers;
import com.seamfix.nimc.maybeach.services.jms.JmsSender;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
@SpringBootTest
public class CbsEnrollmentServiceTest {

	@Autowired
	MayBeachEnrollmentService target;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private AppConfig appConfig;

	@BeforeEach
	void init(){
		target = new MayBeachEnrollmentService();
		target.setRestTemplate(restTemplate);
		target.setAppConfig(appConfig);
		target.setJmsSender(Mockito.mock(JmsSender.class));
	}

	private String deviceId = "MANTRA-911573953260076";
	private String entityType = "fep";
	private String entityIdentifier = "NM0093";

	@Test
	void callNewDeviceNotificationService_ForDuplicateDeviceId_ShouldReturnConflict() {
		CbsNewDeviceNotification cbsNewDeviceNotification = new CbsNewDeviceNotification();
		cbsNewDeviceNotification.setMachineTag("DROID-S120-NNEOMS-" + System.currentTimeMillis());
		String deviceId = "INFINIX-88888" + System.currentTimeMillis();
		cbsNewDeviceNotification.setProviderDeviceIdentifier(deviceId);
		cbsNewDeviceNotification.setCurrentLocationLongitude("3.47182494");
		cbsNewDeviceNotification.setCurrentLocationLatitude("6.4380415");
		cbsNewDeviceNotification.setCenterCode("FC0001");
		cbsNewDeviceNotification.setCenterName("kuklux");
		cbsNewDeviceNotification.setAuthorizedByLastName("Nwachukwu");
		cbsNewDeviceNotification.setAuthorizedByFirstName("Nneoma");
		cbsNewDeviceNotification.setActivatedByLastName("KELLY KELL");
		cbsNewDeviceNotification.setActivatedByFirstName("KELLY KELL");
		cbsNewDeviceNotification.setActivatorEmail("nneoma@yopmail.com");
		cbsNewDeviceNotification.setActivatorPhone("08099887766");
		cbsNewDeviceNotification.setActivatorNin("11111111111");
		cbsNewDeviceNotification.setEsaName("SEAMFIX");
		cbsNewDeviceNotification.setEsaCode(entityIdentifier);
		cbsNewDeviceNotification.setProviderIdentifier("SEAMFIX");
		cbsNewDeviceNotification.setDateActivated("2019-11-29T12:11:32.385Z");
		cbsNewDeviceNotification.setRequestId(deviceId);
		cbsNewDeviceNotification.setLocation("LEKKI");

		target.sendNewDeviceNotification(cbsNewDeviceNotification);
		MayBeachResponse response = target.sendNewDeviceNotification(cbsNewDeviceNotification);

		assertNotNull(response);
		assertEquals(409, response.getCode());
		assertEquals("Duplicate request Id " + deviceId, response.getMessage());
	}

	@Test
	void callNewDeviceNotificationService_ForValidRequest_ShouldReturnSuccess() {
		CbsNewDeviceNotification cbsNewDeviceNotification = new CbsNewDeviceNotification();
		cbsNewDeviceNotification.setMachineTag("DROID-S120-NNEOMS-" + System.currentTimeMillis());
		String deviceId = "INFINIX-88888" + System.currentTimeMillis();
		cbsNewDeviceNotification.setProviderDeviceIdentifier(deviceId);
		cbsNewDeviceNotification.setCurrentLocationLongitude("3.47182494");
		cbsNewDeviceNotification.setCurrentLocationLatitude("6.4380415");
		cbsNewDeviceNotification.setCenterCode("FC0001");
		cbsNewDeviceNotification.setCenterName("kuklux");
		cbsNewDeviceNotification.setAuthorizedByLastName("Nwachukwu");
		cbsNewDeviceNotification.setAuthorizedByFirstName("Nneoma");
		cbsNewDeviceNotification.setActivatedByLastName("KELLY KELL");
		cbsNewDeviceNotification.setActivatedByFirstName("KELLY KELL");
		cbsNewDeviceNotification.setActivatorEmail("nneoma@yopmail.com");
		cbsNewDeviceNotification.setActivatorPhone("08099887766");
		cbsNewDeviceNotification.setActivatorNin("11111111111");
		cbsNewDeviceNotification.setEsaName("SEAMFIX");
		cbsNewDeviceNotification.setEsaCode(entityIdentifier);
		cbsNewDeviceNotification.setProviderIdentifier("SEAMFIX");
		cbsNewDeviceNotification.setDateActivated("2019-11-29T12:11:32.385Z");
		cbsNewDeviceNotification.setRequestId(deviceId);
		cbsNewDeviceNotification.setLocation("LEKKI");

		MayBeachResponse response = target.sendNewDeviceNotification(cbsNewDeviceNotification);

		assertNotNull(response);
		assertEquals(200, response.getCode());
		assertEquals("Successful", response.getMessage());
	}

	@Test
	void callGetEntityStatus_ForInvalidDeviceId_ShouldReturnNull() {
		MayBeachRequestResponse response = (MayBeachRequestResponse) target.getEntityStatus(entityType, entityIdentifier, "INVALID-DEVICE-ID");

		assertNotNull(response);
		assertEquals(400, response.getCode());
		assertEquals("Node with identifier INVALID-DEVICE-ID and provider Seamfix not found", response.getMessage());
	}

	@Test
	void callGetEntityStatus_ForUnknownEntity_ShouldReturnNotFound() {
		String entityIdentifier = "UNKNOWN_ESA_CODE";

		MayBeachRequestResponse response = (MayBeachRequestResponse) target.getEntityStatus(entityType, entityIdentifier, deviceId);

		assertNotNull(response);
		assertEquals(400, response.getCode());
		assertEquals("Entity not found", response.getMessage());
	}

	@Test
	void callGetEntityStatus_ForValidRequest_ShouldReturnSuccess() {
		MayBeachRequestResponse response = (MayBeachRequestResponse) target.getEntityStatus(entityType, entityIdentifier, deviceId);

		assertNotNull(response);
		assertEquals(200, response.getCode());
		assertEquals("OK", response.getMessage());
	}

	@Test
	void doPreEnrollmentCheck_ForInvalidRequest_ShouldReturnError() throws FileNotFoundException {

		CbsPreEnrollmentCheckRequest cbsPreEnrollmentCheckRequest = new CbsPreEnrollmentCheckRequest();
		cbsPreEnrollmentCheckRequest.setDeviceId(deviceId);
		cbsPreEnrollmentCheckRequest.setRequestTransactionRef("001");
		cbsPreEnrollmentCheckRequest.setRequestTimestamp("2020-07-23T00:55:03.370");

		Fingers fingers = new Fingers();
		fingers.setFingerPosition(1);

		String wsq = "INVALID_WSQ_IMAGE";
		fingers.setBase64FingerWsq(wsq);

		List<Fingers> fingersList = new ArrayList<>();
		fingersList.add(fingers);

		cbsPreEnrollmentCheckRequest.setFingers(fingersList);

		MayBeachResponse response = target.doPreEnrollmentCheck(cbsPreEnrollmentCheckRequest);
		log.debug("code {}, msg {}, status {} ", response.getCode(), response.getMessage(), response.getStatus());

		assertNotNull(response);

	}

	void doPreEnrollmentCheck_ForValidRequest_ShouldReturnSuccess() throws FileNotFoundException {

		CbsPreEnrollmentCheckRequest preEnrollmentCheckRequest = new CbsPreEnrollmentCheckRequest();
		preEnrollmentCheckRequest.setDeviceId(deviceId);
		preEnrollmentCheckRequest.setRequestTransactionRef("001");
		preEnrollmentCheckRequest.setRequestTimestamp("2020-07-23T00:55:03.370");

		Fingers fingers = new Fingers();
		fingers.setFingerPosition(1);

		File file = new File(CbsEnrollmentServiceTest.class.getResource("/wsq-image.txt").getFile());
		Scanner scanner = new Scanner(file);
		String wsq = scanner.nextLine();
		fingers.setBase64FingerWsq(wsq);

		List<Fingers> fingersList = new ArrayList<>();
		fingersList.add(fingers);

		preEnrollmentCheckRequest.setFingers(fingersList);

		MayBeachRequestResponse response = (MayBeachRequestResponse) target.doPreEnrollmentCheck(preEnrollmentCheckRequest);

		assertNotNull(response);
		assertEquals(200, response.getCode());
		assertEquals("Success", response.getMessage());
		assertNotNull(response.getData());

	}

	@Test
	void doPhonePreEnrollmentCheck_ForInValidPhoneNumber_ShouldReturnVerifiedAsFalseAndNullResults() throws FileNotFoundException {

		CbsPreEnrollmentCheckRequest preEnrollmentCheckRequest = new CbsPreEnrollmentCheckRequest();
		preEnrollmentCheckRequest.setDeviceId(deviceId);
		preEnrollmentCheckRequest.setSearchFieldValue("08117110842");

		CbsPreEnrollmentVerificationResponse response = target.doPhonePreEnrollmentCheck(preEnrollmentCheckRequest);

		assertNotNull(response);
		assertFalse(response.isVerified());
		assertNull(response.getResults());
	}

	@Test
	void doPhonePreEnrollmentCheck_ForValidRequest_ShouldReturnVerifiedAsTrueAndResults() throws FileNotFoundException {

		CbsPreEnrollmentCheckRequest preEnrollmentCheckRequest = new CbsPreEnrollmentCheckRequest();
		preEnrollmentCheckRequest.setDeviceId(deviceId);
		preEnrollmentCheckRequest.setSearchFieldValue("07031922049");

		CbsPreEnrollmentVerificationResponse response = target.doPhonePreEnrollmentCheck(preEnrollmentCheckRequest);

		assertNotNull(response);
		assertEquals(0, response.getCode()); // Only code is asserted because of byteworks's API inconsistency
	}

	@Test
	void doDemographicPreEnrollmentCheck_ForInValidPhoneNumber_ShouldReturnVerifiedAsFalseAndNullResults() throws FileNotFoundException {

		CbsPreEnrollmentCheckRequest preEnrollmentCheckRequest = new CbsPreEnrollmentCheckRequest();
		preEnrollmentCheckRequest.setDeviceId(deviceId);
		preEnrollmentCheckRequest.setFirstName("Chibueze");
		preEnrollmentCheckRequest.setLastName("Paul");
		preEnrollmentCheckRequest.setGender("MALE");
		preEnrollmentCheckRequest.setDob("08-08-2022");

		CbsPreEnrollmentVerificationResponse response = target.doDemographicPreEnrollmentCheck(preEnrollmentCheckRequest);

		assertNotNull(response);
		assertFalse(response.isVerified());
		assertNull(response.getResults());
	}

	@Test
	void doDemographicPreEnrollmentCheck_ForValidRequest_ShouldReturnVerifiedAsTrueAndResults() throws FileNotFoundException {

		CbsPreEnrollmentCheckRequest preEnrollmentCheckRequest = new CbsPreEnrollmentCheckRequest();
		preEnrollmentCheckRequest.setDeviceId(deviceId);
		preEnrollmentCheckRequest.setFirstName("Bisi");
		preEnrollmentCheckRequest.setLastName("Eniola");
		preEnrollmentCheckRequest.setGender("FEMALE");
		preEnrollmentCheckRequest.setDob("13-02-1975");

		CbsPreEnrollmentVerificationResponse response = target.doDemographicPreEnrollmentCheck(preEnrollmentCheckRequest);

		assertNotNull(response);
		assertEquals(0, response.getCode()); // Only code is asserted because of byteworks's API inconsistency
	}

	@Test
	void fetchEnrollmentCenters_shouldReturnSuccess() {
		MayBeachResponse cbsResponse = target.fetchEnrollmentCenters(deviceId, entityIdentifier);
		assertNotNull(cbsResponse);
		assertEquals(200, cbsResponse.getStatus());
	}

	@Test
	void sendDeviceUpdateNotification_shouldReturn400_whenRequiredParameterIsMissing() {
		var deviceNotification = new CbsDeviceUpdateNotification();

		MayBeachResponse cbsResponse = target.sendDeviceUpdateNotification(deviceNotification, deviceId);
		assertNotNull(cbsResponse);
		assertEquals(400, cbsResponse.getCode());
	}

	@Test
	void sendEnrollmentNotificationService_shouldReturn400_whenRequiredParameterIsMissing() {
		var enrollmentNotificationRequest = new CbsEnrollmentNotificationRequest();

		MayBeachResponse cbsResponse = target.sendEnrollmentNotificationService(enrollmentNotificationRequest);
		assertNotNull(cbsResponse);
		assertEquals(400, cbsResponse.getCode());
	}
}
