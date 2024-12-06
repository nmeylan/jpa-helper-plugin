package ch.nmeylan.plugin.jpa.generator;

import ch.nmeylan.plugin.jpa.generator.model.ClassToGenerate;
import ch.nmeylan.plugin.jpa.generator.model.EntityField;
import ch.nmeylan.plugin.jpa.generator.ui.ProjectionGeneratorDialog;
import ch.nmeylan.plugin.jpa.generator.ui.SqlGeneratorDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerateSQLAction extends AnAction {
    private final static List<String> entityClasses = List.of("javax.persistence.Entity", "jakarta.persistence.Entity");


    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        if (psiFile != null) {
            PsiClass psiClass = PsiTreeUtil.getParentOfType(PsiUtilBase.getElementAtCaret(editor), PsiClass.class);
            event.getPresentation().setEnabledAndVisible(isEntityClass(psiClass));
            return;
        }
        event.getPresentation().setEnabledAndVisible(false);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        ProjectionModelGenerator projectionModelGenerator = new ProjectionModelGenerator(project);

        if (psiFile != null) {
            PsiClass psiClass = PsiTreeUtil.getParentOfType(PsiUtilBase.getElementAtCaret(editor), PsiClass.class);

            if (psiClass != null && isEntityClass(psiClass)) {
                EntityField rootField = Graph.entityFields(psiClass);
                ProjectionGeneratorDialog dialog = new ProjectionGeneratorDialog(project, rootField);

                if (dialog.showAndGet()) {
                    String suffix = dialog.getClassNameSuffix();
                    List<EntityField> selectedFields = dialog.getSelectedFields();

                    Map<String, ClassToGenerate> classesToGenerate = ProjectionModelGenerator.classesToGenerate(suffix, rootField, selectedFields, dialog.isInnerClass());
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        projectionModelGenerator.generateProjection(classesToGenerate, dialog.isInnerClass());
                    });

                    SqlGeneratorDialog sqlGeneratorDialog = new SqlGeneratorDialog(project, classesToGenerate.get("root-" + psiClass.getQualifiedName()), psiFile);
                    sqlGeneratorDialog.show();
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        String.format("The selected class is not an entity (missing %s annotation).", entityClasses.stream().map(c -> "@" + c).collect(Collectors.joining(" or "))),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static boolean isEntityClass(PsiClass psiClass) {
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
}