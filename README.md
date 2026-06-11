# Smart Library Console System

This project implements the Smart Library assignment as a plain Java console application.

## Files

- `Book.java`: book data model with `int` ISBN, title, and author.
- `LibrarySystem.java`: ADT interface used by the console program.
- `BookBST.java`: custom ISBN-indexed binary search tree with recursive search.
- `BorrowHistoryStack.java`: stack wrapper using `java.util.Stack`, matching the PDF page 3 snippet style.
- `SmartLibrary.java`: admin logic, borrow/return logic, and `books.txt` persistence.
- `User.java` and `UserStore.java`: login/register logic and `users.txt` persistence.
- `SmartLibraryApp.java`: menu-driven console interface.
- `SmartLibraryTests.java`: no-framework test harness for the main requirements.

## Default Admin

The app creates a default admin user if one is missing:

```text
ID: admin
Password: admin123
```

Registered users are created as students. Admin users can add books and can return any borrowed book.

## Data Files

- `users.txt`: stores `id|password|role`.
- `books.txt`: stores `isbn|title|author|status|borrowedBy`.

## Run

```powershell
javac *.java
java SmartLibraryApp
```
