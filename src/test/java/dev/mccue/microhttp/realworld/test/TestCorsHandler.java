package dev.mccue.microhttp.realworld.test;

import dev.mccue.microhttp.realworld.handlers.CorsHandler;
import org.junit.jupiter.api.Test;
import org.microhttp.Request;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class TestCorsHandler {
    @Test
    public void testOptionsRequest() throws Exception {
        for (var uri : List.of("/", "/abc", "/a/b/c/d/e/f/g")) {
            var handler = new CorsHandler();
            var response = Objects.requireNonNull(
                    handler.handle(new Request("OPTIONS", uri, "1.1", List.of(), new byte[]{}))
            ).intoResponse();

            assertEquals(200, response.status());
            assertTrue(response.hasHeader("Access-Control-Allow-Origin"));
            assertTrue(response.hasHeader("Access-Control-Allow-Headers"));
        }

    }

    @Test
    public void testOtherRequests() throws Exception {
        var handler = new CorsHandler();
        assertNull(handler.handle(new Request("GET", "/", "1.1", List.of(), new byte[]{})));
        assertNull(handler.handle(new Request("POST", "/", "1.1", List.of(), new byte[]{})));
        assertNull(handler.handle(new Request("PUT", "/", "1.1", List.of(), new byte[]{})));
        assertNull(handler.handle(new Request("PATCH", "/", "1.1", List.of(), new byte[]{})));
        assertNull(handler.handle(new Request("DELETE", "/", "1.1", List.of(), new byte[]{})));
    }
}
