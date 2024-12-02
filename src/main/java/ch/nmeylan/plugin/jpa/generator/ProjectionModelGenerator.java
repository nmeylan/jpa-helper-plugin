package ch.nmeylan.plugin.jpa.generator;

import ch.nmeylan.plugin.jpa.generator.model.ClassToGenerate;
import ch.nmeylan.plugin.jpa.generator.model.EntityField;
import com.intellij.ide.util.PackageUtil;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectionModelGenerator {
    private JavaPsiFacade javaPsiFacade;
    private Project project;
    private PsiElementFactory elementFactory;

    public ProjectionModelGenerator(JavaPsiFacade javaPsiFacade, Project project) {
        this.javaPsiFacade = javaPsiFacade;
        this.elementFactory = javaPsiFacade.getElementFactory();
        this.project = project;
    }

    public List<PsiClass> generateProjection(String projectionSuffix, EntityField rootField, List<EntityField> selectedFields, boolean innerClass) {
        Map<String, ClassToGenerate> classesToGenerate = classesToGenerate(projectionSuffix, rootField, selectedFields, innerClass);
        return generateProjection(classesToGenerate, innerClass);
    }

    public List<PsiClass> generateProjection(Map<String, ClassToGenerate> classesToGenerate, boolean innerClass) {
        HashSet<String> classesSignature = new HashSet<>();
        return classesToGenerate.entrySet().stream()
                .filter(entry -> {
                    String classSignature = entry.getValue().getName() + entry.getValue().getFields().stream().map(EntityField::getName).collect(Collectors.joining());
                    if (classesSignature.contains(classSignature)) {
                        return false;
                    }
                    classesSignature.add(classSignature);
                    return true;
                })
                .map(entry -> psiClassToCreate(entry.getValue(), innerClass)).toList();
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
            field.getPsiField().getModifierList().setModifierProperty(PsiModifier.PROTECTED, true);
            constructor.getParameterList().add(elementFactory.createParameter(field.getName(), field.getType()));
            constructor.getBody().add(elementFactory.createStatementFromText("this." + field.getName() + " = " + field.getName() + ";", null));
        }
        psiClass.add(constructor);
        CodeStyleManager.getInstance(project).reformat(psiClass);
        if (innerClass) {
            classToGenerate.getExistingClass().add(psiClass);
        }
        return psiClass;
    }

    public static Map<String, ClassToGenerate> classesToGenerate(String projectionSuffix, EntityField rootField, List<EntityField> selectedFields, boolean innerClass) {
        Map<String, ClassToGenerate> classesToGenerate = new HashMap<>();
        Helper.iterateEntityFields(rootField.getChildrenFields(), (field, path) -> {
            if (!selectedFields.contains(field)) {
                return;
            }
            ClassToGenerate classToGenerate = null;
            String key = path + "-" + field.getOwnerClass().getQualifiedName();
            classToGenerate = getClassToGenerate(key, projectionSuffix, field, classesToGenerate, innerClass);
            classToGenerate.addField(field);

            EntityField parent = field.getParentField();
            if (parent != null) {
                key = path.substring(0, path.lastIndexOf(".")) + "-" + parent.getOwnerClass().getQualifiedName();
                ClassToGenerate parentClass = getClassToGenerate(key, projectionSuffix, parent, classesToGenerate, innerClass);

                classToGenerate.addParentRelation(parentClass);
                if (parentClass.addChildRelation(parent.getName(), classToGenerate)) {
                   classToGenerate.setFieldNameForInParentRelation(parent.getName());
                }
                parentClass.addField(parent);
                parent = parent.getParentField();
                classToGenerate = parentClass;
            }
        });
        return classesToGenerate;
    }

    private static ClassToGenerate getClassToGenerate(String key, String projectionSuffix, EntityField field, Map<String, ClassToGenerate> classesToGenerate, boolean innerClass) {

        ClassToGenerate classToGenerate;
        if (!classesToGenerate.containsKey(key)) {
            classToGenerate = new ClassToGenerate(field.getOwnerClass().getName() + projectionSuffix, field.getOwnerClass(), innerClass);
            classesToGenerate.put(key, classToGenerate);
        } else {
            classToGenerate = classesToGenerate.get(key);
        }
        return classToGenerate;
    }
}
