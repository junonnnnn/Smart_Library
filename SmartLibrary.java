import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Core library logic: catalogue, borrow/return, and books.txt persistence.
public class SmartLibrary implements LibrarySystem {
    private static final String DELIMITER = "\\|";
    private static final String FILE_DELIMITER = "|";
    private static final String AVAILABLE = "AVAILABLE";
    private static final String BORROWED = "BORROWED";

    // catalogue holds only available books; bookRecords tracks every book + status.
    private final BookBST catalogue;
    private final Map<Integer, BookRecord> bookRecords;
    private final Map<String, BorrowHistoryStack> borrowingHistories;
    private final Path booksPath;

    public SmartLibrary() {
        this(Path.of("books.txt"));
    }

    public SmartLibrary(Path booksPath) {
        this.catalogue = new BookBST();
        this.bookRecords = new LinkedHashMap<>();
        this.borrowingHistories = new HashMap<>();
        this.booksPath = booksPath;
        loadBooks();
    }

    @Override
    public boolean addBook(int isbn, String title, String author) {
        if (isbn <= 0 || isBlank(title) || isBlank(author) || containsDelimiter(title) || containsDelimiter(author)) {
            return false;
        }

        if (bookRecords.containsKey(isbn)) {
            return false;
        }

        Book book = new Book(isbn, title.trim(), author.trim());
        BookRecord record = BookRecord.available(book);
        bookRecords.put(isbn, record);
        catalogue.insert(book);
        saveBooks();
        return true;
    }

    @Override
    public Book searchBook(int isbn) {
        return catalogue.search(isbn);
    }

    @Override
    public List<Book> searchBooksByTitle(String title) {
        if (isBlank(title)) {
            return new ArrayList<>();
        }

        String searchTerm = title.trim().toLowerCase();
        List<Book> results = new ArrayList<>();

        for (BookRecord record : bookRecords.values()) {
            if (record.book.getTitle().toLowerCase().contains(searchTerm)) {
                results.add(record.book);
            }
        }

        return results;
    }

    @Override
    public List<Book> searchBooksByAuthor(String author) {
        if (isBlank(author)) {
            return new ArrayList<>();
        }

        String searchTerm = author.trim().toLowerCase();
        List<Book> results = new ArrayList<>();

        for (BookRecord record : bookRecords.values()) {
            if (record.book.getAuthor().toLowerCase().contains(searchTerm)) {
                results.add(record.book);
            }
        }

        return results;
    }

    @Override
    public boolean borrowBook(int isbn, String userId) {
        if (isBlank(userId)) {
            return false;
        }

        Book book = catalogue.search(isbn);
        BookRecord record = bookRecords.get(isbn);
        if (book == null || record == null || !record.available) {
            return false;
        }

        // Mark borrowed: drop from the catalogue and log it in the user's history.
        catalogue.delete(isbn);
        record.available = false;
        record.borrowedBy = userId.trim();
        historyFor(userId).push(book);
        saveBooks();
        return true;
    }

    @Override
    public boolean returnBook(int isbn, String userId, boolean admin) {
        if (isBlank(userId)) {
            return false;
        }

        BookRecord record = bookRecords.get(isbn);
        if (record == null || record.available) {
            return false;
        }

        // Students may only return their own book; admins may return any.
        if (!admin && !userId.trim().equals(record.borrowedBy)) {
            return false;
        }

        // Mark available again and put it back into the catalogue.
        record.available = true;
        record.borrowedBy = "";
        catalogue.insert(record.book);
        saveBooks();
        return true;
    }

    @Override
    public List<Book> viewBorrowingHistory(String userId) {
        if (isBlank(userId)) {
            return new ArrayList<>();
        }

        return historyFor(userId).toListLifo();
    }

    // Returns the user's history, creating an empty one on first use.
    private BorrowHistoryStack historyFor(String userId) {
        return borrowingHistories.computeIfAbsent(userId.trim(), key -> new BorrowHistoryStack());
    }

    // Reads books.txt on startup, creating the file if it is missing.
    private void loadBooks() {
        try {
            if (Files.notExists(booksPath)) {
                Files.createFile(booksPath);
                return;
            }

            List<String> lines = Files.readAllLines(booksPath);
            for (String line : lines) {
                loadBookLine(line);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load books from " + booksPath, exception);
        }
    }

    // Parses one "isbn|title|author|status|borrowedBy" line into a record.
    private void loadBookLine(String line) {
        // Skip blank lines and comments (e.g. the file's format header).
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return;
        }

        String[] parts = trimmed.split(DELIMITER, -1);
        if (parts.length < 5) {
            return;
        }

        try {
            int isbn = Integer.parseInt(parts[0].trim());
            String title = parts[1].trim();
            String author = parts[2].trim();
            String status = parts[3].trim();
            String borrowedBy = parts[4].trim();

            if (isbn <= 0 || isBlank(title) || isBlank(author) || bookRecords.containsKey(isbn)) {
                return;
            }

            Book book = new Book(isbn, title, author);
            BookRecord record = BORROWED.equalsIgnoreCase(status)
                    ? BookRecord.borrowed(book, borrowedBy)
                    : BookRecord.available(book);

            // Only available books belong in the searchable catalogue.
            bookRecords.put(isbn, record);
            if (record.available) {
                catalogue.insert(book);
            }
        } catch (NumberFormatException ignored) {
            // Invalid saved rows are skipped so one bad line does not stop the app.
        }
    }

    // Rewrites books.txt from the current records (called after every change).
    private void saveBooks() {
        List<String> lines = new ArrayList<>();
        lines.add("# Format: isbn|title|author|status|borrowedBy");

        for (BookRecord record : bookRecords.values()) {
            lines.add(record.book.getIsbn()
                    + FILE_DELIMITER + record.book.getTitle()
                    + FILE_DELIMITER + record.book.getAuthor()
                    + FILE_DELIMITER + (record.available ? AVAILABLE : BORROWED)
                    + FILE_DELIMITER + record.borrowedBy);
        }

        try {
            Files.write(booksPath, lines);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save books to " + booksPath, exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean containsDelimiter(String value) {
        return value != null && value.contains(FILE_DELIMITER);
    }

    // A book plus its current status (available, or borrowed by whom).
    private static class BookRecord {
        private final Book book;
        private boolean available;
        private String borrowedBy;

        private BookRecord(Book book, boolean available, String borrowedBy) {
            this.book = book;
            this.available = available;
            this.borrowedBy = borrowedBy;
        }

        private static BookRecord available(Book book) {
            return new BookRecord(book, true, "");
        }

        private static BookRecord borrowed(Book book, String borrowedBy) {
            return new BookRecord(book, false, borrowedBy == null ? "" : borrowedBy);
        }
    }
}
