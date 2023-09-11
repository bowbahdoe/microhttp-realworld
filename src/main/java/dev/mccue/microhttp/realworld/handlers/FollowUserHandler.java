package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.Responses;
import dev.mccue.microhttp.realworld.domain.ProfileResponse;
import dev.mccue.microhttp.realworld.domain.User;
import dev.mccue.microhttp.realworld.service.AuthService;
import dev.mccue.microhttp.realworld.service.UserService;
import org.microhttp.Request;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FollowUserHandler extends AuthenticatedRouteHandler {
    private final UserService userService;

    public FollowUserHandler(
            AuthService authService,
            UserService userService
    ) {
        super(
                "POST",
                Pattern.compile("/api/profiles/(?<username>.+)/follow"),
                authService
        );
        this.userService = userService;
    }

    @Override
    protected IntoResponse handleAuthenticatedRoute(User user, Matcher matcher, Request request) throws Exception {
        var username = matcher.group("username");

        User userToFollow = null;
        if (username != null) {
            userToFollow = userService
                    .findByUsername(username)
                    .orElse(null);
        }

        if (userToFollow != null) {
            userService.follow(user.id(), userToFollow.id());
            return new ProfileResponse(
                    userToFollow.username(),
                    userToFollow.bio(),
                    userToFollow.image(),
                    true
            );
        }
        else {
            return Responses.validationError(List.of("invalid user"));
        }
    }
}