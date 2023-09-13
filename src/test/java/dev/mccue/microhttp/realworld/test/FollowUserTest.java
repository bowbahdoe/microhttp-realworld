package dev.mccue.microhttp.realworld.test;

import dev.mccue.json.Json;
import org.junit.jupiter.api.Test;
import org.microhttp.Request;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FollowUserTest extends HandlerTest {
    @Test
    public void followUser() throws Exception {
        var userA = AuthUser.create(rootHandler);
        var userB = AuthUser.create(rootHandler);

        var response = rootHandler.handle(new Request(
                "POST",
                "/api/profiles/" + userB.username() + "/follow",
                "",
                List.of(userA.authHeader()),
                Json.objectBuilder()
                        .put("", "")
                        .build()
                        .toString()
                        .getBytes(StandardCharsets.UTF_8)
        )).intoResponse();

        assertEquals(200, response.status());

        // TODO
    }
}
