package dev.mccue.microhttp.realworld;

import dev.mccue.json.Json;
import dev.mccue.json.JsonDecodeException;
import dev.mccue.json.JsonDecoder;
import dev.mccue.json.JsonReadException;
import org.microhttp.Request;

import java.nio.charset.StandardCharsets;

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
}
