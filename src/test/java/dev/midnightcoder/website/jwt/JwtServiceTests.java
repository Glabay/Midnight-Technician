package dev.midnightcoder.website.jwt;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-27
 */
class JwtServiceTests {
    private static final String ISSUER = "test-issuer";
    private static final String AUDIENCE = "test-audience";
    private final SecretKey key = Jwts.SIG.HS256.key().build();
    private final String secret = Encoders.BASE64.encode(key.getEncoded());
    private final JwtService service = new JwtService(ISSUER, AUDIENCE, 15, 0, secret);

    @Test
    void generatedTokenHasSignedIdentityRolesAndConfiguredLifetime() {
        var before = Instant.now().minusSeconds(1);
        var token = service.generateAccessToken("user@example.test", List.of("ROLE_USER", "ROLE_ADMIN"));
        var after = Instant.now();
        var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.getAudience()).containsExactly(AUDIENCE);
        assertThat(claims.getSubject()).isEqualTo("user@example.test");
        assertThat(claims.getIssuedAt().toInstant()).isBetween(before, after);
        assertThat(claims.getExpiration().toInstant())
            .isEqualTo(claims.getIssuedAt().toInstant().plusSeconds(900));
        assertThat(service.extractRoles(claims)).containsExactly("ROLE_USER", "ROLE_ADMIN");
        assertThat(service.parseClaims(token).getSubject()).isEqualTo("user@example.test");
        assertThat(service.isTokenValid(token)).isTrue();
    }

    @Test
    void emptyRolesAreValid() {
        var token = service.generateAccessToken("user@example.test", List.of());

        assertThat(service.extractRoles(service.parseClaims(token))).isEmpty();
        assertThat(service.isTokenValid(token)).isTrue();
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        var token = token(builder -> builder.signWith(Jwts.SIG.HS256.key().build()));

        assertInvalid(token);
    }

    @Test
    void rejectsUnsignedToken() {
        var token = Jwts.builder().issuer(ISSUER).audience().add(AUDIENCE).and()
            .subject("user@example.test").expiration(Date.from(Instant.now().plusSeconds(300)))
            .claim("roles", List.of("ROLE_USER")).compact();

        assertInvalid(token);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"other-issuer", " "})
    void rejectsMissingOrWrongIssuer(String issuer) {
        assertInvalid(token(builder -> builder.issuer(issuer)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"other-audience", " "})
    void rejectsMissingOrWrongAudience(String audience) {
        assertInvalid(token(builder -> builder.claim("aud", audience)));
    }

    @Test
    void rejectsExpiredToken() {
        assertInvalid(token(builder -> builder.expiration(Date.from(Instant.now().minusSeconds(300)))));
    }

    @Test
    void rejectsMissingExpiration() {
        assertInvalid(token(builder -> builder.expiration(null)));
    }

    @Test
    void configuredClockSkewAllowsOnlyItsExpirationWindow() {
        var tolerantService = new JwtService(ISSUER, AUDIENCE, 15, 120, secret);
        var recentlyExpired = token(builder -> builder.expiration(Date.from(Instant.now().minusSeconds(30))));
        var longExpired = token(builder -> builder.expiration(Date.from(Instant.now().minusSeconds(300))));

        assertThat(tolerantService.isTokenValid(recentlyExpired)).isTrue();
        assertThat(tolerantService.parseClaims(recentlyExpired).getSubject()).isEqualTo("user@example.test");
        assertThat(tolerantService.isTokenValid(longExpired)).isFalse();
        assertThatThrownBy(() -> tolerantService.parseClaims(longExpired)).isInstanceOf(JwtException.class);
        assertInvalid(recentlyExpired);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsMissingOrBlankSubject(String subject) {
        assertInvalid(token(builder -> builder.subject(subject)));
    }

    @ParameterizedTest
    @MethodSource("invalidRoles")
    void rejectsMalformedRolesWhenParsing(Object roles) {
        assertInvalid(token(builder -> builder.claim("roles", roles)));
    }

    @ParameterizedTest
    @MethodSource("invalidRoles")
    void rejectsMalformedRolesWhenExtracting(Object roles) {
        var claims = Jwts.claims().add("roles", roles).build();

        assertThatThrownBy(() -> service.extractRoles(claims)).isInstanceOf(JwtException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "not-a-jwt", "a.b.c"})
    void rejectsMalformedToken(String token) {
        assertInvalid(token);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        " ", "\t\n", "not!valid!base64", "c2hvcnQ=",
        "changeItAsSoonAsPossibleBeforeProductionOrAnythingReallyThisIsNotGood"
    })
    void invalidSecretFailsStartup(String invalidSecret) {
        assertThatThrownBy(() -> new JwtService(ISSUER, AUDIENCE, 15, 0, invalidSecret))
            .isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void nonpositiveTtlFailsStartup(int ttl) {
        assertThatThrownBy(() -> new JwtService(ISSUER, AUDIENCE, ttl, 0, secret))
            .isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, Long.MIN_VALUE})
    void negativeClockSkewFailsStartup(long skew) {
        assertThatThrownBy(() -> new JwtService(ISSUER, AUDIENCE, 15, skew, secret))
            .isInstanceOf(RuntimeException.class);
    }

    private static Stream<Object> invalidRoles() {
        return Stream.of(
            null, "ROLE_USER", "ROLE_USER,ROLE_ADMIN", 42, Map.of("role", "ROLE_USER"),
            List.of(42), List.of(""), List.of(" "), List.of("\t\n"),
            List.of("ROLE_USER", 42), List.of("ROLE_USER", " "),
            Arrays.asList("ROLE_USER", null), List.of(List.of("ROLE_USER")));
    }

    private String token(Consumer<JwtBuilder> customize) {
        var builder = Jwts.builder().issuer(ISSUER).audience().add(AUDIENCE).and()
            .subject("user@example.test").issuedAt(new Date())
            .expiration(Date.from(Instant.now().plusSeconds(300)))
            .claim("roles", List.of("ROLE_USER")).signWith(key);
        customize.accept(builder);
        return builder.compact();
    }

    private void assertInvalid(String token) {
        assertThatThrownBy(() -> service.parseClaims(token)).isInstanceOf(JwtException.class);
        assertThat(service.isTokenValid(token)).isFalse();
    }
}