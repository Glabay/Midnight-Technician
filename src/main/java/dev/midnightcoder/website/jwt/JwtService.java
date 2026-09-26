package dev.midnightcoder.website.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
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
    private final int accessTtlMinutes;
    private final long clockSkewSeconds;
    private final Key signingKey;

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
        this.clockSkewSeconds = clockSkewSeconds;
        if (secret.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured. Set jwt.secret or JWT_SECRET env var.");
        }
        byte[] keyBytes = Decoders.BASE64.decode(secret.trim());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
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
            var jws = Jwts.parser()
                .requireIssuer(issuer)
                .requireAudience(audience)
                .clockSkewSeconds(clockSkewSeconds)
                .setSigningKey(signingKey)
                .build()
                .parseSignedClaims(token);
            // Exp is checked by the parser; additional custom checks can go here.
            return jws != null;
        }
        catch (Exception e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
            .requireIssuer(issuer)
            .requireAudience(audience)
            .clockSkewSeconds(clockSkewSeconds)
            .setSigningKey(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public List<String> extractRoles(Claims claims) {
        var raw = claims.get("roles");
        if (raw instanceof List<?> list) {
            var roles = new ArrayList<String>();
            for (var o : list)
                roles.add(o.toString());
            return roles;
        }
        if (raw instanceof String s)
            return Arrays.asList(s.split(","));
        return Collections.emptyList();
    }
}
