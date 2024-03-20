package com.seamfix.nimc.maybeach.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DeviceUserLogin {
	private String id;
	@JsonProperty("agentfirstname")
	private String agentFirstName;
	@JsonProperty("agentlastname")
	private String agentLastName;
	@JsonProperty("agentemail")
	private String agentEmail;
	private String permission;
}
