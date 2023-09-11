package dev.mccue.microhttp.realworld.domain;

import java.util.Objects;
import java.util.Optional;

public record User(
        long id,
        String email,
        String username,
        Optional<String> bio,
        Optional<String> image,
        PasswordHash passwordHash
) {
    public User {
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(bio, "bio must not be null");
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(passwordHash, "passwordHash must not be null");
    }

    public boolean isCorrectPassword(String password) {
        return this.passwordHash.isCorrectPassword(password);
    }
}
