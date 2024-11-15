package ch.nmeylan.plugin.jpa.generator.model;

import com.intellij.psi.PsiType;

import java.util.List;

public class EntityField {
    private final String name;
    private final PsiType type;
    private boolean isRelation;
    private List<EntityField> relationFields;

    public EntityField(String name, PsiType type) {
        this.name = name;
        this.type = type;
        this.isRelation = false;
    }

    public String getName() {
        return name;
    }

    public PsiType getType() {
        return type;
    }

    public boolean isRelation() {
        return isRelation;
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
