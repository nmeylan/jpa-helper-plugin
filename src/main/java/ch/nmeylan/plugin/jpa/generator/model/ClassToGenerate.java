package ch.nmeylan.plugin.jpa.generator.model;

import com.intellij.psi.PsiClass;
import com.intellij.psi.util.PsiTypesUtil;

import java.util.LinkedHashSet;
import java.util.Optional;

public class ClassToGenerate {
    private final String name;
    private String fieldNameForInParentRelation;
    private PsiClass existingClass;
    private final LinkedHashSet<EntityField> fields;
    private ClassToGenerate parentRelation;
    private String joinNameForParent;
    private LinkedHashSet<ClassToGenerate> childrenRelation;


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

    public String getFieldNameForInParentRelation() {
        return fieldNameForInParentRelation;
    }

    public ClassToGenerate setFieldNameForInParentRelation(String fieldNameForInParentRelation) {
        this.fieldNameForInParentRelation = fieldNameForInParentRelation;
        return this;
    }

    public LinkedHashSet<EntityField> getFields() {
        return fields;
    }

    public Optional<EntityField> getFieldOfType(PsiClass psiClass) {
        // TODO handle collection
        return getFields().stream().filter(f -> PsiTypesUtil.getPsiClass(f.getType()).equals(psiClass)).findFirst();
    }

    public ClassToGenerate getParentRelation() {
        return parentRelation;
    }

    public LinkedHashSet<ClassToGenerate> getChildrenRelation() {
        return childrenRelation;
    }

    public PsiClass getExistingClass() {
        return existingClass;
    }

    public String getJoinNameForParent() {
        return joinNameForParent;
    }

    public void addParentRelation(ClassToGenerate parentRelation) {
        this.parentRelation = parentRelation;
        String nameWithoutEntity = name.substring(0, name.indexOf("Entity"));
        this.joinNameForParent = Character.toLowerCase(nameWithoutEntity.charAt(0)) + nameWithoutEntity.substring(1)+ "Join";
    }

    public boolean addChildRelation(ClassToGenerate childRelation) {
        if (childrenRelation == null) {
            childrenRelation = new LinkedHashSet<>();
        }
       return childrenRelation.add(childRelation);
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
