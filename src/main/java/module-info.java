import org.jspecify.annotations.NullMarked;

@NullMarked
module dev.mccue.microhttp.realworld {
    requires org.microhttp;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires dev.mccue.json;
    requires dev.mccue.microhttp.systemlogger;
    requires org.jspecify;
    requires com.auth0.jwt;

    requires org.slf4j.jdk.platform.logging;
    requires ch.qos.logback.classic;

    requires static io.soabase.recordbuilder.core;
    requires static java.compiler;
}