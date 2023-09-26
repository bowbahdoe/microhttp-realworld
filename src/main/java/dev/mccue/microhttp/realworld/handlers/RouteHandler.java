package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.microhttp.handler.Handler;
import dev.mccue.microhttp.handler.IntoResponse;
import org.jspecify.annotations.Nullable;
import org.microhttp.Request;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RouteHandler implements Handler {
    private final String method;
    private final Pattern pattern;

    protected RouteHandler(String method, Pattern pattern) {
        this.method = method;
        this.pattern = pattern;
    }

    protected abstract @Nullable IntoResponse handleRoute(
            Matcher routeMatch,
            Request request
    ) throws Exception;

    @Override
    public final @Nullable IntoResponse handle(Request request) throws Exception {
        if (!method.equalsIgnoreCase(request.method())) {
            return null;
        }

        var matcher = pattern.matcher(new URI(request.uri()).getPath());

        if (!matcher.matches()) {
            return null;
        }

        return handleRoute(matcher, request);
    }
}
