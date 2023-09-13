package dev.mccue.microhttp.realworld.handlers;

import dev.mccue.microhttp.realworld.IntoResponse;
import dev.mccue.microhttp.realworld.RequestUtils;
import dev.mccue.microhttp.realworld.domain.ArticleResponse;
import dev.mccue.microhttp.realworld.domain.AuthContext;
import org.microhttp.Request;
import org.sqlite.SQLiteDataSource;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ListArticlesHandler
    extends AuthenticatedRouteHandler {
    private final SQLiteDataSource db;

    public ListArticlesHandler(
            SQLiteDataSource db
    ) {
        super("GET", Pattern.compile("/api/articles"));
        this.db = db;
    }

    @Override
    protected IntoResponse handleAuthenticatedRoute(
            AuthContext authContext,
            Matcher matcher,
            Request request
    ) {
        var params = RequestUtils.parseUri(request).queryParams();

        var tag = params.get("tag");
        var author = params.get("author");
        var favorited = params.get("favorited");
        var limit = params.get("limit");
        var offset = params.get("offset");

        var query = """
                SELECT article.*
                FROM article
                JOIN article_favorite
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

        if (limit != null) {
            try {
                int limitInt = Integer.parseInt(limit);
                filters.add(new Filter("LIMIT ?", limitInt));
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (offset != null) {
            try {
                int offsetInt = Integer.parseInt(offset);
                filters.add(new Filter("OFFSET ?", offsetInt));
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        return null;
    }
}
