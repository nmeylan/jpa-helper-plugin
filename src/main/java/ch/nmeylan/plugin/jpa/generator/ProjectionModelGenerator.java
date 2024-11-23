package ch.nmeylan.plugin.jpa.generator;

import ch.nmeylan.plugin.jpa.generator.model.ClassToGenerate;
import ch.nmeylan.plugin.jpa.generator.model.EntityField;
import com.intellij.ide.util.PackageUtil;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.util.PsiUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectionModelGenerator {
    private JavaPsiFacade javaPsiFacade;
    private PsiElementFactory elementFactory;

    public ProjectionModelGenerator(JavaPsiFacade javaPsiFacade) {
        this.javaPsiFacade = javaPsiFacade;
        this.elementFactory = javaPsiFacade.getElementFactory();
    }

    public String generateProjection(String projectionSuffix, EntityField rootField, List<EntityField> selectedFields, boolean innerClass) {
        Map<String, ClassToGenerate> classesToGenerate = classesToGenerate(projectionSuffix, rootField, selectedFields);
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

    public PsiClass psiClassToCreate(ClassToGenerate classToGenerate, boolean innerClass) {
        PsiClass psiClass = null;
        if (innerClass) {
            psiClass = elementFactory.createClass(classToGenerate.getName());
            psiClass.getModifierList().setModifierProperty(PsiModifier.STATIC, true);
        } else {
            JavaDirectoryService javaDirectoryService = JavaDirectoryService.getInstance();
            String packageName = PsiUtil.getPackageName(classToGenerate.getExistingClass());
            PsiDirectory targetDirectory = PackageUtil.findPossiblePackageDirectoryInModule(ModuleUtilCore.findModuleForPsiElement(classToGenerate.getExistingClass()), packageName);
            psiClass = javaDirectoryService.createClass(targetDirectory, classToGenerate.getName());
        }
        PsiJavaCodeReferenceElement superClassReference = elementFactory.createReferenceFromText(classToGenerate.getExistingClass().getName(), null);
        psiClass.getExtendsList().add(superClassReference);
        PsiMethod constructor = elementFactory.createConstructor(classToGenerate.getName());
        for (EntityField field : classToGenerate.getFields()) {
            constructor.getParameterList().add(elementFactory.createParameter(field.getName(), field.getType()));
            constructor.getBody().add(elementFactory.createStatementFromText("this." + field.getName() + " = " + field.getName() + ";", null));
        }
        psiClass.add(constructor);
        return psiClass;
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
            classToGenerate = new ClassToGenerate(field.getOwnerClass().getName() + projectionSuffix, field.getOwnerClass());
            classesToGenerate.put(key, classToGenerate);
        } else {
            classToGenerate = classesToGenerate.get(key);
        }
        return classToGenerate;
    }
}
