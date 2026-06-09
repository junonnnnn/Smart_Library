public class User {
    // Admins can add books and return any book; students cannot.
    public enum Role {
        ADMIN,
        STUDENT
    }

    private final String id;
    private final String password;
    private final Role role;

    public User(String id, String password, Role role) {
        this.id = id;
        this.password = password;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public boolean passwordMatches(String password) {
        return this.password.equals(password);
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    // Serializes the user to one "id|password|role" line for users.txt.
    public String toFileLine() {
        return id + "|" + password + "|" + role;
    }
}