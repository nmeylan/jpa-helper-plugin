package ch.nmeylan.plugin.jpa.generator.model;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;

import java.util.ArrayList;
import java.util.List;

public class EntityField {
    private final String name;
    private final PsiType type;
    private final PsiField psiField;
    private final PsiClass ownerClass;
    private List<EntityField> childrenFields;
    private final EntityField parentField;
    private boolean isCollection;
    private boolean isRelation;
    private boolean disabledToAvoidLoop;

    public EntityField(String name, PsiField psiField, PsiType type, PsiClass ownerClass, EntityField parentField) {
        this.name = name;
        this.psiField = psiField;
        this.type = type;
        this.ownerClass = ownerClass;
        this.parentField = parentField;
        isCollection = false;
        isRelation = false;
        disabledToAvoidLoop = false;
    }

    public String getName() {
        return name;
    }

    public PsiClass getOwnerClass() {
        return ownerClass;
    }

    public PsiType getType() {
        return type;
    }

    public PsiField getPsiField() {
        return psiField;
    }

    public boolean isCollection() {
        return isCollection;
    }

    public boolean isRelation() {
        return isRelation;
    }

    public EntityField setCollection(boolean collection) {
        isCollection = collection;
        return this;
    }

    public List<EntityField> getMutRelationFields() {
        if (childrenFields == null) {
            childrenFields = new ArrayList<>();
        }
        return childrenFields;
    }

    public List<EntityField> getChildrenFields() {
        return childrenFields;
    }

    public EntityField getParentField() {
        return parentField;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (type != null) {
            sb.append(type.toString()).append(" ");
        }
        sb.append(name);
        return sb.toString();
    }

    public void setRelation(boolean relation) {
        isRelation = relation;
    }

    public void setDisabledToAvoidLoop() {
        disabledToAvoidLoop = true;
    }
    public boolean isDisabledToAvoidLoop() {
        return disabledToAvoidLoop;
    }
}
