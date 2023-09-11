package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.domain.User;
import dev.mccue.microhttp.realworld.service.AuthService;
import org.microhttp.Request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GetProfileHandler
    extends AuthenticatedRouteHandler {
    public GetProfileHandler(AuthService authService) {
        super("GET", Pattern.compile("/api/profiles/(?<username>.+)"), authService);
    }

    @Override
    protected IntoResponse handleAuthenticatedRoute(
            User user,
            Matcher matcher,
            Request request
    ) throws Exception {
        var username = matcher.group("username");

        return null;
    }
}
