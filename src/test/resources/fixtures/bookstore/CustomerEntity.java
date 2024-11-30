package ch.nmeylan.blog.example.bookstore;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String address;

    private String phoneNumber;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderEntity> orders = new HashSet<>();

    public CustomerEntity() {}

    // Getters and setters
}