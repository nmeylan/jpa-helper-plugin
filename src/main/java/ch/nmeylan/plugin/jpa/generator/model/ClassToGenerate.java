package ch.nmeylan.plugin.jpa.generator.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class ClassToGenerate {
    private final String name;
    private final LinkedHashSet<EntityField> fields;
    private ClassToGenerate parentRelation;
    private List<ClassToGenerate> childrenRelation;


    public ClassToGenerate(String name) {
        this.name = name;
        this.fields = new LinkedHashSet<>();
    }

    public void addField(EntityField field) {
        fields.add(field);
    }

    public String getName() {
        return name;
    }

    public LinkedHashSet<EntityField> getFields() {
        return fields;
    }

    public ClassToGenerate getParentRelation() {
        return parentRelation;
    }

    public List<ClassToGenerate> getChildrenRelation() {
        return childrenRelation;
    }

    public void addParentRelation(ClassToGenerate parentRelation) {
        this.parentRelation = parentRelation;
    }

    public void addChildRelation(ClassToGenerate childRelation) {
        if (childrenRelation == null) {
            childrenRelation = new ArrayList<>();
        }
        childrenRelation.add(childRelation);
    }
}
