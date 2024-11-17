package ch.nmeylan.plugin.jpa.generator;

import ch.nmeylan.plugin.jpa.generator.model.EntityField;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTypesUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class Helper {

    private static Map<String, PsiClass> classes = new HashMap<String, PsiClass>();
    private static Set<String> RELATIONS_ANNOTATIONS = Set.of("OneToMany", "OneToOne", "ManyToMany", "ManyToOne");
    private static Set<String> JPA_PACKAGES = Set.of("javax.persistence", "jakarta.persistence", "");

    public static EntityField entityFields(PsiClass psiClass) {
        List<String> visitedClasses = new ArrayList<>();
        EntityField root = new EntityField(null, null, psiClass, null);
        collectEntityFields(psiClass, root.getMutRelationFields(), visitedClasses, null);
        return root;
    }

    public static void iterateEntityFields(List<EntityField> fields, BiConsumer<EntityField, Integer> callback) {
        iterateEntityFields(fields, callback, 0);
    }

    private static void iterateEntityFields(List<EntityField> fields, BiConsumer<EntityField, Integer> callback, int depth) {
        fields.sort((o1, o2) -> {
            if (o1.getRelationFields() == null && o2.getRelationFields() != null) {
                return -1;
            }
            if (o1.getRelationFields() != null && o2.getRelationFields() == null) {
                return 1;
            }
            return 0;
        });
        for (EntityField field : fields) {
            callback.accept(field, depth);
            if (field.getRelationFields() != null) {
                iterateEntityFields(field.getRelationFields(), callback, depth + 1);
            }
        }
    }

    private static void collectEntityFields(PsiClass psiClass, List<EntityField> fields, List<String> visitedClasses, EntityField parentRelation) {
        if (psiClass == null || visitedClasses.contains(psiClass.getQualifiedName())) {
            return;
        }
        visitedClasses.add(psiClass.getQualifiedName());
        for (PsiField field : psiClass.getFields()) {
            if (!isTransientField(field)) {
                PsiType fieldType = field.getType();
                EntityField entityField = new EntityField(field.getName(), fieldType, psiClass, parentRelation);
                fields.add(entityField);
                boolean isCollection = isCollection(fieldType);
                entityField.setCollection(isCollection);
                if (hasRelation(field)) {
                    if (isCollection) {
                        PsiClassType classType = (PsiClassType) fieldType;
                        PsiType genericType = null;
                        if (classType.getParameters().length > 1) {
                            genericType = classType.getParameters()[1];
                        } else if (classType.getParameters().length > 0) {
                            genericType = classType.getParameters()[0];
                        }
                        if (genericType != null) {
                            collectEntityFields(PsiTypesUtil.getPsiClass(genericType), entityField.getMutRelationFields(), visitedClasses, entityField);
                        }
                    } else {
                        collectEntityFields(PsiTypesUtil.getPsiClass(fieldType), entityField.getMutRelationFields(), visitedClasses, entityField);
                    }
                }
            }
        }
    }

    public static boolean isCollection(PsiType psiType) {
        if (psiType instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) psiType;
            PsiClass psiClass = classType.resolve();
            if (psiClass != null) {
                if (psiClass.isEquivalentTo(findClass("java.util.Collection", psiClass.getProject()))
                        || psiClass.isInheritor(findClass("java.util.Collection", psiClass.getProject()), true)
                        || psiClass.isInheritor(findClass("java.util.Map", psiClass.getProject()), true)
                        || psiClass.isEquivalentTo(findClass("java.util.Map", psiClass.getProject()))
                ) {
                    return true;
                }
            }
        }

        if (psiType instanceof PsiArrayType) {
            return true;
        }

        return false;
    }

    public static boolean isTransientField(PsiField field) {
        if (field.getModifierList() == null) {
            return false;
        }
        for (String jpaPackage : JPA_PACKAGES) {
            PsiAnnotation annotation = field.getModifierList().findAnnotation((jpaPackage.isEmpty() ? jpaPackage : jpaPackage + ".") + "Transient");
            if (annotation != null) {
                return true;
            }

        }
        return false;
    }

    public static boolean hasRelation(PsiField field) {
        if (field.getModifierList() == null) {
            return false;
        }
        for (String jpaPackage : JPA_PACKAGES) {
            for (String relationAnnotation : RELATIONS_ANNOTATIONS) {
                PsiAnnotation annotation = field.getModifierList().findAnnotation((jpaPackage.isEmpty() ? jpaPackage : jpaPackage + ".") + relationAnnotation);
                if (annotation != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private static PsiClass findClass(String name, Project project) {
        if (classes.containsKey(name)) {
            return classes.get(name);
        }
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(name, GlobalSearchScope.allScope(project));
        classes.put(name, psiClass);
        return psiClass;
    }
}
