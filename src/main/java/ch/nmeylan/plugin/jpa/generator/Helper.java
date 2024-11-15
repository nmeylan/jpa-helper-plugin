package ch.nmeylan.plugin.jpa.generator;

import ch.nmeylan.plugin.jpa.generator.model.EntityField;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiTypesUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Helper {

    public static final List<String> CLASSES_WITH_PACKAGE = Arrays.asList(
        "java.util.AbstractCollection",
        "java.util.AbstractList",
        "java.util.AbstractQueue",
        "java.util.AbstractSequentialList",
        "java.util.AbstractSet",
        "java.util.concurrent.ArrayBlockingQueue",
        "java.util.ArrayDeque",
        "java.util.ArrayList",
        "javax.management.AttributeList",
        "java.sql.BatchUpdateException",
        "java.util.concurrent.ConcurrentHashMap.KeySetView",
        "java.util.concurrent.ConcurrentLinkedDeque",
        "java.util.concurrent.ConcurrentLinkedQueue",
        "java.util.concurrent.ConcurrentSkipListSet",
        "java.util.concurrent.CopyOnWriteArrayList",
        "java.util.concurrent.CopyOnWriteArraySet",
        "java.sql.DataTruncation",
        "java.util.concurrent.DelayQueue",
        "java.util.EnumSet",
        "java.util.HashSet",
        "java.util.concurrent.LinkedBlockingDeque",
        "java.util.concurrent.LinkedBlockingQueue",
        "java.util.LinkedHashSet",
        "java.util.LinkedList",
        "java.util.concurrent.LinkedTransferQueue",
        "java.util.concurrent.PriorityBlockingQueue",
        "java.util.PriorityQueue"
    );

    public static List<EntityField> entityFields(PsiClass psiClass) {
        List<EntityField> fields = new ArrayList<>();
        collectEntityFields(psiClass, fields);
        return fields;
    }

    private static void collectEntityFields(PsiClass psiClass, List<EntityField> fields) {
        for (PsiField field : psiClass.getFields()) {
            if (!isTransientField(field)) {
                EntityField entityField = new EntityField(field.getName(), field.getType());
                fields.add(entityField);



                String canonicalText = field.getType().getCanonicalText();
                PsiClass psiClass1 = PsiTypesUtil.getPsiClass(field.getType());
                System.out.println(canonicalText + " -> " + psiClass1);
//               if (canonicalText.startsWith("java.util.")) {
//        return canonicalText.equals("java.util.Collection") ||
//               canonicalText.equals("java.util.List") ||
//               canonicalText.equals("java.util.Set") ||
//               canonicalText.equals("java.util.Map") ||
//               canonicalText.startsWith("java.util.ArrayList") ||
//               canonicalText.startsWith("java.util.LinkedList") ||
//               canonicalText.startsWith("java.util.HashSet") ||
//               canonicalText.startsWith("java.util.TreeSet") ||
//               canonicalText.startsWith("java.util.HashMap") ||
//               canonicalText.startsWith("java.util.TreeMap");

            }
        }
    }

    public static boolean isCollectionField(PsiField field) {
        PsiType type = field.getType();

        // Check if it's an array
        if (type instanceof PsiArrayType) {
            return true;
        }

        // Handle primitive types and other non-class types
        if (!(type instanceof PsiClassType)) {
            return false;
        }

        PsiClassType classType = (PsiClassType) type;
        String canonicalText = classType.getCanonicalText();

        // Check for known collection types
        if (canonicalText.startsWith("java.util.")) {
            return canonicalText.startsWith("java.util.Collection") ||
                    canonicalText.startsWith("java.util.List") ||
                    canonicalText.startsWith("java.util.Set") ||
                    canonicalText.startsWith("java.util.Map") ||
                    canonicalText.startsWith("java.util.ArrayList") ||
                    canonicalText.startsWith("java.util.LinkedList") ||
                    canonicalText.startsWith("java.util.HashSet") ||
                    canonicalText.startsWith("java.util.TreeSet") ||
                    canonicalText.startsWith("java.util.HashMap") ||
                    canonicalText.startsWith("java.util.TreeMap");
        }

        PsiClass psiClass = classType.resolve();
        if (psiClass != null) {
            return InheritanceUtil.isInheritor(psiClass, "java.util.Collection") ||
                    InheritanceUtil.isInheritor(psiClass, "java.util.Map");
        }

        // If resolution fails, try to infer from the name
        String className = classType.getClassName();
        return className != null && (
                className.endsWith("List") ||
                        className.endsWith("Set") ||
                        className.endsWith("Map") ||
                        className.endsWith("Collection"));
    }

    private static boolean isTransientField(PsiField field) {
        return field.getModifierList() != null
                && field.getModifierList().findAnnotation("javax.persistence.Transient") != null
                && field.getModifierList().findAnnotation("jakarta.persistence.Transient") != null;
    }
}
