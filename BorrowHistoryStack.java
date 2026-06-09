import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class BorrowHistoryStack {
    private final Stack<Book> stack = new Stack<>();

    public void push(Book book) {
        stack.push(book);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public List<Book> toListLifo() {
        List<Book> books = new ArrayList<>();

        for (int index = stack.size() - 1; index >= 0; index--) {
            books.add(stack.get(index));
        }

        return books;
    }
}
