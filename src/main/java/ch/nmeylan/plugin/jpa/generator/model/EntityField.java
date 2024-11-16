package ch.nmeylan.plugin.jpa.generator.model;

import com.intellij.psi.PsiType;

import java.util.ArrayList;
import java.util.List;

public class EntityField {
    private final String name;
    private final PsiType type;
    private List<EntityField> relationFields;
    private boolean isCollection;

    public EntityField(String name, PsiType type) {
        this.name = name;
        this.type = type;
        isCollection = false;
    }

    public String getName() {
        return name;
    }

    public PsiType getType() {
        return type;
    }

    public boolean isCollection() {
        return isCollection;
    }

    public EntityField setCollection(boolean collection) {
        isCollection = collection;
        return this;
    }

    public List<EntityField> getMutRelationFields() {
        if (relationFields == null) {
            relationFields = new ArrayList<>();
        }
        return relationFields;
    }

    public List<EntityField> getRelationFields() {
        return relationFields;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.toString()).append(" ").append(name);
        return sb.toString();
    }

}
