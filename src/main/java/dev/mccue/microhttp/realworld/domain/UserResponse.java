package dev.mccue.microhttp.realworld.domain;

import dev.mccue.json.Json;
import dev.mccue.json.JsonEncodable;
import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.JsonResponse;
import org.microhttp.Response;

public record UserResponse(
        User user,
        String token
) implements JsonEncodable, IntoResponse {
    @Override
    public Json toJson() {
        return Json.objectBuilder()
                .put(
                        "user",
                        Json.objectBuilder()
                                .put("email", Json.of(user.email()))
                                .put("username", Json.of(user.username()))
                                .put("bio", Json.of(user.bio().orElse(null)))
                                .put("image", Json.of(user.image().orElse(null)))
                                .put("token", Json.of(token))
                )
                .build();
    }

    @Override
    public Response intoResponse() {
        return new JsonResponse(this).intoResponse();
    }
}
