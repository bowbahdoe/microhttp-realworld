package dev.mccue.microhttp.realworld;

import dev.mccue.json.Json;
import dev.mccue.json.JsonDecodeException;
import dev.mccue.json.JsonDecoder;
import dev.mccue.json.JsonReadException;
import org.microhttp.Request;
import org.microhttp.Response;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class RequestUtils {
    private RequestUtils() {}

    public static <T> T parseBody(Request request, JsonDecoder<T> decoder) {
        try {
            return decoder.decode(Json.readString(new String(
                    request.body(),
                    StandardCharsets.UTF_8
            )));
        } catch (JsonReadException | JsonDecodeException e) {
            throw new ValidationException(e);
        }
    }

    private static final class ValidationException
            extends RuntimeException
            implements IntoResponse {
        private final IntoResponse intoResponse;
        ValidationException(Throwable e) {
            super(e);
            this.intoResponse = Responses.validationError(List.of(e.getMessage()));
        }

        @Override
        public Response intoResponse() {
            return this.intoResponse.intoResponse();
        }
    }

    public record ParsedUri(String base, String queryString) {
        public Map<String, String> queryParams() {
            return Arrays.stream(queryString.split("&"))
                    .map(query -> query.split("="))
                    .filter(query -> query.length == 2)
                    .collect(Collectors.toUnmodifiableMap(
                            query -> URLDecoder.decode(query[0], StandardCharsets.UTF_8),
                            query -> URLDecoder.decode(query[1], StandardCharsets.UTF_8)
                    ));
        }
    }

    public static ParsedUri parseUri(Request request) {
        var split = request.uri().split("\\?", 2);

        if (split.length == 1) {
            return new ParsedUri(split[0], null);
        }
        else {
            return new ParsedUri(split[0], split[1]);
        }
    }
}
