package ch.nmeylan.plugin.jpa.generator;

import ch.nmeylan.plugin.jpa.generator.model.ClassToGenerate;
import ch.nmeylan.plugin.jpa.generator.model.EntityField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectionModelGenerator {

    public static String generateProjection(String projectionSuffix, EntityField rootField, List<EntityField> selectedFields) {
        classesToGenerate(projectionSuffix, rootField, selectedFields);
        for (EntityField field : selectedFields) {
            String out = field.getOwnerClass().getName() + "." + field.getName();
            EntityField parent = field.getParentRelation();
            EntityField root = parent;
            while (parent != null) {
                out = parent.getOwnerClass().getName() + "." + out;
                root = parent;
                parent = parent.getParentRelation();
            }
            System.out.println(out);
            System.out.println("root: " + root.getOwnerClass().getName());
        }
        return "";
    }

    public static Map<String, ClassToGenerate> classesToGenerate(String projectionSuffix, EntityField rootField, List<EntityField> selectedFields) {
        Map<String, ClassToGenerate> classesToGenerate = new HashMap<>();

        Helper.iterateEntityFields(rootField.getRelationFields(), (field, _depth) -> {
            if (!selectedFields.contains(field)) {
                return;
            }
            ClassToGenerate classToGenerate = null;
            classToGenerate = getClassToGenerate(projectionSuffix, field, classesToGenerate);
            classToGenerate.addField(field);

            EntityField parent = field.getParentRelation();
            while (parent != null) {
                ClassToGenerate parentClass = getClassToGenerate(projectionSuffix, parent, classesToGenerate);
                classToGenerate.addParentRelation(parentClass);
                parentClass.addChildRelation(classToGenerate);
                parentClass.addField(parent);
                parent = parent.getParentRelation();
                classToGenerate = parentClass;
            }
        });
        return classesToGenerate;
    }

    private static ClassToGenerate getClassToGenerate(String projectionSuffix, EntityField field, Map<String, ClassToGenerate> classesToGenerate) {
        String key = field.getOwnerClass().getQualifiedName();
        ClassToGenerate classToGenerate;
        if (!classesToGenerate.containsKey(key)) {
            classToGenerate = new ClassToGenerate(field.getOwnerClass().getName() + projectionSuffix);
            classesToGenerate.put(key, classToGenerate);
        } else {
            classToGenerate = classesToGenerate.get(key);
        }
        return classToGenerate;
    }
}
