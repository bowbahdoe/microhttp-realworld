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

public final class UnfollowUserHandler extends AuthenticatedRouteHandler {
    private final UserService userService;

    public UnfollowUserHandler(
            AuthService authService,
            UserService userService
    ) {
        super(
                "DELETE",
                Pattern.compile("/api/profiles/(?<username>.+)/follow"),
                authService
        );
        this.userService = userService;
    }

    @Override
    protected IntoResponse handleAuthenticatedRoute(User user, Matcher matcher, Request request) {
        var username = matcher.group("username");

        User userToUnfollow = null;
        if (username != null) {
            userToUnfollow = userService
                    .findByUsername(username)
                    .orElse(null);
        }


        if (userToUnfollow != null) {
            userService.unfollow(user.id(), userToUnfollow.id());
            return new ProfileResponse(
                    userToUnfollow.username(),
                    userToUnfollow.bio(),
                    userToUnfollow.image(),
                    false
            );
        }
        else {
            return Responses.validationError(List.of("invalid user"));
        }
    }
}