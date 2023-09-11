package dev.mccue.microhttp.realworld.domain;


import dev.mccue.json.Json;
import dev.mccue.json.JsonEncodable;
import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.JsonResponse;
import org.microhttp.Response;

import java.util.Optional;

public record ProfileResponse(
        String username,
        Optional<String> bio,
        Optional<String> image,
        boolean following
) implements IntoResponse, JsonEncodable {
    public Json toJson() {
        return Json.objectBuilder()
                .put("profile", Json.objectBuilder()
                        .put("username", Json.of(username))
                        .put("bio", Json.of(bio.orElse(null)))
                        .put("image", Json.of(image.orElse(null)))
                        .put("following", Json.of(following))
                        .build())
                .build();
    }

    @Override
    public Response intoResponse() {
        return new JsonResponse(this).intoResponse();
    }
}
