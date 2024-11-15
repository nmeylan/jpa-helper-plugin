package ch.nmeylan.plugin.jpa.generator;

import ch.nmeylan.plugin.jpa.generator.ui.ProjectionGeneratorDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
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
                    List<String> selectedFields = dialog.getSelectedFields();

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

    private void openProjectionDialog(Project project, PsiClass psiClass) {
        // Creating the dialog UI components
        JDialog dialog = new JDialog();
        dialog.setTitle("Generate SQL Projection");

        JPanel panel = new JPanel(new BorderLayout());
        JPanel fieldsPanel = new JPanel(new GridLayout(0, 1));

        JTextField classNameField = new JTextField(psiClass.getName() + "Projection");
        panel.add(new JLabel("Enter target class name:"), BorderLayout.NORTH);
        panel.add(classNameField, BorderLayout.CENTER);

        java.util.List<JCheckBox> fieldCheckBoxes = new ArrayList<>();

        for (PsiField field : psiClass.getFields()) {
            if (!isTransientField(field)) {
                JCheckBox checkBox = new JCheckBox(field.getName());
                fieldCheckBoxes.add(checkBox);
                fieldsPanel.add(checkBox);
            }
        }

        panel.add(fieldsPanel, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(null, panel, "SQL Projection Options", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String targetClassName = classNameField.getText();
            java.util.List<String> selectedFields = new ArrayList<>();

            for (JCheckBox checkBox : fieldCheckBoxes) {
                if (checkBox.isSelected()) {
                    selectedFields.add(checkBox.getText());
                }
            }

            generateProjectionClass(project, psiClass, targetClassName, selectedFields);
        }

        dialog.pack();
        dialog.setVisible(true);
    }

    private boolean isTransientField(PsiField field) {
        PsiModifierList modifierList = field.getModifierList();
        if (modifierList != null) {
            for (PsiAnnotation annotation : modifierList.getAnnotations()) {
                if ("javax.persistence.Transient".equals(annotation.getQualifiedName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void generateProjectionClass(Project project, PsiClass originalClass, String targetClassName, List<String> selectedFields) {
        // Logic to generate the projection class goes here
        // This is where you would use PsiClass and PsiElementFactory to create the class with selected fields.
    }
}