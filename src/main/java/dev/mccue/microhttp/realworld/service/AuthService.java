package dev.mccue.microhttp.realworld.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.mccue.json.Json;
import dev.mccue.json.JsonDecodeException;
import dev.mccue.json.JsonDecoder;
import dev.mccue.json.JsonReadException;
import dev.mccue.microhttp.realworld.domain.User;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

public final class AuthService {
    private final UserService userService;

    public AuthService(UserService userService) {
        this.userService = userService;
    }

    public Optional<User> decodeJwt(String authToken) {
        DecodedJWT decodedJWT;
        try {
            var algorithm = Algorithm.HMAC256(
                    Objects.requireNonNull(System.getenv("JWT_SECRET"))
            );
            var verifier = JWT.require(algorithm).build();
            decodedJWT = verifier.verify(authToken);
        } catch (JWTVerificationException __) {
            return Optional.empty();
        }

        long userId;
        try {
            var json = Json.readString(new String(
                    Base64.getDecoder().decode(decodedJWT.getPayload())
            ));
            userId = Long.parseLong(JsonDecoder.field(json, "user_id", JsonDecoder::string));
        } catch (JsonDecodeException | JsonReadException | NumberFormatException __) {
            return Optional.empty();
        }


        var user = userService.findById(userId).orElse(null);
        return Optional.ofNullable(user);
    }

    public String jwtForUser(User user) {
        var algorithm = Algorithm.HMAC256(
                Objects.requireNonNull(System.getenv("JWT_SECRET"))
        );
        return JWT.create()
                .withClaim("user_id", Long.toString(user.id()))
                .withExpiresAt(Instant.now().plus(Duration.ofHours(72)))
                .sign(algorithm);
    }
}
