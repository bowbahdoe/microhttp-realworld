package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.json.Json;
import dev.mccue.json.JsonDecoder;
import dev.mccue.microhttp.realworld.RequestUtils;
import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.Responses;
import dev.mccue.microhttp.realworld.domain.User;
import dev.mccue.microhttp.realworld.domain.UserResponse;
import dev.mccue.microhttp.realworld.service.AuthService;
import dev.mccue.microhttp.realworld.service.UserService;
import dev.mccue.microhttp.realworld.service.UserService.RegistrationResult.EmailTaken;
import dev.mccue.microhttp.realworld.service.UserService.RegistrationResult.Success;
import org.microhttp.Request;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegisterUserHandler extends RouteHandler {
    private final UserService userService;
    private final AuthService authService;

    public RegisterUserHandler(
            AuthService authService,
            UserService userService
    ) {
        super("POST", Pattern.compile("/api/users"));
        this.authService = authService;
        this.userService = userService;
    }

    public record RegisterUserRequest(String email, String username, String password) {
        static RegisterUserRequest fromJson(Json json){
            return JsonDecoder.field(
                    json,
                    "user",
                    userFields -> new RegisterUserRequest(
                            JsonDecoder.field(userFields, "email", JsonDecoder::string),
                            JsonDecoder.field(userFields, "username", JsonDecoder::string),
                            JsonDecoder.field(userFields, "password", JsonDecoder::string)
                    ));
        }
    }

    @Override
    protected IntoResponse handleRoute(
            Matcher routeMatch,
            Request request
    )  {
        var registerUserRequest = RequestUtils.parseBody(
                request,
                RegisterUserRequest::fromJson
        );

        return switch (userService.register(
                registerUserRequest.username,
                registerUserRequest.email,
                registerUserRequest.password
        )) {
            case EmailTaken __ ->
                    Responses.validationError(List.of("email taken"));
            case UserService.RegistrationResult.UsernameTaken __ ->
                    Responses.validationError(List.of("username taken"));
            case Success(User user) ->
                    new UserResponse(user, authService.jwtForUser(user));
        };
    }
}