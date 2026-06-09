public class BookBST {
    private Node root;

    private static class Node {
        private Book book;
        private Node left;
        private Node right;

        private Node(Book book) {
            this.book = book;
        }
    }

    public boolean insert(Book book) {
        if (root == null) {
            root = new Node(book);
            return true;
        }

        return insertRecursive(root, book);
    }

    private boolean insertRecursive(Node current, Book book) {
        // Duplicate ISBNs are rejected.
        if (book.getIsbn() == current.book.getIsbn()) {
            return false;
        }

        // Smaller ISBNs go left, larger go right.
        if (book.getIsbn() < current.book.getIsbn()) {
            if (current.left == null) {
                current.left = new Node(book);
                return true;
            }
            return insertRecursive(current.left, book);
        }

        if (current.right == null) {
            current.right = new Node(book);
            return true;
        }
        return insertRecursive(current.right, book);
    }
     public Book search(int isbn) {
        return searchRecursive(root, isbn);
    }

    private Book searchRecursive(Node current, int isbn) {
        if (current == null) {
            return null;
        }

        if (isbn == current.book.getIsbn()) {
            return current.book;
        }

        if (isbn < current.book.getIsbn()) {
            return searchRecursive(current.left, isbn);
        }

        return searchRecursive(current.right, isbn);
    }

    public boolean delete(int isbn) {
        // If the book doesn't exist, we can't delete it.
        if (search(isbn) == null) {
            return false;
        }

        root = deleteRecursive(root, isbn);
        return true;
    }

    private Node deleteRecursive(Node current, int isbn) {
        if (current == null) {
            return null;
        }

        // Traverse left or right based on ISBN comparison.
        if (isbn < current.book.getIsbn()) {
            current.left = deleteRecursive(current.left, isbn);
            return current;
        }

        if (isbn > current.book.getIsbn()) {
            current.right = deleteRecursive(current.right, isbn);
            return current;
        }

        // Match found. One child (or none): replace node with that child.
        if (current.left == null) {
            return current.right;
        }

        if (current.right == null) {
            return current.left;
        }

        // Two children: replace with the in-order successor, then remove it.
        Node successor = findSmallestNode(current.right);
        current.book = successor.book;
        current.right = deleteRecursive(current.right, successor.book.getIsbn());
        return current;
    }

    // Leftmost node holds the smallest ISBN in this subtree.
    private Node findSmallestNode(Node current) {
        return current.left == null ? current : findSmallestNode(current.left);
    }

}
