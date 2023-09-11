package dev.mccue.microhttp.realworld.domain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public record ArticleSlug(ExternalId externalId, String title) {
    public ArticleSlug {
        Objects.requireNonNull(externalId, "externalId must not be null");
        Objects.requireNonNull(title, "title must not be null");
    }

    public static ArticleSlug fromTitle(String title) {
        return fromTitle(ThreadLocalRandom.current(), title);
    }

    public static ArticleSlug fromTitle(Random random, String title) {
        return new ArticleSlug(ExternalId.generate(random), title);
    }

    public boolean matches(String slugText) {
        return slugText.startsWith(externalId.value());
    }

    @Override
    public String toString() {
        var urlSafe = URLEncoder.encode(
                title.replaceAll("\\s+", "-"),
                StandardCharsets.UTF_8
        ).toLowerCase();

        return externalId.value() + "-" + urlSafe;
    }
}
