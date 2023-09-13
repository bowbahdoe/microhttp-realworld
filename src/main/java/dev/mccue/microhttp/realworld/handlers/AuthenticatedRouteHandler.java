package dev.mccue.microhttp.realworld.handlers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.mccue.json.Json;
import dev.mccue.json.JsonDecodeException;
import dev.mccue.json.JsonDecoder;
import dev.mccue.json.JsonReadException;
import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.JsonResponse;
import dev.mccue.microhttp.realworld.Responses;
import dev.mccue.microhttp.realworld.domain.AuthContext;
import org.jspecify.annotations.Nullable;
import org.microhttp.Request;

import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AuthenticatedRouteHandler extends RouteHandler {
    protected AuthenticatedRouteHandler(
            String method,
            Pattern pattern
    ) {
        super(method, pattern);
    }

    public interface Callback {
        @Nullable IntoResponse handleAuthenticatedRoute(
                AuthContext user,
                Matcher matcher,
                Request request
        ) throws Exception;
    }

    public static AuthenticatedRouteHandler of(
            String method, Pattern pattern, Callback callback
    ) {
        return new AuthenticatedRouteHandler(method, pattern) {
            @Override
            protected @Nullable IntoResponse handleAuthenticatedRoute(
                    AuthContext user,
                    Matcher matcher,
                    Request request
            ) throws Exception {
                return callback.handleAuthenticatedRoute(user, matcher, request);
            }
        };
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

    private static Optional<AuthContext> decodeJwt(String authToken) {
        DecodedJWT decodedJWT;
        try {
            var algorithm = Algorithm.HMAC256(
                    Objects.requireNonNull(System.getenv("JWT_SECRET"))
            );
            var verifier = JWT.require(algorithm).build();
            decodedJWT = verifier.verify(authToken);
        } catch (JWTVerificationException __) {
            return Optional.empty();
        }

        long userId;
        try {
            var json = Json.readString(new String(
                    Base64.getDecoder().decode(decodedJWT.getPayload())
            ));
            userId = Long.parseLong(JsonDecoder.field(json, "user_id", JsonDecoder::string));
        } catch (JsonDecodeException | JsonReadException | NumberFormatException __) {
            return Optional.empty();
        }

        return Optional.of(new AuthContext(userId));
    }

    private static Optional<AuthContext> userForRequest(Request request) {
        return authTokenFromRequest(request)
                .flatMap(AuthenticatedRouteHandler::decodeJwt);
    }


    protected abstract @Nullable IntoResponse handleAuthenticatedRoute(
            AuthContext user,
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

        var user = userForRequest(request).orElse(null);
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
