package com.seamfix.nimc.maybeach.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class MayBeachClientAppUserData implements Serializable {
    private static final long serialVersionUID = 1899374484684010171L;
    private String loginId;
    private String firstname;
    private String lastname;
    private String email;
    private List<String> roles;

}
