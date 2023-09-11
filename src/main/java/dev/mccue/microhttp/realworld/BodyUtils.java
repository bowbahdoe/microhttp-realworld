package dev.mccue.microhttp.realworld;

import dev.mccue.json.Json;
import dev.mccue.json.JsonDecodeException;
import dev.mccue.json.JsonDecoder;
import dev.mccue.json.JsonReadException;
import org.microhttp.Request;
import org.microhttp.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class BodyUtils {
    private BodyUtils() {}

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
}
