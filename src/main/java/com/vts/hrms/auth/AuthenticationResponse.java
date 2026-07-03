package com.vts.hrms.auth;

import java.util.Set;

public class AuthenticationResponse {

    private final String token;
    private final Set<String> roles; // Add this field

    public AuthenticationResponse(String token,Set<String> roles) {
        this.token = token;
        this.roles = roles;
    }

    public String getToken() { return token; }
    public Set<String> getRoles() { return roles; }

}
