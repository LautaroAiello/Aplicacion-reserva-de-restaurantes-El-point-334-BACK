package com.auth_service.dto;

import java.util.List;
import java.util.Map;

public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private List<String> roles;
    private List<Map<String, Object>> restauranteRoles;

    public LoginResponse() {}

    public LoginResponse(String token) {
        this.token = token;
    }

    public LoginResponse(String token, List<String> roles, List<Map<String, Object>> restauranteRoles) {
        this.token = token;
        this.roles = roles;
        this.restauranteRoles = restauranteRoles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<Map<String, Object>> getRestauranteRoles() {
        return restauranteRoles;
    }

    public void setRestauranteRoles(List<Map<String, Object>> restauranteRoles) {
        this.restauranteRoles = restauranteRoles;
    }
}
