import java.util.List;

public interface LibrarySystem {
    boolean addBook(int isbn, String title, String author);

    // Exact ISBN lookup; the others are partial, case-insensitive matches.
    Book searchBook(int isbn);

    List<Book> searchBooksByTitle(String title);

    List<Book> searchBooksByAuthor(String author);

    boolean borrowBook(int isbn, String userId);

    // admin = true lets the caller return a book borrowed by anyone.
    boolean returnBook(int isbn, String userId, boolean admin);

    List<Book> viewBorrowingHistory(String userId);
}