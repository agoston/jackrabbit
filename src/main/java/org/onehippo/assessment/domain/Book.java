package org.onehippo.assessment.domain;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;
import org.onehippo.assessment.Refreshable;

@Node
public class Book implements Refreshable {
    @Field(path=true) String path;

    @Field String author;
    @Field String isbn;
    @Field String title;

    public Book() {}

    public Book(String path, String author, String isbn, String title) {
        this.path = path;
        this.author = author;
        this.isbn = isbn;
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Book book = (Book) o;

        if (!author.equals(book.author)) return false;
        if (!isbn.equals(book.isbn)) return false;
        if (!title.equals(book.title)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public void refresh(Object fresh) {
        Book o = (Book)fresh;
        this.author = o.author;
        this.isbn = o.isbn;
        this.title = o.title;
    }
}
