package dev.midnightcoder.website.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-27
 */
class JwtAuthenticationFilterTests {
    private final SecretKey key = Jwts.SIG.HS256.key().build();
    private final JwtService service = new JwtService(
        "test-issuer", "test-audience", 15, 0, Encoders.BASE64.encode(key.getEncoded()));
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(service);
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/account");
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final FilterChain chain = mock(FilterChain.class);

    @BeforeEach
    void initializeContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void accessCookieAuthenticatesAnEmptyContext() throws Exception {
        var token = service.generateAccessToken("user@example.test", List.of("ROLE_USER", "ROLE_ADMIN"));
        request.setCookies(new Cookie("unrelated", "ignored"), new Cookie("mt_at", token));

        filter.doFilter(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getName()).isEqualTo("user@example.test");
        assertThat(authentication.getCredentials()).isNull();
        assertThat(authentication.getAuthorities()).extracting("authority")
            .containsExactly("ROLE_USER", "ROLE_ADMIN");
        verify(chain).doFilter(request, response);
    }

    @Test
    void accessCookieWithEmptyRolesStillAuthenticates() throws Exception {
        request.setCookies(new Cookie("mt_at", service.generateAccessToken("user@example.test", List.of())));

        filter.doFilter(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getAuthorities()).isEmpty();
        verify(chain).doFilter(request, response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid-token", "a.b.c"})
    void invalidAccessCookieLeavesRequestAnonymous(String token) throws Exception {
        request.setCookies(new Cookie("mt_at", token));

        assertAnonymousAndContinues();
    }

    @Test
    void expiredAccessCookieLeavesRequestAnonymous() throws Exception {
        var token = Jwts.builder().issuer("test-issuer").audience().add("test-audience").and()
            .subject("user@example.test").claim("roles", List.of("ROLE_USER"))
            .expiration(Date.from(Instant.now().minusSeconds(300))).signWith(key).compact();
        request.setCookies(new Cookie("mt_at", token));

        assertAnonymousAndContinues();
    }

    @Test
    void malformedRolesLeaveRequestAnonymous() throws Exception {
        var token = Jwts.builder().issuer("test-issuer").audience().add("test-audience").and()
            .subject("user@example.test").claim("roles", List.of("ROLE_USER", 42))
            .expiration(Date.from(Instant.now().plusSeconds(300))).signWith(key).compact();
        request.setCookies(new Cookie("mt_at", token));

        assertAnonymousAndContinues();
    }

    @Test
    void missingCookiesLeaveRequestAnonymous() throws Exception {
        assertAnonymousAndContinues();
    }

    @Test
    void unrelatedCookieDoesNotAuthenticate() throws Exception {
        request.setCookies(new Cookie("unrelated", service.generateAccessToken("user@example.test", List.of())));

        assertAnonymousAndContinues();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void existingAuthenticationIsUnchanged(boolean validCookie) throws Exception {
        var existing = UsernamePasswordAuthenticationToken.authenticated(
            "existing-user", null, List.of(new SimpleGrantedAuthority("ROLE_EXISTING")));
        SecurityContextHolder.getContext().setAuthentication(existing);
        var token = validCookie
            ? service.generateAccessToken("different-user", List.of("ROLE_ADMIN"))
            : "invalid-token";
        request.setCookies(new Cookie("mt_at", token));

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existing);
        assertThat(existing.getName()).isEqualTo("existing-user");
        assertThat(existing.getAuthorities()).extracting("authority").containsExactly("ROLE_EXISTING");
        verify(chain).doFilter(request, response);
    }

    private void assertAnonymousAndContinues() throws Exception {
        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }
}