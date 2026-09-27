package dev.midnightcoder.website.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author Glabay | The Midnight Coder
 * @project Midnight-Technician
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-09-24
 */
@Service
@NullMarked
public class JwtService {
    private final String issuer;
    private final String audience;
    @Getter private final int accessTtlMinutes;
    private final SecretKey signingKey;
    private final JwtParser parser;

    public JwtService(
        @Value("${jwt.issuer}") String issuer,
        @Value("${jwt.audience}") String audience,
        @Value("${jwt.access-ttl-minutes}") int accessTtlMinutes,
        @Value("${jwt.clock-skew-seconds}") long clockSkewSeconds,
        @Value("${jwt.secret}") String secret
    ) {
        this.issuer = issuer;
        this.audience = audience;
        this.accessTtlMinutes = accessTtlMinutes;
        if (issuer == null || issuer.isBlank() || audience == null || audience.isBlank()
            || accessTtlMinutes <= 0 || clockSkewSeconds < 0)
            throw new IllegalStateException("Invalid JWT issuer, audience, TTL or clock skew configuration");
        this.signingKey = signingKey(secret);
        this.parser = Jwts.parser().verifyWith(signingKey)
            .requireIssuer(issuer).requireAudience(audience)
            .clockSkewSeconds(clockSkewSeconds).build();
    }

    private SecretKey signingKey(String secret) {
        if (secret == null || secret.isBlank()
            || secret.equals("changeItAsSoonAsPossibleBeforeProductionOrAnythingReallyThisIsNotGood"))
            throw new IllegalStateException("Configure an external Base64 JWT signing key");
        try {
            return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        }
        catch (IllegalArgumentException | io.jsonwebtoken.security.WeakKeyException e) {
            throw new IllegalStateException("JWT signing key must be valid Base64 with at least 256 bits");
        }
    }

    public String generateAccessToken(String subject, Collection<String> roles) {
        var now = Instant.now();
        var exp = now.plus(accessTtlMinutes, ChronoUnit.MINUTES);

        var claims = new HashMap<String, Object>();
        claims.put("roles", new ArrayList<>(roles));

        return Jwts.builder()
            .issuer(issuer)
            .audience()
            .add(audience)
            .and()
            .subject(subject)
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claims(claims)
            .signWith(signingKey)
            .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        }
        catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        if (token == null || token.isBlank())
            throw new MalformedJwtException("Access token is missing");
        var claims = parser.parseSignedClaims(token).getPayload();
        if (claims.getExpiration() == null || claims.getSubject() == null || claims.getSubject().isBlank())
            throw new MalformedJwtException("Access token requires expiration and subject");
        extractRoles(claims);
        return claims;
    }

    public List<String> extractRoles(Claims claims) {
        var raw = claims.get("roles");
        if (raw instanceof List<?> list) {
            var roles = new ArrayList<String>();
            for (var value : list) {
                if (!(value instanceof String role) || role.isBlank())
                    throw new MalformedJwtException("Invalid authority claim");
                roles.add(role);
            }
            return List.copyOf(roles);
        }
        throw new MalformedJwtException("Access token requires an authority list");
    }
}
