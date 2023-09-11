package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.json.Json;
import dev.mccue.microhttp.realworld.JsonResponse;
import dev.mccue.microhttp.realworld.domain.User;
import dev.mccue.microhttp.realworld.service.AuthService;
import dev.mccue.microhttp.realworld.service.TagService;
import org.microhttp.Request;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GetTagsHandler extends AuthenticatedRouteHandler {
    private final TagService tagService;

    public GetTagsHandler(TagService tagService, AuthService authService) {
        super("GET", Pattern.compile("/api/tags"), authService);
        this.tagService = tagService;
    }


    @Override
    protected JsonResponse handleAuthenticatedRoute(
            User user,
            Matcher matcher,
            Request request
    ) {
        return new JsonResponse(
                Json.objectBuilder()
                .put("tags", Json.of(
                        tagService.all(),
                        tag -> Json.of(tag.name())
                ))
                .build()
        );
    }
}
