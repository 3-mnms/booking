package com.mnms.booking.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class AuthDetails extends WebAuthenticationDetails {
    private final String userName;

    public AuthDetails(HttpServletRequest request, String userName) {
        super(request);
        this.userName = userName;
    }
    public String getUserName() {
        return userName;
    }
}
