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
import dev.mccue.microhttp.realworld.Responses;
import dev.mccue.microhttp.realworld.domain.AuthContext;
import org.jspecify.annotations.Nullable;
import org.microhttp.Request;

import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AuthenticatedRouteHandler
        extends MaybeAuthenticatedRouteHandler {
    protected AuthenticatedRouteHandler(
            String method,
            Pattern pattern
    ) {
        super(method, pattern);
    }

    protected abstract @Nullable IntoResponse handleAuthenticatedRoute(
            AuthContext authContext,
            Matcher matcher,
            Request request
    ) throws Exception;

    protected final @Nullable IntoResponse handleMaybeAuthenticatedRoute(
            @Nullable AuthContext authContext,
            Matcher matcher,
            Request request
    ) throws Exception {
        if (authContext == null) {
            return Responses.unauthenticated();
        }
        else {
            return handleAuthenticatedRoute(authContext, matcher, request);
        }
    }
}
