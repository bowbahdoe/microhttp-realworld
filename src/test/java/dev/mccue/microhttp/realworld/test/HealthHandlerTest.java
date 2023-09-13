package dev.mccue.microhttp.realworld.test;

import dev.mccue.json.Json;
import dev.mccue.microhttp.realworld.Env;
import dev.mccue.microhttp.realworld.JsonResponse;
import dev.mccue.microhttp.realworld.handlers.HealthHandler;
import org.junit.jupiter.api.Test;
import org.microhttp.Request;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HealthHandlerTest {
    @Test
    public void testHealthHandler() throws Exception {
        assertEquals(
                new JsonResponse(
                        Json.objectBuilder()
                                .put("environment", Env.ENVIRONMENT)
                ),
                new HealthHandler().handle(new Request(
                        "GET", "/api/health", "", List.of(), new byte[0]
                ))
        );
        assertNull(
                new HealthHandler().handle(new Request(
                        "POST", "/api/health", "", List.of(), new byte[0]
                ))
        );
        assertNull(
                new HealthHandler().handle(new Request(
                        "GET", "/api/other", "", List.of(), new byte[0]
                ))
        );
    }
}
