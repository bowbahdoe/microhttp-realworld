package dev.mccue.microhttp.realworld;

import org.sqlite.SQLiteDataSource;

import java.nio.file.Path;

public final class DB {
    private DB() {}


    public static SQLiteDataSource getDB(Path path) throws Exception {
        SQLiteDataSource db = new SQLiteDataSource();
        db.setUrl("jdbc:sqlite:" + path);
        try (var conn = db.getConnection()) {
            try(var stmt = conn.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS "user" (
                        id integer primary key autoincrement,
                        password_hash text,
                        email text unique,
                        username text unique,
                        image text,
                        bio text
                    );
                    """)) {
                stmt.execute();
            }

            try(var stmt = conn.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS follow (
                        id integer primary key autoincrement,
                        follower_user_id integer references user(id),
                        following_user_id integer references user(id)
                    );
                    """)) {
                stmt.execute();
            }

            try(var stmt = conn.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS article (
                        id integer primary key autoincrement,
                        external_id text unique,
                        title text,
                        description text,
                        body text,
                        created_at datetime not null default current_timestamp,
                        updated_at datetime not null default current_timestamp,
                        user_id integer references user(id)
                    );
                    """
            )) {
                stmt.execute();
            }

            try(var stmt = conn.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS tag (
                        id integer primary key autoincrement,
                        name text unique
                    );
                    """)) {
                stmt.execute();
            }

            try(var stmt = conn.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS article_tag (
                        article_id integer references article(id),
                        tag_id integer references tag(id)
                    );
                    """)) {
                stmt.execute();
            }

            try(var stmt = conn.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS article_favorite (
                        article_id integer references article(id),
                        user_id integer references user(id)
                    );
                    """)) {
                stmt.execute();
            }

            try(var stmt = conn.prepareStatement(
                    """
                    CREATE TABLE IF NOT EXISTS comment (
                        id integer primary key autoincrement,
                        body text,
                        article_id integer references article(id),
                        user_id integer references user(id),
                        created_at datetime,
                        updated_at datetime
                    );
                    """)) {
                stmt.execute();
            }
        }
        return db;
    }
}
