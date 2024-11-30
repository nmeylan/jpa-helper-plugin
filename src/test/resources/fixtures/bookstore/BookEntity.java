package ch.nmeylan.blog.example.bookstore;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class BookEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String isbn;

    private Double price;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private AuthorEntity author;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "details_id", referencedColumnName = "id")
    private BookDetailsEntity details;

    @ManyToMany
    @JoinTable(
            name = "book_category",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<CategoryEntity> categories = new HashSet<>();

    public BookEntity() {}

    // Getters and setters
}