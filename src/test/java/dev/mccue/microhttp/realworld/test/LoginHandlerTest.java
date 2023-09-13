package dev.mccue.microhttp.realworld.test;

import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import org.junit.jupiter.api.Test;
import org.microhttp.Request;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LoginHandlerTest extends HandlerTest {
    @Test
    public void testLogin() throws Exception {
        var user = AuthUser.create(rootHandler);
        var response = rootHandler.handle(new Request(
                "POST",
                "/api/users/login",
                "",
                List.of(user.authHeader()),
                Json.objectBuilder()
                        .put("user", Json.objectBuilder()
                                .put("email", user.email())
                                .put("password", user.password()))
                        .build()
                        .toString()
                        .getBytes(StandardCharsets.UTF_8)
        )).intoResponse();

        assertEquals(200, response.status());

        var jsonBody = Json.readString(new String(response.body(), StandardCharsets.UTF_8));
        var email = JsonDecoder.field(jsonBody, "user", JsonDecoder.field(
                "email", JsonDecoder::string
        ));
        var username = JsonDecoder.field(jsonBody, "user", JsonDecoder.field(
                "username", JsonDecoder::string
        ));
        var token = JsonDecoder.field(jsonBody, "user", JsonDecoder.field(
                "token", JsonDecoder::string
        ));

        assertEquals(email, user.email());
        assertEquals(username, user.username());
        assertNotNull(token);
    }

    @Test
    public void testBadLogin() throws Exception {
        var user = AuthUser.create(rootHandler);
        var response = rootHandler.handle(new Request(
                "POST",
                "/api/users/login",
                "",
                List.of(user.authHeader()),
                Json.objectBuilder()
                        .put("user", Json.objectBuilder()
                                .put("email", "Gibber")
                                .put("password", "ish"))
                        .build()
                        .toString()
                        .getBytes(StandardCharsets.UTF_8)
        )).intoResponse();

        assertEquals(401, response.status());
    }
}
