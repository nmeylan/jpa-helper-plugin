package ch.nmeylan.plugin.jpa.generator.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

public class ProjectionGeneratorDialog extends DialogWrapper {
    private final PsiClass psiClass;
    private JTextField classNameField;
    private List<JCheckBox> fieldCheckBoxes;


    public ProjectionGeneratorDialog(Project project, PsiClass psiClass) {
        super(project);
        this.psiClass = psiClass;
        setTitle("Generate SQL Projection");
        init(); // Initialize the dialog
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Input field for the target class name
        JPanel namePanel = new JPanel(new BorderLayout());
        classNameField = new JTextField(psiClass.getName() + "Projection");
        namePanel.add(new JLabel("Enter target class name:"), BorderLayout.WEST);
        namePanel.add(classNameField, BorderLayout.CENTER);
        mainPanel.add(namePanel, BorderLayout.NORTH);

        // Checkbox panel for fields
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldCheckBoxes = new ArrayList<>();

        for (PsiField field : psiClass.getFields()) {
            if (!isTransientField(field)) {
                JCheckBox checkBox = new JCheckBox(field.getName());
                fieldCheckBoxes.add(checkBox);
                fieldsPanel.add(checkBox);
            }
        }

        JBScrollPane scrollPane = new JBScrollPane(fieldsPanel);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    public String getTargetClassName() {
        return classNameField.getText().trim();
    }

    public List<String> getSelectedFields() {
        List<String> selectedFields = new ArrayList<>();
        for (JCheckBox checkBox : fieldCheckBoxes) {
            if (checkBox.isSelected()) {
                selectedFields.add(checkBox.getText());
            }
        }
        return selectedFields;
    }

    private boolean isTransientField(PsiField field) {
        return field.getModifierList() != null
                && field.getModifierList().findAnnotation("javax.persistence.Transient") != null
                && field.getModifierList().findAnnotation("jakarta.persistence.Transient") != null;
    }
}
