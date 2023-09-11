package dev.mccue.microhttp.realworld.service;

import org.sqlite.SQLiteDataSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TagService {
    private final SQLiteDataSource db;

    public TagService(SQLiteDataSource db) {
        this.db = db;
    }

    public record Tag(
            long id,
            String name
    ) {}

    public List<Tag> all() {
        try (var conn = db.getConnection();
             var stmt = conn.prepareStatement("""
                     SELECT id, name
                     FROM tag
                     """)) {
            var tags = new ArrayList<Tag>();
            var rs = stmt.executeQuery();
            while (rs.next()) {
                tags.add(new Tag(
                        rs.getLong(1),
                        rs.getString(2)
                ));
            }
            return Collections.unmodifiableList(tags);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}