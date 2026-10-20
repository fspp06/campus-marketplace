package edu.campusmarketplace.model;

import java.util.UUID;

/** Common account details shared by every type of marketplace user. */
public abstract class User {
    private final UUID id = UUID.randomUUID();
    private final String name;
    private final String email;
    private final String password;

    protected User(String name, String email, String password) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required.");
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Enter a valid email.");
        if (password == null || password.length() < 4) throw new IllegalArgumentException("Password must have at least 4 characters.");
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.password = password;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public boolean passwordMatches(String candidate) { return password.equals(candidate); }
}
