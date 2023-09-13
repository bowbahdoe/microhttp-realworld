import org.jspecify.annotations.NullMarked;

@NullMarked
module dev.mccue.microhttp.realworld {
    requires org.microhttp;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires dev.mccue.json;
    requires dev.mccue.microhttp.systemlogger;
    requires dev.mccue.reasonphrase;
    requires org.jspecify;
    requires com.auth0.jwt;
    requires io.github.cdimascio.dotenv.java;

    requires org.slf4j.jdk.platform.logging;
    requires ch.qos.logback.classic;

    requires static io.soabase.recordbuilder.core;
    requires static java.compiler;

    exports dev.mccue.microhttp.realworld
            to dev.mccue.microhttp.realworld.test;
    exports dev.mccue.microhttp.realworld.handlers
            to dev.mccue.microhttp.realworld.test;
    exports dev.mccue.microhttp.realworld.domain
            to dev.mccue.microhttp.realworld.test;
}