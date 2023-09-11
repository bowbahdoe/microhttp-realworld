package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.json.Json;
import dev.mccue.microhttp.realworld.JsonResponse;
import org.microhttp.Request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HelloHandler extends RouteHandler {
    public HelloHandler() {
        super("GET", Pattern.compile("/hello"));
    }

    @Override
    protected JsonResponse handleRoute(Matcher routeMatch, Request request) {
        return new JsonResponse(Json.of("Hello, world"));
    }
}
