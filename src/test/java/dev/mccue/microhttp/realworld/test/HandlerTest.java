package dev.mccue.microhttp.realworld.test;

import dev.mccue.microhttp.realworld.DB;
import org.junit.jupiter.api.BeforeEach;
import org.sqlite.SQLiteDataSource;

import java.nio.file.Files;

public abstract class HandlerTest {
    public SQLiteDataSource db;
    public RootHandler rootHandler;

    @BeforeEach
    public void makeDB() throws Exception {
        this.db = DB.getDB(Files.createTempFile(null, null));
        this.rootHandler = new RootHandler(db);
    }
}
