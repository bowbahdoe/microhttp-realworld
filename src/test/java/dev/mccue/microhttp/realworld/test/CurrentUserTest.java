package dev.mccue.microhttp.realworld.test;

import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import org.junit.jupiter.api.Test;
import org.microhttp.Request;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CurrentUserTest extends HandlerTest {
    @Test
    public void getCurrentUser() throws Exception {
        var user = AuthUser.create(rootHandler);
        var response = rootHandler.handle(new Request(
                "GET",
                "/api/user",
                "",
                List.of(user.authHeader()),
                new byte[0]
        )).intoResponse();
        assertEquals(200, response.status());
        var jsonBody = Json.readString(new String(response.body(), StandardCharsets.UTF_8));
        var email = JsonDecoder.field(jsonBody, "user", JsonDecoder.field(
                "email", JsonDecoder::string
        ));
        var username = JsonDecoder.field(jsonBody, "user", JsonDecoder.field(
                "username", JsonDecoder::string
        ));
        assertEquals(email, user.email());
        assertEquals(username, user.username());
    }
}
