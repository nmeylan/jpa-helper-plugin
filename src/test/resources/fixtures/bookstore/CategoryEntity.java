package ch.nmeylan.blog.example.bookstore;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @ManyToMany(mappedBy = "categories")
    private Set<BookEntity> books = new HashSet<>();

    public CategoryEntity() {}

    // Getters and setters
}