import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Holds user accounts and handles login, registration, and users.txt persistence.
public class UserStore {
    private static final String DEFAULT_ADMIN_ID = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String DELIMITER = "\\|";
    private static final String FILE_DELIMITER = "|";

    private final Path usersPath;
    private final Map<String, User> users;

    public UserStore() {
        this(Path.of("users.txt"));
    }

    public UserStore(Path usersPath) {
        this.usersPath = usersPath;
        this.users = new LinkedHashMap<>();
        loadUsers();
        ensureDefaultAdmin();
    }

    // Returns the matching user on correct credentials, or null otherwise.
    public User authenticate(String id, String password) {
        if (isBlank(id) || password == null) {
            return null;
        }

        User user = users.get(id.trim());
        if (user == null || !user.passwordMatches(password.trim())) {
            return null;
        }

        return user;
    }

    // Creates a new student account; fails if the ID is taken or invalid.
    public boolean registerStudent(String id, String password) {
        if (isInvalidCredential(id) || isInvalidCredential(password)) {
            return false;
        }

        String trimmedId = id.trim();
        if (users.containsKey(trimmedId)) {
            return false;
        }

        users.put(trimmedId, new User(trimmedId, password.trim(), User.Role.STUDENT));
        saveUsers();
        return true;
    }

    private void loadUsers() {
        try {
            if (Files.notExists(usersPath)) {
                Files.createFile(usersPath);
                return;
            }

            List<String> lines = Files.readAllLines(usersPath);
            for (String line : lines) {
                loadUserLine(line);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load users from " + usersPath, exception);
        }
    }

    private void loadUserLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return;
        }

        String[] parts = trimmed.split(DELIMITER, -1);
        if (parts.length < 3 || isInvalidCredential(parts[0]) || isInvalidCredential(parts[1])) {
            return;
        }

        try {
            User.Role role = User.Role.valueOf(parts[2].trim().toUpperCase());
            users.put(parts[0].trim(), new User(parts[0].trim(), parts[1].trim(), role));
        } catch (IllegalArgumentException ignored) {
            // Skip saved users with invalid roles.
        }
    }

    // Guarantees a built-in admin account exists so the app is always usable.
    private void ensureDefaultAdmin() {
        if (!users.containsKey(DEFAULT_ADMIN_ID)) {
            users.put(DEFAULT_ADMIN_ID, new User(DEFAULT_ADMIN_ID, DEFAULT_ADMIN_PASSWORD, User.Role.ADMIN));
            saveUsers();
        }
    }

    private void saveUsers() {
        try {
            Files.write(usersPath, users.values().stream().map(User::toFileLine).toList());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save users to " + usersPath, exception);
        }
    }

    private boolean isInvalidCredential(String value) {
        return isBlank(value) || value.contains(FILE_DELIMITER);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
