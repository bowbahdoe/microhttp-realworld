package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.domain.User;
import dev.mccue.microhttp.realworld.domain.UserResponse;
import dev.mccue.microhttp.realworld.service.AuthService;
import org.microhttp.Request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GetCurrentUserHandler
        extends AuthenticatedRouteHandler {
    public GetCurrentUserHandler(AuthService authService) {
        super(
                "GET",
                Pattern.compile("/api/user"),
                authService
        );
    }

    @Override
    protected IntoResponse handleAuthenticatedRoute(
            User user,
            Matcher matcher,
            Request request
    ) {
        return new UserResponse(
                user,
                authService.jwtForUser(user)
        );
    }
}
