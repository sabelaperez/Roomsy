package com.roomsy.backend.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {
    @Value("${cookie.access-token-name}")
    private String accessTokenName;

    @Value("${cookie.refresh-token-name}")
    private String refreshTokenName;

    @Value("${cookie.domain}")
    private String domain;

    @Value("${cookie.path}")
    private String path;

    @Value("${cookie.secure}")
    private boolean secure;

    @Value("${cookie.http-only}")
    private boolean httpOnly;

    @Value("${cookie.same-site}")
    private String sameSite;

    public Cookie createAccessTokenCookie(String token, long maxAge) {
        Cookie cookie = new Cookie(accessTokenName, token);
        configureCookie(cookie, maxAge);
        return cookie;
    }

    public Cookie createRefreshTokenCookie(String token, long maxAge) {
        Cookie cookie = new Cookie(refreshTokenName, token);
        configureCookie(cookie, maxAge);
        return cookie;
    }

    private void configureCookie(Cookie cookie, long maxAge) {
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath(path);
        cookie.setDomain(domain);
        cookie.setMaxAge((int) (maxAge / 1000)); // Convert milliseconds to seconds
        // SameSite is set via response header in SecurityConfig
    }

    public Cookie createExpiredAccessTokenCookie() {
        Cookie cookie = new Cookie(accessTokenName, "");
        cookie.setMaxAge(0);
        cookie.setPath(path);
        cookie.setDomain(domain);
        return cookie;
    }

    public Cookie createExpiredRefreshTokenCookie() {
        Cookie cookie = new Cookie(refreshTokenName, "");
        cookie.setMaxAge(0);
        cookie.setPath(path);
        cookie.setDomain(domain);
        return cookie;
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return extractCookie(request, accessTokenName);
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return extractCookie(request, refreshTokenName);
    }

    private Optional<String> extractCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public String getAccessTokenName() {
        return accessTokenName;
    }

    public String getRefreshTokenName() {
        return refreshTokenName;
    }
}