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

	@Value("${nimc.maybeach.api.key}")
	private String cbsApiKey;

	@Value("${nimc.maybeach.integration.enabled}")
	protected boolean mayBeachIntegrationEnabled;

	@Value("${nimc.maybeach.acceptable.codes}")
	private String cbsAcceptableCodes;

	@Value("${nimc.maybeach.api.timeout}")
	private int cbsApiTimeout;

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
