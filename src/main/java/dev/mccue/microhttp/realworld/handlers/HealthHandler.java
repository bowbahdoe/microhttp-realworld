package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.json.Json;
import dev.mccue.microhttp.realworld.Env;
import dev.mccue.microhttp.realworld.JsonResponse;
import org.microhttp.Request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HealthHandler extends RouteHandler {
    public HealthHandler() {
        super("GET", Pattern.compile("/api/health"));
    }

    @Override
    protected JsonResponse handleRoute(Matcher routeMatch, Request request) {
        return new JsonResponse(
                Json.objectBuilder()
                        .put("environment", Env.ENVIRONMENT)
        );
    }
}
