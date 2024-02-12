package com.seamfix.nimc.maybeach.configs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Getter
@Setter
@Configuration
@RefreshScope
public class AppConfig {

	@Value("${nimc.maybeach.account.code}")
	private String cbsAccountCode;

	@Value("${nimc.maybeach.api.key}")
	private String cbsApiKey;

	@Value("${nimc.maybeach.integration.enabled}")
	protected boolean mayBeachIntegrationEnabled;

	@Value("${nimc.maybeach.payment.verification.mock.response.enabled}")
	protected boolean mockPaymentVerificationResponse;

	@Value("${nimc.maybeach.acceptable.codes}")
	private String cbsAcceptableCodes;

	@Value("${nimc.maybeach.api.timeout}")
	private int cbsApiTimeout;

	@Value("${maybeach.microservice.baseurl}")
	protected String cbsServiceBaseUrl;

	@Value("${nimc.maybeach.device.activation.uri}")
	protected String cbsDeviceActivationUri;

	@Value("${nimc.maybeach.device.certification.uri}")
	protected String cbsDeviceCertificationUri;

	@Value("${nimc.maybeach.fetch.activation.uri}")
	protected String cbsFetchActivationUri;

	@Value("${nimc.maybeach.heartbeats.uri}")
	protected String cbsHeartbeatsUri;

	@Value("${nimc.maybeach.payment.uri}")
	protected String cbsPaymentUri;

	@Value("${nimc.maybeach.payment.status.uri}")
	protected String cbsPaymentStatusUri;

	@Value("${nimc.maybeach.payment.status-v2.uri}")
	protected String cbsPaymentStatusV2Uri;

	@Value("${nimc.maybeach.enrolment.notification.uri}")
	protected String cbsEnrolmentNotificationUri;

	@Value("${nimc.maybeach.new.device.notification.uri}")
	protected String cbsNewDeviceNotificationUri;

	@Value("${nimc.maybeach.device.update.notification.uri}")
	protected String cbsDeviceUpdateNotificationUri;

	@Value("${nimc.maybeach.fetch.enrolment.centre.uri}")
	protected String cbsFetchEnrolmentNotificationUri;

	@Value("${nimc.maybeach.entity.status.uri}")
	protected String cbsEntityStatusUri;

	@Value("${nimc.maybeach.pre-enrolment.check.uri}")
	protected String cbsPreEnrolmentCheckUri;

	@Value("${nimc.maybeach.phone.pre-enrolment.check.uri}")
	protected String cbsPhonePreEnrolmentCheckUri;

	@Value("${nimc.maybeach.demographic.pre-enrolment.check.uri}")
	protected String cbsDemographicPreEnrolmentCheckUri;

	@Value("${skip.authentication.header}")
	protected boolean skipAuthenticationHeaders;

	@Value("${header.salt.key}")
	protected String saltKey;

	@Value("${nimc.maybeach.enable.payload}")
	protected boolean enablePayload;

	@Value("${mock.success.rrr:120000000000}")
	protected String mockSuccessRRR;

	@Value("${mock.overpaid.rrr:120000000001}")
	protected String mockOverPaidRRR;

	@Value("${mock.service.not.available.rrr:120000000002}")
	protected String mockServiceNotAvailableRRR;

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplateBuilder()
				.setConnectTimeout(Duration.ofMillis(getCbsApiTimeout()))
				.setReadTimeout(Duration.ofMillis(getCbsApiTimeout())).build();
	}

	@Bean
	public ObjectMapper objectMapper(){
		return new ObjectMapper()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

}
