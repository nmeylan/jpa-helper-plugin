package ch.nmeylan.blog.example.bookstore;


import jakarta.persistence.*;

@Entity
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    private Double price;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    public OrderItemEntity() {}

    // Getters and setters
}