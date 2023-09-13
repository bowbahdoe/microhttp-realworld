package dev.mccue.microhttp.realworld;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class Auth {
    private Auth() {}

    public static String jwtForUser(long userId) {
        var algorithm = Algorithm.HMAC256(
                Objects.requireNonNull(Env.JWT_SECRET)
        );
        return JWT.create()
                .withClaim("user_id", Long.toString(userId))
                .withExpiresAt(Instant.now().plus(Duration.ofHours(72)))
                .sign(algorithm);
    }

}
