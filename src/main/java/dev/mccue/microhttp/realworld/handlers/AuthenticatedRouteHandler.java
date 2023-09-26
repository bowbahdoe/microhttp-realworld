package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.microhttp.handler.IntoResponse;
import dev.mccue.microhttp.realworld.Responses;
import dev.mccue.microhttp.realworld.AuthContext;
import org.jspecify.annotations.Nullable;
import org.microhttp.Request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AuthenticatedRouteHandler
        extends MaybeAuthenticatedRouteHandler {
    protected AuthenticatedRouteHandler(
            String method,
            Pattern pattern
    ) {
        super(method, pattern);
    }

    protected abstract @Nullable IntoResponse handleAuthenticatedRoute(
            AuthContext authContext,
            Matcher matcher,
            Request request
    ) throws Exception;

    protected final @Nullable IntoResponse handleMaybeAuthenticatedRoute(
            @Nullable AuthContext authContext,
            Matcher matcher,
            Request request
    ) throws Exception {
        if (authContext == null) {
            return Responses.unauthenticated();
        }
        else {
            return handleAuthenticatedRoute(authContext, matcher, request);
        }
    }
}
