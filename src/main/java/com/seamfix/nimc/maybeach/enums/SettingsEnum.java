/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.seamfix.nimc.maybeach.enums;

@SuppressWarnings("PMD")
public enum SettingsEnum {

    MAYBEACH_URL("MAYBEACH-URL", "https://graph.maybeachtech.com.ng/graphql", "Maybeach url for production"),
    MAYBEACH_AUTHORIZATION("MAYBEACH-AUTHORIZATION", "Basic ZHI5cGhpbDAwNzpjaGlHSVJMPTEyeDU=", "Maybeach authorization token for production"),
    MAYBEACH_TOKEN("MAYBEACH-TOKEN", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYXBpOXUzNDUzNzgzODIxMTUxMzk5NjQ1IiwiYXBwbGljYXRpb25fa2V5IjoiOXUzNDUzNzgzODIxNzM4NDIyOTM5IiwiZXhwaXJhdGlvbiI6MTc5NDM5Mjc2MH0.bB9qfXsxCDfNyXVPxYq0NpaPucE_w0TxwOU4pZ8-FuI", "Maybeach token for production"),
    MOCK_MAYBEACH("MOCK-MAYBEACH", "false", "Mock maybeach calls");

    SettingsEnum(String name, String value, String description) {
        this.name = name;
        this.value = value;
        this.description = description;
    }

    private String name;
    private String value;
    private String description;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
