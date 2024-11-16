package fixtures;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "inventories")
public class InventoryEntity {
    @Id
    private String id;

    @OneToOne
    @JoinColumn(nullable = false, name = "char_id")
    private  fixtures.CharacterEntity characterEntity;

    @jakarta.persistence.OneToMany
    @JoinColumn(nullable = false, name = "id")
    private java.util.List<ItemEntity> items;

    private Instant lastUpdatedAt;

    @Transient
    private long internalId;
}
