package dev.mccue.microhttp.realworld.test;


import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import dev.mccue.json.JsonNull;
import dev.mccue.json.JsonString;
import dev.mccue.microhttp.realworld.DB;
import dev.mccue.microhttp.realworld.handlers.RegisterHandler;
import org.junit.jupiter.api.Test;
import org.microhttp.Request;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class RegisterHandlerTest {
    @Test
    public void testRegisterNewUser() throws Exception {
        var db = DB.getDB(Files.createTempFile(null, null));

        var handler = new RegisterHandler(db);

        var response = handler.handle(new Request(
                "POST",
                "/api/users",
                "",
                List.of(),
                """
                {
                  "user": {
                    "username": "bob",
                    "password": "123",
                    "email": "a@b.c"
                  }
                }
                """.getBytes(StandardCharsets.UTF_8)
        ));

        assertNotNull(response);

        var responseJson =
                Json.readString(new String(response.intoResponse().body(), StandardCharsets.UTF_8));

        var userInfo = JsonDecoder.field(responseJson, "user", JsonDecoder::object);

        assertEquals(
                JsonString.of("a@b.c"),
                userInfo.get("email")
        );

        assertEquals(
                JsonString.of("bob"),
                userInfo.get("username")
        );

        assertEquals(
                JsonNull.instance(),
                userInfo.get("bio")
        );

        assertEquals(
                JsonNull.instance(),
                userInfo.get("image")
        );

        assertInstanceOf(JsonString.class, userInfo.get("token"));
    }

    @Test
    public void testDuplicateEmail() throws Exception {
        var db = DB.getDB(Files.createTempFile(null, null));

        var handler = new RegisterHandler(db);

        var firstResponse = handler.handle(new Request(
                "POST",
                "/api/users",
                "",
                List.of(),
                """
                {
                  "user": {
                    "username": "bob",
                    "password": "123",
                    "email": "a@b.c"
                  }
                }
                """.getBytes(StandardCharsets.UTF_8)
        ));

        assertEquals(200, Objects.requireNonNull(firstResponse).intoResponse().status());

        var secondResponse = handler.handle(new Request(
                "POST",
                "/api/users",
                "",
                List.of(),
                """
                {
                  "user": {
                    "username": "bobby",
                    "password": "123",
                    "email": "a@b.c"
                  }
                }
                """.getBytes(StandardCharsets.UTF_8)
        ));

        var response = Objects.requireNonNull(secondResponse).intoResponse();
        assertEquals(422, response.status());
        assertEquals(
                        Json.readString("""
                            {
                                "errors": {
                                    "body": [
                                        "email taken"
                                    ]
                                }
                            }
                            """
                        ),
                Json.readString(new String(response.body(), StandardCharsets.UTF_8))
        );
    }

    @Test
    public void testDuplicateUsername() throws Exception {
        var db = DB.getDB(Files.createTempFile(null, null));

        var handler = new RegisterHandler(db);

        var firstResponse = handler.handle(new Request(
                "POST",
                "/api/users",
                "",
                List.of(),
                """
                {
                  "user": {
                    "username": "bob",
                    "password": "123",
                    "email": "a@b.c"
                  }
                }
                """.getBytes(StandardCharsets.UTF_8)
        ));

        assertEquals(200, Objects.requireNonNull(firstResponse).intoResponse().status());

        var secondResponse = handler.handle(new Request(
                "POST",
                "/api/users",
                "",
                List.of(),
                """
                {
                  "user": {
                    "username": "bob",
                    "password": "123",
                    "email": "d@e.f"
                  }
                }
                """.getBytes(StandardCharsets.UTF_8)
        ));

        var response = Objects.requireNonNull(secondResponse).intoResponse();
        assertEquals(422, response.status());
        assertEquals(
                Json.readString("""
                            {
                                "errors": {
                                    "body": [
                                        "username taken"
                                    ]
                                }
                            }
                            """
                ),
                Json.readString(new String(response.body(), StandardCharsets.UTF_8))
        );
    }
}
