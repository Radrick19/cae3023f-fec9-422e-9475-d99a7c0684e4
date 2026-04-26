package org.example.codeconfirmapp.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.example.codeconfirmapp.model.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final Algorithm algorithm;
    private final long ttlSeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.ttl-seconds:3600}") long ttlSeconds
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.ttlSeconds = ttlSeconds;
    }

    public String generateToken(AuthPrincipal principal) {
        var now = Instant.now();
        return JWT.create()
                .withSubject(principal.userId().toString())
                .withClaim("username", principal.username())
                .withClaim("role", principal.role().name())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(ttlSeconds)))
                .sign(algorithm);
    }

    public AuthPrincipal verify(String token) {
        DecodedJWT jwt = JWT.require(algorithm).build().verify(token);
        return new AuthPrincipal(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaim("username").asString(),
                Role.valueOf(jwt.getClaim("role").asString())
        );
    }
}
