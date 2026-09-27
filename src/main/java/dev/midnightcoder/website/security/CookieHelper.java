package dev.midnightcoder.website.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-24
 */
@Component
public class CookieHelper {
    public static final String ACCESS_COOKIE = "mt_at";
    public static final String REFRESH_COOKIE = "mt_rt";

    private final boolean secureCookies;
    private final String cookieDomain;

    public CookieHelper(
        @Value("${security.cookies.secure}") boolean secureCookies,
        @Value("${security.cookies.domain}") String cookieDomain
    ) {
        this.secureCookies = secureCookies;
        this.cookieDomain = (cookieDomain == null ||
                             cookieDomain.isBlank())
            ? null
            : cookieDomain;
    }


    public void setAccessCookie(HttpServletResponse response, String token, Duration ttl) {
        var cookie = baseCookieBuilder(ACCESS_COOKIE, token)
            .path("/")
            .maxAge(ttl)
            .httpOnly(true)
            .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void setRefreshCookie(HttpServletResponse response, String token, Duration ttl) {
        var cookie = baseCookieBuilder(REFRESH_COOKIE, token)
            .path("/auth")
            .maxAge(ttl)
            .httpOnly(true)
            .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearAuthCookies(HttpServletResponse response) {
        var atRoot = baseCookieBuilder(ACCESS_COOKIE, "")
            .path("/")
            .maxAge(Duration.ZERO)
            .httpOnly(true)
            .build();
        var atAuth = baseCookieBuilder(ACCESS_COOKIE, "")
            .path("/auth")
            .maxAge(Duration.ZERO)
            .httpOnly(true)
            .build();
        var rtAuth = baseCookieBuilder(REFRESH_COOKIE, "")
            .path("/auth")
            .maxAge(Duration.ZERO)
            .httpOnly(true)
            .build();
        var rtRoot = baseCookieBuilder(REFRESH_COOKIE, "")
            .path("/")
            .maxAge(Duration.ZERO)
            .httpOnly(true)
            .build();

        response.addHeader("Set-Cookie", atRoot.toString());
        response.addHeader("Set-Cookie", atAuth.toString());
        response.addHeader("Set-Cookie", rtAuth.toString());
        response.addHeader("Set-Cookie", rtRoot.toString());
    }

    public Optional<String> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null)
            return Optional.empty();

        return Arrays.stream(request.getCookies())
            .filter(c -> name.equals(c.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse("")
            .describeConstable();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookieBuilder(String name, String value) {
        var baseCookie = ResponseCookie.from(name, value)
            .secure(secureCookies)
            .sameSite("Lax");

        return cookieDomain != null
            ? baseCookie.domain(cookieDomain)
            : baseCookie;
    }
}
