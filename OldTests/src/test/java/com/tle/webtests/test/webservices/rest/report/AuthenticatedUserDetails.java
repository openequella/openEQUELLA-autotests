package com.tle.webtests.test.webservices.rest.report;

public class AuthenticatedUserDetails {
    String token;
    String username;
    String id;

    public AuthenticatedUserDetails(String name) {
        username = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
