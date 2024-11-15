import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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

    @OneToMany
    @JoinColumn(nullable = false, name = "id")
    private List<String> items;

    private Instant lastUpdatedAt;
}
