package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import dev.mccue.microhttp.realworld.RequestUtils;
import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.domain.PasswordHash;
import dev.mccue.microhttp.realworld.domain.User;
import dev.mccue.microhttp.realworld.domain.UserResponse;
import dev.mccue.microhttp.realworld.service.AuthService;
import dev.mccue.microhttp.realworld.service.UserService;
import org.microhttp.Request;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UpdateUserHandler
        extends AuthenticatedRouteHandler {
    private final UserService userService;

    public UpdateUserHandler(
            AuthService authService,
            UserService userService
    ) {
        super("PUT", Pattern.compile("/api/user"), authService);
        this.userService = userService;
    }

    public record UpdateUserRequest(
            Optional<String> email,
            Optional<String> username,
            Optional<String> password,
            Optional<String> image,
            Optional<String> bio
    ) {
        public static UpdateUserRequest fromJson(Json json) {
            return new UpdateUserRequest(
                    JsonDecoder.optionalField(json, "email", JsonDecoder::string),
                    JsonDecoder.optionalField(json, "username", JsonDecoder::string),
                    JsonDecoder.optionalField(json, "password", JsonDecoder::string),
                    JsonDecoder.optionalField(json, "image", JsonDecoder::string),
                    JsonDecoder.optionalField(json, "bio", JsonDecoder::string)
            );
        }
    }

    @Override
    protected IntoResponse handleAuthenticatedRoute(
            User user,
            Matcher matcher,
            Request request
    ) {
        var updateUserRequest = RequestUtils.parseBody(request, UpdateUserRequest::fromJson);
        var updatedUser = new User(
                user.id(),
                updateUserRequest.email.orElse(user.email()),
                updateUserRequest.username.orElse(user.username()),
                updateUserRequest.bio.or(user::bio),
                updateUserRequest.image.or(user::image),
                updateUserRequest.password
                        .map(PasswordHash::fromUnHashedPassword)
                        .orElse(user.passwordHash())
        );
        userService.update(updatedUser);

        return new UserResponse(
                updatedUser,
                authService.jwtForUser(updatedUser)
        );
    }
}
