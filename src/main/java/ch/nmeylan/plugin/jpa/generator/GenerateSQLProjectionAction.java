package ch.nmeylan.plugin.jpa.generator;

import ch.nmeylan.plugin.jpa.generator.model.EntityField;
import ch.nmeylan.plugin.jpa.generator.ui.ProjectionGeneratorDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateSQLProjectionAction extends AnAction {
    private final static List<String> entityClasses = List.of("javax.persistence.Entity", "jakarta.persistence.Entity");

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);

        if (psiFile != null) {
            PsiClass psiClass = PsiTreeUtil.getParentOfType(PsiUtilBase.getElementAtCaret(editor), PsiClass.class);

            if (psiClass != null && isEntityClass(psiClass)) {
                ProjectionGeneratorDialog dialog = new ProjectionGeneratorDialog(project, psiClass);

                if (dialog.showAndGet()) {
                    String targetClassName = dialog.getTargetClassName();
                    List<EntityField> selectedFields = dialog.getSelectedFields();

                    generateProjectionClass(project, psiClass, targetClassName, selectedFields);
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
    private void generateProjectionClass(Project project, PsiClass originalClass, String targetClassName, List<EntityField> selectedFields) {
        for(EntityField field : selectedFields) {
            String out = field.getOwnerClass().getName() + "." + field.getName();
            EntityField parent = field.getParentRelation();
            while(parent != null) {
                out = parent.getOwnerClass().getName() + "." + out;
                parent = parent.getParentRelation();
            }
            System.out.println(out);
        }
        // Logic to generate the projection class goes here
        // This is where you would use PsiClass and PsiElementFactory to create the class with selected fields.
    }
}