package ch.nmeylan.blog.example.bookstore;

import jakarta.persistence.*;

@Entity
public class BookDetailsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numberOfPages;

    private String language;

    private Double weight;

    private String dimensions;

    @OneToOne(mappedBy = "details")
    private BookEntity book;

    public BookDetailsEntity() {}

}