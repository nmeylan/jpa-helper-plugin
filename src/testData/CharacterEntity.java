import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "characters")
public class CharacterEntity {
    @Id
    private String id;

    @OneToOne
    private Inventory inventory;
}
