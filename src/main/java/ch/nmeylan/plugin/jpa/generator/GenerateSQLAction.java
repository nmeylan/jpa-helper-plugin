package ch.nmeylan.plugin.jpa.generator;

import ch.nmeylan.plugin.jpa.generator.model.EntityField;
import ch.nmeylan.plugin.jpa.generator.ui.ProjectionGeneratorDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateSQLAction extends AnAction {
    private final static List<String> entityClasses = List.of("javax.persistence.Entity", "jakarta.persistence.Entity");

    public GenerateSQLAction() {
        super("Generate SQL");
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        ProjectionModelGenerator projectionModelGenerator = new ProjectionModelGenerator(JavaPsiFacade.getInstance(project));

        if (psiFile != null) {
            PsiClass psiClass = PsiTreeUtil.getParentOfType(PsiUtilBase.getElementAtCaret(editor), PsiClass.class);

            if (psiClass != null && isEntityClass(psiClass)) {
                EntityField rootField = Helper.entityFields(psiClass);
                ProjectionGeneratorDialog dialog = new ProjectionGeneratorDialog(project, rootField);

                if (dialog.showAndGet()) {
                    String targetClassName = dialog.getClassNameSuffix();
                    List<EntityField> selectedFields = dialog.getSelectedFields();

                    generateProjectionClass(projectionModelGenerator, project, psiClass, targetClassName, rootField, selectedFields, dialog.isInnerClass());
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        String.format("The selected class is not an entity (missing %s annotation).", entityClasses.stream().map(c -> "@" + c).collect(Collectors.joining(" or "))),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean isEntityClass(PsiClass psiClass) {
        PsiModifierList modifierList = psiClass.getModifierList();
        if (modifierList != null) {
            for (PsiAnnotation annotation : modifierList.getAnnotations()) {
                if (entityClasses.contains(annotation.getQualifiedName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void generateProjectionClass(ProjectionModelGenerator projectionModelGenerator, Project project, PsiClass originalClass, String classSuffix, EntityField rootField, List<EntityField> selectedFields, boolean innerClass) {

        WriteCommandAction.runWriteCommandAction(project, () -> {
            for (PsiClass classToGenerate : projectionModelGenerator.generateProjection(classSuffix, rootField, selectedFields, innerClass)) {
                CodeStyleManager.getInstance(project).reformat(classToGenerate);
            }
        });

    }
}