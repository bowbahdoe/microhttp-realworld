package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import dev.mccue.microhttp.realworld.BodyUtils;
import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.Responses;
import dev.mccue.microhttp.realworld.domain.UserResponse;
import dev.mccue.microhttp.realworld.service.AuthService;
import dev.mccue.microhttp.realworld.service.UserService;
import org.microhttp.Request;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LoginHandler extends RouteHandler {
    private final UserService userService;
    private final AuthService authService;

    public LoginHandler(UserService userService, AuthService authService) {
        super("POST", Pattern.compile("/api/users/login"));
        this.userService = userService;
        this.authService = authService;
    }

    public record LoginRequest(
            String email,
            String password
    ) {
        public static LoginRequest fromJson(Json json) {
            return JsonDecoder.field(json, "user", user -> new LoginRequest(
                    JsonDecoder.field(user, "email", JsonDecoder::string),
                    JsonDecoder.field(user, "password", JsonDecoder::string)
            ));
        }
    }

    @Override
    protected IntoResponse handleRoute(Matcher routeMatch, Request request) {
        var loginRequest = BodyUtils.parseBody(request, LoginRequest::fromJson);

        var badEmailOrPassword = Responses.validationError(List.of("invalid email or password"));

        var user = userService.findByEmail(loginRequest.email).orElse(null);
        if (user == null || !user.isCorrectPassword(loginRequest.password())) {
            return badEmailOrPassword;
        }
        else {
            return new UserResponse(user, authService.jwtForUser(user));
        }
    }
}