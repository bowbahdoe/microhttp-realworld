package dev.mccue.microhttp.realworld;

import dev.mccue.json.Json;
import dev.mccue.json.JsonEncodable;
import dev.mccue.reasonphrase.ReasonPhrase;
import org.microhttp.Header;
import org.microhttp.Response;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record JsonResponse(
        int status,
        List<Header> headers,
        JsonEncodable body
) implements IntoResponse {
    public JsonResponse(JsonEncodable body) {
        this(200, List.of(), body);
    }

    public JsonResponse(int status, JsonEncodable body) {
        this(status, List.of(), body);
    }

    @Override
    public Response intoResponse() {
        var headers = new ArrayList<>(headers());
        headers.add(
                new Header("Content-Type", "application/json; charset=utf-8")
        );
        return new Response(
                status,
                ReasonPhrase.forStatus(status),
                Collections.unmodifiableList(headers),
                Json.writeString(body).getBytes(StandardCharsets.UTF_8)
        );
    }
}
