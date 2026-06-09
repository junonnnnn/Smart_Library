public class Book {
    private final int isbn;
    private final String title;
    private final String author;

    public Book(int isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
    }

    public int getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return "ISBN: " + isbn + " | Title: " + title + " | Author: " + author;
    }
}