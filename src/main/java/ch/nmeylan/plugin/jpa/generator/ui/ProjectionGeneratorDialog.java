package ch.nmeylan.plugin.jpa.generator.ui;

import ch.nmeylan.plugin.jpa.generator.Helper;
import ch.nmeylan.plugin.jpa.generator.model.EntityField;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiClass;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckboxTreeBase;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

public class ProjectionGeneratorDialog extends DialogWrapper {
    private final PsiClass psiClass;
    private JTextField classNameField;
    private CheckboxTree checkboxTree;


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
        List<EntityField> fields = Helper.entityFields(psiClass);
        CheckedTreeNode root = iterateEntityFields(fields);
        CheckboxTreeBase.CheckPolicy checkPolicy = new CheckboxTreeBase.CheckPolicy(true, true, true, true);
        checkboxTree = new CheckboxTree(new CheckboxTree.CheckboxTreeCellRenderer() {
            @Override
            public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                String text = null;
                if (node.getUserObject() == null) {
                    text = psiClass.getName();
                } else {
                    text = ((EntityField) node.getUserObject()).getName();
                }
                getTextRenderer().append(text, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        }, root, checkPolicy);
//        Helper.iterateEntityFields(fields, (field, depth) -> {
//            JCheckBox checkBox = new JCheckBox(field.getName());
//            checkBox.setBorder(BorderFactory.createEmptyBorder(0, depth * 10, 0, 0));
//            fieldCheckBoxes.add(checkBox);
//            fieldsPanel.add(checkBox);
//        });
        JBScrollPane scrollPane = new JBScrollPane(checkboxTree);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    public static CheckedTreeNode iterateEntityFields(List<EntityField> fields) {
        CheckedTreeNode root = new CheckedTreeNode(null);
        iterateEntityFields(fields, root);
        return root;
    }

    private static void iterateEntityFields(List<EntityField> fields, CheckedTreeNode node) {
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
            CheckedTreeNode newChild = new CheckedTreeNode(field);
            node.add(newChild);
            if (field.getRelationFields() != null) {
                iterateEntityFields(field.getRelationFields(), newChild);
            }
        }
    }

    public String getTargetClassName() {
        return classNameField.getText().trim();
    }

    public List<EntityField> getSelectedFields() {
        return Arrays.asList(checkboxTree.getCheckedNodes(EntityField.class, null));
    }

}
