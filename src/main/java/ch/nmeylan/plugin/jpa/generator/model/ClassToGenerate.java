package ch.nmeylan.plugin.jpa.generator.model;

import com.intellij.psi.PsiClass;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class ClassToGenerate {
    private final String name;
    private PsiClass existingClass;
    private final LinkedHashSet<EntityField> fields;
    private ClassToGenerate parentRelation;
    private List<ClassToGenerate> childrenRelation;


    public ClassToGenerate(String name, PsiClass existingClass) {
        this.name = name;
        this.existingClass = existingClass;
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

    public PsiClass getExistingClass() {
        return existingClass;
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

//    public String generateClass() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("public ").append(name).append("(");
//        Iterator<EntityField> iterator = fields.iterator();
//        while (iterator.hasNext()) {
//            EntityField field = iterator.next();
//            sb.append(field.getType().getPresentableText()).append(" ").append(field.getName());
//            if (iterator.hasNext()) {
//                sb.append(", ");
//            }
//        }
//        sb.append(")");
//    }
}
