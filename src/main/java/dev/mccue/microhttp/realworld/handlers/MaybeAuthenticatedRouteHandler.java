package dev.mccue.microhttp.realworld.handlers;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.mccue.json.Json;
import dev.mccue.json.JsonDecodeException;
import dev.mccue.json.JsonDecoder;
import dev.mccue.json.JsonReadException;
import dev.mccue.microhttp.realworld.Env;
import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.JsonResponse;
import dev.mccue.microhttp.realworld.AuthContext;
import org.jspecify.annotations.Nullable;
import org.microhttp.Request;

import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class MaybeAuthenticatedRouteHandler extends RouteHandler {
    protected MaybeAuthenticatedRouteHandler(
            String method,
            Pattern pattern
    ) {
        super(method, pattern);
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
                    Objects.requireNonNull(Env.JWT_SECRET)
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

    private static Optional<AuthContext> authContextForRequest(Request request) {
        return authTokenFromRequest(request)
                .flatMap(MaybeAuthenticatedRouteHandler::decodeJwt);
    }

    protected abstract @Nullable IntoResponse handleMaybeAuthenticatedRoute(
            @Nullable AuthContext authContext,
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

        var authContext = authContextForRequest(request)
                .orElse(null);

        return handleMaybeAuthenticatedRoute(
                authContext,
                matcher,
                request
        );
    }
}
