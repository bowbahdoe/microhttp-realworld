package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.microhttp.realworld.Handler;
import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.RequestUtils;
import org.jspecify.annotations.Nullable;
import org.microhttp.Request;

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

        var matcher = pattern.matcher(RequestUtils.parseUri(request).base());

        if (!matcher.matches()) {
            return null;
        }

        return handleRoute(matcher, request);
    }
}
