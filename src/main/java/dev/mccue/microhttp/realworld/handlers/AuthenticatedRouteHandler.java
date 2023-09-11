package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.json.Json;
import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.JsonResponse;
import dev.mccue.microhttp.realworld.Responses;
import dev.mccue.microhttp.realworld.domain.User;
import dev.mccue.microhttp.realworld.service.AuthService;
import org.jspecify.annotations.Nullable;
import org.microhttp.Request;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AuthenticatedRouteHandler extends RouteHandler {
    protected final AuthService authService;

    protected AuthenticatedRouteHandler(
            List<String> methods,
            Pattern pattern,
            AuthService authService
    ) {
        super(methods, pattern);
        this.authService = authService;
    }

    protected AuthenticatedRouteHandler(
            String method,
            Pattern pattern,
            AuthService authService
    ) {
        super(method, pattern);
        this.authService = authService;
    }

    private static Optional<String> authTokenFromRequest(Request request) {
        String authHeader = null;
        for (var header : request.headers()) {
            if (header.name().equalsIgnoreCase("authorization")) {
                authHeader = header.value();
            }
        }

        if (authHeader == null) {
            return Optional.empty();
        }
        else {
            var split = authHeader.split(" ");
            if (split.length != 2 || !"Token".equalsIgnoreCase(split[0])) {
                return Optional.empty();
            }
            else {
                return Optional.of(split[1]);
            }
        }
    }

    private static Optional<User> userForRequest(AuthService authService, Request request) {
        var authToken = authTokenFromRequest(request).orElse(null);
        if (authToken == null) {
            return Optional.empty();
        }

        var user = authService.decodeJwt(authToken).orElse(null);
        if (user == null) {
            return Optional.empty();
        }

        return Optional.of(user);
    }


    protected abstract @Nullable IntoResponse handleAuthenticatedRoute(
            User user,
            Matcher matcher,
            Request request
    ) throws Exception;

    @Override
    protected final @Nullable IntoResponse handleRoute(
            Matcher matcher,
            Request request
    ) throws Exception {
        var authToken = authTokenFromRequest(request).orElse(null);

        if (authToken == null) {
            return new JsonResponse(
                    401,
                    Json.objectBuilder()
                            .put("errors", Json.objectBuilder()
                                    .put("body", Json.arrayBuilder()
                                            .add("unauthenticated")))
            );
        }

        var user = userForRequest(authService, request).orElse(null);
        if (user == null) {
            return Responses.unauthenticated();
        }

        return handleAuthenticatedRoute(
                user,
                matcher,
                request
        );
    }
}
