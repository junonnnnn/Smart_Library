import java.util.List;
import java.util.Scanner;

// Menu-driven console UI that wires user input to the library and user store.
public class SmartLibraryApp {
    private final Scanner scanner;
    private final LibrarySystem librarySystem;
    private final UserStore userStore;

    public SmartLibraryApp(LibrarySystem librarySystem, UserStore userStore) {
        this.scanner = new Scanner(System.in);
        this.librarySystem = librarySystem;
        this.userStore = userStore;
    }

    public static void main(String[] args) {
        try {
            LibrarySystem librarySystem = new SmartLibrary();
            UserStore userStore = new UserStore();
            SmartLibraryApp app = new SmartLibraryApp(librarySystem, userStore);
            app.run();
        } catch (IllegalStateException exception) {
            System.out.println("Smart Library could not start: " + exception.getMessage());
        }
    }

    // Outer loop: login / register / exit until the user quits the app.
    public void run() {
        while (true) {
            printLoginMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    User user = login();
                    if (user != null && runLibraryMenu(user)) {
                        return;
                    }
                    break;
                case "2":
                    register();
                    break;
                case "3":
                    System.out.println("Thank you for using Smart Library. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Please choose 1, 2, or 3.");
                    break;
            }

            System.out.println();
        }
    }

    // Inner loop for a logged-in user; returns true if the user chose to exit
    // the whole app (vs. just logging out).
    private boolean runLibraryMenu(User user) {
        boolean loggedIn = true;

        while (loggedIn) {
            printLibraryMenu(user);
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addBook(user);
                    break;
                case "2":
                    searchBook();
                    break;
                case "3":
                    searchBookByTitle();
                    break;
                case "4":
                    searchBookByAuthor();
                    break;
                case "5":
                    borrowBook(user);
                    break;
                case "6":
                    returnBook(user);
                    break;
                case "7":
                    viewHistory(user);
                    break;
                case "8":
                    loggedIn = false;
                    System.out.println("Logged out.");
                    break;
                case "9":
                    System.out.println("Thank you for using Smart Library. Goodbye!");
                    return true;
                default:
                    System.out.println("Invalid option. Please choose 1, 2, 3, 4, 5, 6, 7, 8, or 9.");
                    break;
            }

            System.out.println();
        }

        return false;
    }

    private void printLoginMenu() {
        System.out.println("===== Smart Library Login =====");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
    }

    private void printLibraryMenu(User user) {
        System.out.println("===== Smart Library System =====");
        System.out.println("Logged in as: " + user.getId() + (user.isAdmin() ? " (Admin)" : " (Student)"));
        System.out.println("1. Add Book" + (user.isAdmin() ? "" : " (Admin Only)"));
        System.out.println("2. Search Book by ISBN");
        System.out.println("3. Search Book by Title");
        System.out.println("4. Search Book by Author");
        System.out.println("5. Borrow Book");
        System.out.println("6. Return Book");
        System.out.println("7. View History");
        System.out.println("8. Logout");
        System.out.println("9. Exit");
        System.out.print("Choose an option: ");
    }

    private User login() {
        String id = readRequiredText("Enter ID: ");
        if (id == null) {
            return null;
        }

        String password = readRequiredText("Enter Password: ");
        if (password == null) {
            return null;
        }

        User user = userStore.authenticate(id, password);
        if (user == null) {
            System.out.println("Invalid ID or password.");
            return null;
        }

        System.out.println("Login successful.");
        return user;
    }

    private void register() {
        String id = readRequiredText("Create ID: ");
        if (id == null) {
            return;
        }

        String password = readRequiredText("Create Password: ");
        if (password == null) {
            return;
        }

        boolean registered = userStore.registerStudent(id, password);
        if (registered) {
            System.out.println("Registration successful. You can now log in.");
        } else {
            System.out.println("Registration failed. ID may already exist, or ID/password may contain '|'.");
        }
    }

    private void addBook(User user) {
        // Adding books is an admin-only action.
        if (!user.isAdmin()) {
            System.out.println("Only admin users can add books.");
            return;
        }

        Integer isbn = readIsbn("Enter ISBN: ");
        if (isbn == null) {
            return;
        }

        String title = readRequiredText("Enter Title: ");
        if (title == null) {
            return;
        }

        String author = readRequiredText("Enter Author: ");
        if (author == null) {
            return;
        }

        boolean added = librarySystem.addBook(isbn, title, author);
        if (added) {
            System.out.println("Book added successfully.");
        } else {
            System.out.println("Book could not be added. Check for duplicate ISBNs, empty fields, or '|'.");
        }
    }

    private void searchBook() {
        Integer isbn = readIsbn("Enter ISBN to search: ");
        if (isbn == null) {
            return;
        }

        Book book = librarySystem.searchBook(isbn);
        if (book == null) {
            System.out.println("Book not found or currently borrowed.");
        } else {
            System.out.println("Book found:");
            System.out.println(book);
        }
    }

    private void searchBookByTitle() {
        String title = readRequiredText("Enter Title to search: ");
        if (title == null) {
            return;
        }

        List<Book> results = librarySystem.searchBooksByTitle(title);
        if (results.isEmpty()) {
            System.out.println("No books found with that title.");
            return;
        }

        System.out.println("Books found by title:");
        for (int index = 0; index < results.size(); index++) {
            System.out.println((index + 1) + ". " + results.get(index));
        }
    }

    private void searchBookByAuthor() {
        String author = readRequiredText("Enter Author to search: ");
        if (author == null) {
            return;
        }

        List<Book> results = librarySystem.searchBooksByAuthor(author);
        if (results.isEmpty()) {
            System.out.println("No books found with that author.");
            return;
        }

        System.out.println("Books found by author:");
        for (int index = 0; index < results.size(); index++) {
            System.out.println((index + 1) + ". " + results.get(index));
        }
    }

    private void borrowBook(User user) {
        Integer isbn = readIsbn("Enter ISBN to borrow: ");
        if (isbn == null) {
            return;
        }

        boolean borrowed = librarySystem.borrowBook(isbn, user.getId());
        if (borrowed) {
            System.out.println("Book borrowed successfully and added to history.");
        } else {
            System.out.println("Book not found. It may already be borrowed or not in the catalogue.");
        }
    }

    private void returnBook(User user) {
        Integer isbn = readIsbn("Enter ISBN to return: ");
        if (isbn == null) {
            return;
        }

        boolean returned = librarySystem.returnBook(isbn, user.getId(), user.isAdmin());
        if (returned) {
            System.out.println("Book returned successfully and restored to the catalogue.");
        } else {
            System.out.println("Return failed. The book may not exist, may not be borrowed, or may belong to another user.");
        }
    }

    private void viewHistory(User user) {
        List<Book> history = librarySystem.viewBorrowingHistory(user.getId());
        if (history.isEmpty()) {
            System.out.println("Borrowing history is empty.");
            return;
        }

        System.out.println("Borrowing History (Most Recent First):");
        for (int index = 0; index < history.size(); index++) {
            System.out.println((index + 1) + ". " + history.get(index));
        }
    }

    // Prompts for an ISBN; returns null (and prints why) if input is invalid.
    private Integer readIsbn(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        try {
            int isbn = Integer.parseInt(input);
            if (isbn <= 0) {
                System.out.println("ISBN must be a positive integer.");
                return null;
            }
            return isbn;
        } catch (NumberFormatException exception) {
            System.out.println("Invalid ISBN. Please enter a whole number that fits Java int range.");
            return null;
        }
    }

    // Prompts for non-empty text; returns null (and prints why) if blank.
    private String readRequiredText(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            System.out.println("This field cannot be empty.");
            return null;
        }

        return input;
    }
}
