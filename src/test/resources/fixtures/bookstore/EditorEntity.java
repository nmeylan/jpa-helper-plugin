package ch.nmeylan.blog.example.bookstore;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class EditorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String phoneNumber;

    // Address
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    @OneToMany(mappedBy = "editor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AuthorEntity> authors = new HashSet<>();

    public EditorEntity() {}

    // Getters and setters
}