package dev.mccue.microhttp.realworld;

import dev.mccue.json.Json;

import java.util.List;

public final class Responses {
    private Responses() {}

    public static JsonResponse validationError(List<String> messages) {
        return new JsonResponse(
                422,
                Json.objectBuilder()
                        .put("errors", Json.objectBuilder()
                                .put("body", Json.of(messages, Json::of)))
        );
    }

    public static JsonResponse unauthenticated() {
        return new JsonResponse(
                401,
                Json.objectBuilder()
                        .put("errors", Json.objectBuilder()
                                .put("body", Json.arrayBuilder()
                                        .add(Json.of("unauthenticated"))))
        );
    }

    public static JsonResponse notTheAuthor() {
        return new JsonResponse(
                401,
                Json.objectBuilder()
                        .put("errors", Json.objectBuilder()
                                .put("body", Json.arrayBuilder()
                                        .add(Json.of("not the author of the article"))))
        );
    }

    public static JsonResponse internalError() {
        return new JsonResponse(
                401,
                Json.objectBuilder()
                        .put("errors", Json.objectBuilder()
                                .put("request", Json.arrayBuilder()
                                        .add(Json.of("internal error"))))
        );
    }

    public static JsonResponse notFound() {
        return new JsonResponse(
                404,
                Json.objectBuilder()
                        .put("errors", Json.objectBuilder()
                                .put("body", Json.arrayBuilder()
                                        .add(Json.of("not found"))))
        );
    }
}
