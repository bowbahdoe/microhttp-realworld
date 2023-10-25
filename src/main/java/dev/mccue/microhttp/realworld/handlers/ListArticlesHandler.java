package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.microhttp.handler.IntoResponse;
import dev.mccue.microhttp.realworld.AuthContext;
import dev.mccue.microhttp.realworld.UriUtils;
import org.jspecify.annotations.Nullable;
import org.microhttp.Request;
import org.sqlite.SQLiteDataSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ListArticlesHandler
    extends MaybeAuthenticatedRouteHandler {
    private final SQLiteDataSource db;

    public ListArticlesHandler(
            SQLiteDataSource db
    ) {
        super("GET", Pattern.compile("/api/articles"));
        this.db = db;
    }

    @Override
    protected IntoResponse handleMaybeAuthenticatedRoute(
            @Nullable AuthContext authContext,
            Matcher matcher,
            Request request
    ) throws URISyntaxException {
        var params = UriUtils.queryParams(new URI(request.uri()));

        var tag = params.get("tag");
        var author = params.get("author");
        var favorited = params.get("favorited");

        int limit = 20;
        try {
            limit = Integer.parseInt(params.get("limit"));
        } catch (NumberFormatException e) {
            // pass
        }

        int offset = 0;
        try {
            offset = Integer.parseInt(params.get("offset"));
        } catch (NumberFormatException e) {
            // pass
        }

        var query = """
                SELECT
                    article.slug,
                    article.title,
                    article.description,
                    article.body,
                    article.?? as tagList,
                    article.created_at,
                    article.updated_at,
                    author.username as author_username,
                    author.bio as author_bio,
                    author.image as author_image,
                    follow.follow_id as follow_id,
                    
                FROM article
                LEFT JOIN article_favorite
                    ON article_favorite.article_id = article.id
                JOIN user
                    ON article_favorite.user_id = user.id
                WHERE
                    user.username = ?
                ORDER BY
                    article.updated_at DESC, article.id DESC
                LIMIT ?
                OFFSET ?
                """;


        record Filter(String expression, Object value) { }

        var filters = new ArrayList<Filter>();
        if (tag != null) {
            filters.add(new Filter("tag = ?", tag));
        }

        if (author != null) {
            filters.add(new Filter("author = ?", tag));
        }

        if (favorited != null) {
            filters.add(new Filter("favorited = ?", tag));
        }



        return null;
    }
}
