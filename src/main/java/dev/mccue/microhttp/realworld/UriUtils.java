package dev.mccue.microhttp.realworld;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public final class UriUtils {
    private UriUtils() {}

    public static Map<String, String> queryParams(URI uri) {
        return Arrays.stream(uri.getQuery().split("&"))
                .map(query -> query.split("="))
                .filter(query -> query.length == 2)
                .collect(Collectors.toUnmodifiableMap(
                        query -> URLDecoder.decode(query[0], StandardCharsets.UTF_8),
                        query -> URLDecoder.decode(query[1], StandardCharsets.UTF_8)
                ));
    }
}
