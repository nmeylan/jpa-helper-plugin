package ch.nmeylan.blog.example.bookstore;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
public class AuthorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Personal Information
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String nationality;
    private String biography;

    // Contact Information
    private String email;
    private String phoneNumber;
    private String website;

    // Address
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    // Publishing Details
    private Integer numberOfBooksPublished;
    private LocalDate firstPublicationDate;
    private String primaryGenre;

    @ManyToOne
    @JoinColumn(name = "editor_id")
    private EditorEntity editor;

    // Additional Fields
    private Boolean isActive; // Indicates if the author is still active
    private LocalDate lastUpdated;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookEntity> books = new HashSet<>();


    public AuthorEntity() {}

}
