package dev.mccue.microhttp.realworld;

import io.github.cdimascio.dotenv.Dotenv;

public final class Env {
    private Env() {}

    private static final Dotenv DOTENV = Dotenv.load();

    public static final String JWT_SECRET = DOTENV.get("JWT_SECRET");
    public static final int PORT = Integer.parseInt(DOTENV.get("PORT", "5555"));
    public static final String ENVIRONMENT = DOTENV.get("ENVIRONMENT");
}
