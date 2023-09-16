package dev.mccue.microhttp.realworld.domain;

import dev.mccue.json.Json;
import dev.mccue.json.JsonEncodable;
import dev.mccue.json.JsonString;

import java.util.Optional;

public record ArticleSlug(ExternalId externalId, String title) implements JsonEncodable {
    public static Optional<ArticleSlug> fromString(String value) {
        var split = value.split("-", 2);
        if (split.length != 2) {
            return Optional.empty();
        }
        else {
            return Optional.of(new ArticleSlug(new ExternalId(split[0]), split[1]));
        }
    }

    @Override
    public String toString() {
        return externalId.value() + "-" + title;
    }

    @Override
    public Json toJson() {
        return JsonString.of(this.toString());
    }
}
