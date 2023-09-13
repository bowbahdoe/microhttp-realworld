package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.domain.AuthContext;
import org.microhttp.Request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GetProfileHandler extends AuthenticatedRouteHandler {
    public GetProfileHandler() {
        super(
                "GET",
                Pattern.compile("/api/profiles/(?<username>.+)")
        );
    }

    @Override
    protected IntoResponse handleAuthenticatedRoute(
            AuthContext authContext,
            Matcher matcher,
            Request request
    ) throws Exception {
        var username = matcher.group("username");

        return null;
    }
}
