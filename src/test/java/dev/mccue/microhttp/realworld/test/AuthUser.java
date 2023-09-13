package dev.mccue.microhttp.realworld.test;

import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import dev.mccue.microhttp.realworld.handlers.RootHandler;
import org.microhttp.Header;
import org.microhttp.Request;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public record AuthUser(
        Header authHeader,
        String email,
        String username,
        String password
) {
    public static AuthUser create(RootHandler rootHandler) throws Exception {
        Supplier<String> randomStr = () -> HexFormat.of().formatHex(new byte[] {
                (byte) ThreadLocalRandom.current().nextInt(),
                (byte) ThreadLocalRandom.current().nextInt(),
                (byte) ThreadLocalRandom.current().nextInt(),
                (byte) ThreadLocalRandom.current().nextInt(),
                (byte) ThreadLocalRandom.current().nextInt(),
                (byte) ThreadLocalRandom.current().nextInt(),
                (byte) ThreadLocalRandom.current().nextInt(),
                (byte) ThreadLocalRandom.current().nextInt(),
                (byte) ThreadLocalRandom.current().nextInt(),
                (byte) ThreadLocalRandom.current().nextInt(),
        });
        var email = randomStr.get();
        var username = randomStr.get();
        var password = randomStr.get();
        var registerResponse = rootHandler.handle(new Request(
                "POST",
                "/api/users",
                "",
                List.of(),
                Json.objectBuilder()
                        .put("user", Json.objectBuilder()
                                .put("email", email)
                                .put("username", username)
                                .put("password", password))
                        .build()
                        .toString()
                        .getBytes(StandardCharsets.UTF_8)
        ));

        var registrationJson = Json.readString(new String(
                registerResponse.intoResponse().body(),
                StandardCharsets.UTF_8
        ));

        var authToken = JsonDecoder.field(
                registrationJson,
                "user",
                JsonDecoder.field("token", JsonDecoder::string)
        );

        var authHeader = new Header(
                "Authorization",
                "Token " + authToken
        );

        return new AuthUser(
                authHeader,
                email,
                username,
                password
        );
    }
}
