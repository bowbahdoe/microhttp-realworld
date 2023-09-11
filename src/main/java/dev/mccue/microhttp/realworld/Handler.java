package dev.mccue.microhttp.realworld;

import org.jspecify.annotations.Nullable;
import org.microhttp.Request;

public interface Handler {
    @Nullable IntoResponse handle(Request request) throws Exception;
}
