package ch.nmeylan.plugin.jpa.generator.model;

import com.intellij.psi.PsiClass;
import com.intellij.psi.util.PsiTypesUtil;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Optional;

public class ClassToGenerate {
    private final String name;
    private final String importableName;
    private final String packageName;
    private String fieldNameForInParentRelation;
    private PsiClass existingClass;
    private final LinkedHashSet<EntityField> fields;
    private ClassToGenerate parentRelation;
    private String joinVariableName;
    private HashMap<String, ClassToGenerate> childrenRelation;


    public ClassToGenerate(String name, PsiClass existingClass, boolean isInner, String packageName) {
        this.name = name;
        this.existingClass = existingClass;
        this.packageName = packageName;
        this.fields = new LinkedHashSet<>();
        if (isInner) {
            importableName = existingClass.getName() + "." + name;
        } else {
            importableName = name;
        }
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

    public HashMap<String, ClassToGenerate> getChildrenRelation() {
        return childrenRelation;
    }

    public PsiClass getExistingClass() {
        return existingClass;
    }

    public String getJoinNameForParent() {
        return fieldNameForInParentRelation + "Join";
    }

    public void addParentRelation(ClassToGenerate parentRelation) {
        this.parentRelation = parentRelation;
    }

    public boolean addChildRelation(String fieldName, ClassToGenerate childRelation) {
        if (childrenRelation == null) {
            childrenRelation = new HashMap<>();
        }
       return childrenRelation.put(fieldName, childRelation) == null;
    }

    public String getImportableName() {
        return importableName;
    }

    public String getJoinVariableName() {
        return joinVariableName;
    }

    public ClassToGenerate setJoinVariableName(String joinVariableName) {
        this.joinVariableName = joinVariableName;
        return this;
    }

    public String getPackageName() {
        return packageName;
    }
}
