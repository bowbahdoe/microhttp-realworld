package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.RequestUtils;
import dev.mccue.microhttp.realworld.domain.ArticleResponse;
import dev.mccue.microhttp.realworld.domain.ArticleSearchQuery;
import dev.mccue.microhttp.realworld.domain.ArticleSearchQueryBuilder;
import dev.mccue.microhttp.realworld.domain.User;
import dev.mccue.microhttp.realworld.service.ArticleService;
import dev.mccue.microhttp.realworld.service.AuthService;
import dev.mccue.microhttp.realworld.service.UserService;
import org.microhttp.Request;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ListArticlesHandler
    extends AuthenticatedRouteHandler {
    private final ArticleService articleService;
    private final UserService userService;

    public ListArticlesHandler(
            AuthService authService,
            ArticleService articleService,
            UserService userService
    ) {
        super("GET", Pattern.compile("/api/articles"), authService);
        this.articleService = articleService;
        this.userService = userService;
    }

    @Override
    protected IntoResponse handleAuthenticatedRoute(
            User user,
            Matcher matcher,
            Request request
    ) {
        var params = RequestUtils.parseUri(request).queryParams();

        var tag = params.get("tag");
        var author = params.get("author");
        var favorited = params.get("favorited");
        var limit = params.get("limit");
        var offset = params.get("offset");

        var queryBuilder = ArticleSearchQueryBuilder.builder(new ArticleSearchQuery());
        if (tag != null) {
            queryBuilder.tag(Optional.of(tag));
        }

        if (author != null) {
            queryBuilder.author(Optional.of(author));
        }

        if (favorited != null) {
            queryBuilder.favorited(Optional.of(favorited));
        }

        if (limit != null) {
            try {
                int limitInt = Integer.parseInt(limit);
                queryBuilder.limit(limitInt);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (offset != null) {
            try {
                int offsetInt = Integer.parseInt(offset);
                queryBuilder.offset(offsetInt);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        var query = queryBuilder.build();

        ArticleResponse.forQuery(
                articleService,
                userService,
                query
        );

        return null;
    }
}
