import org.jspecify.annotations.NullMarked;

@NullMarked
module dev.mccue.microhttp.realworld {
    requires org.microhttp;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires dev.mccue.json;
    requires dev.mccue.microhttp.systemlogger;
    requires dev.mccue.reasonphrase;
    requires dev.mccue.microhttp.handler;
    requires org.jspecify;
    requires com.auth0.jwt;
    requires io.github.cdimascio.dotenv.java;
    requires dev.mccue.feather;

    requires org.slf4j.jdk.platform.logging;
    requires ch.qos.logback.classic;
    requires slugify;

    exports dev.mccue.microhttp.realworld;

    exports dev.mccue.microhttp.realworld.handlers
            to dev.mccue.microhttp.realworld.test, dev.mccue.feather;
}