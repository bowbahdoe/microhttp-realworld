package dev.mccue.microhttp.realworld.domain;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.Objects;
import java.util.Optional;

@RecordBuilder
public record ArticleSearchQuery(
        Optional<String> tag,
        Optional<String> author,
        Optional<String> favorited,
        int limit,
        int offset
){
    public ArticleSearchQuery() {
        this(Optional.empty(), Optional.empty(), Optional.empty(), 20, 0);
    }

    public ArticleSearchQuery {
        Objects.requireNonNull(tag, "tag must not be null");
        Objects.requireNonNull(author, "author must not be null");
        Objects.requireNonNull(favorited, "favorited must not be null");
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must be non-negative");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must be non-negative");
        }
    }
}
