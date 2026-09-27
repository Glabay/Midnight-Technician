package dev.midnightcoder.website.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-27
 */
class CookieHelperTests {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void accessCookieIsSecureHttpOnlyAndHostOnly(String domain) {
        var helper = new CookieHelper(true, domain);
        var response = new MockHttpServletResponse();

        helper.setAccessCookie(response, "test-token", Duration.ofMinutes(15));

        var headers = response.getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(headers).hasSize(1);
        assertThat(headers.iterator().next()).startsWith("mt_at=test-token;")
            .contains("Path=/", "Max-Age=900", "HttpOnly", "Secure", "SameSite=Lax")
            .doesNotContain("Domain=");
        var cookie = response.getCookie("mt_at");
        assertThat(cookie).isNotNull();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getDomain()).isNull();
        assertThat(cookie.getMaxAge()).isEqualTo(900);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
    }

    @Test
    void clearingAuthCookiesExpiresBothNamesAtBothPathsWithMatchingSecurityAttributes() {
        var helper = new CookieHelper(true, "");
        var response = new MockHttpServletResponse();

        helper.clearAuthCookies(response);

        var cookies = response.getCookies();
        assertThat(cookies).hasSize(4);
        assertThat(cookies).extracting(cookie -> cookie.getName() + ":" + cookie.getPath())
            .containsExactlyInAnyOrder("mt_at:/", "mt_at:/auth", "mt_rt:/", "mt_rt:/auth");
        assertThat(cookies).allSatisfy(cookie -> {
            assertThat(cookie.getValue()).isEmpty();
            assertThat(cookie.getMaxAge()).isZero();
            assertThat(cookie.getDomain()).isNull();
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.getSecure()).isTrue();
        });
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).hasSize(4).allSatisfy(header ->
            assertThat(header).contains("Max-Age=0", "HttpOnly", "Secure", "SameSite=Lax")
                .doesNotContain("Domain="));
    }

    @ParameterizedTest
    @ValueSource(strings = {"example.test", ".example.test", "localhost"})
    void configuredDomainScopesIssuedAndDeletedCookies(String domain) {
        var helper = new CookieHelper(true, domain);
        var response = new MockHttpServletResponse();

        helper.setAccessCookie(response, "test-token", Duration.ofMinutes(15));
        helper.setRefreshCookie(response, "test-refresh-token", Duration.ofMinutes(30));

        var issued = response.getCookies();
        assertThat(issued).hasSize(2);
        assertThat(issued).extracting(cookie -> cookie.getName() + ":" + cookie.getPath())
            .containsExactlyInAnyOrder("mt_at:/", "mt_rt:/auth");
        assertThat(issued).allSatisfy(cookie -> {
            assertThat(cookie.getDomain()).isEqualTo(domain);
            assertThat(cookie.getMaxAge()).isPositive();
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.getSecure()).isTrue();
        });
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).hasSize(2).allSatisfy(header ->
            assertThat(header).contains("Domain=" + domain + ";", "HttpOnly", "Secure", "SameSite=Lax"));

        var deletionResponse = new MockHttpServletResponse();
        helper.clearAuthCookies(deletionResponse);

        var deleted = deletionResponse.getCookies();
        assertThat(deleted).hasSize(4);
        assertThat(deleted).extracting(cookie -> cookie.getName() + ":" + cookie.getPath())
            .containsExactlyInAnyOrder("mt_at:/", "mt_at:/auth", "mt_rt:/", "mt_rt:/auth");
        assertThat(deleted).allSatisfy(cookie -> {
            assertThat(cookie.getValue()).isEmpty();
            assertThat(cookie.getMaxAge()).isZero();
            assertThat(cookie.getDomain()).isEqualTo(domain);
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.getSecure()).isTrue();
        });
        assertThat(deletionResponse.getHeaders(HttpHeaders.SET_COOKIE)).hasSize(4).allSatisfy(header ->
            assertThat(header).contains("Domain=" + domain + ";", "Max-Age=0", "HttpOnly", "Secure", "SameSite=Lax"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://example.test", "htp://localhost:8080", "localhost:8080",
        "example.test/path", "example.test\r\nX-Test: value", "-example.test",
        "example-.test", "example..test", "example_test", "."
    })
    void invalidConfiguredDomainIsRejected(String domain) {
        assertThatThrownBy(() -> new CookieHelper(true, domain)).isInstanceOf(IllegalArgumentException.class);
    }
}