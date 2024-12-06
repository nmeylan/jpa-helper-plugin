package ch.nmeylan.plugin.jpa.generator.ui;

import ch.nmeylan.plugin.jpa.generator.model.EntityField;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckboxTreeBase;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.List;

public class ProjectionGeneratorDialog extends DialogWrapper {
    private JTextField classNameSuffix;
    private CheckboxTree checkboxTree;
    private JCheckBox innerClass;
    private EntityField rootField;


    public ProjectionGeneratorDialog(Project project, EntityField rootField) {
        super(project);
        this.rootField = rootField;
        setTitle("Generate Projection");
        setSize(400, getSize().height);
        init(); // Initialize the dialog
        setOKButtonText("Generate");
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel topFormPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbConstraints = new GridBagConstraints();

        topFormPanel.setBorder(IdeBorderFactory.createBorder());
        gbConstraints.anchor = GridBagConstraints.WEST;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        // Class name label
        gbConstraints.insets = JBUI.insets(4, 8);
        gbConstraints.gridx = 0;
        gbConstraints.weightx = 0;
        gbConstraints.gridwidth = 1;
        topFormPanel.add(new JLabel("Class name suffix"), gbConstraints);
        // Class name field
        gbConstraints.insets = JBUI.insets(4, 8);
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 1;
        gbConstraints.gridwidth = GridBagConstraints.REMAINDER;
        classNameSuffix = new JTextField("Projection");
        topFormPanel.add(classNameSuffix, gbConstraints);
        // Inner class checkbox label
        gbConstraints.insets = JBUI.insets(4, 8);
        gbConstraints.gridx = 0;
        gbConstraints.weightx = 0;
        gbConstraints.gridy = 1;
        gbConstraints.gridwidth = 1;
        topFormPanel.add(new JLabel("Inner class"), gbConstraints);
        // Inner class checkbox
        gbConstraints.insets = JBUI.insets(4, 8);
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 1;
        gbConstraints.gridwidth = GridBagConstraints.REMAINDER;
        innerClass = new JCheckBox();
        innerClass.setSelected(true);
        topFormPanel.add(innerClass, gbConstraints);

        // Add top form to main panel
        mainPanel.add(topFormPanel, BorderLayout.NORTH);

        // Checkbox panel for fields
        CheckedTreeNode root = buildCheckboxTree(rootField);
        CheckboxTreeBase.CheckPolicy checkPolicy = new CheckboxTreeBase.CheckPolicy(true, true, true, true);
        checkboxTree = new CheckboxTree(new CheckboxTree.CheckboxTreeCellRenderer() {
            @Override
            public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                String text = null;
                EntityField userObject = (EntityField) node.getUserObject();
                if (userObject == null) {
                    text = "";
                } else if (userObject.getName() == null) {
                    text = userObject.getOwnerClass().getName();
                } else {
                    text = userObject.getName();
                }
                getTextRenderer().append(text, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        }, root, checkPolicy);
        checkboxTree.setRootVisible(true);

        JBScrollPane scrollPane = new JBScrollPane(checkboxTree);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }

    public static CheckedTreeNode buildCheckboxTree(EntityField rootField) {
        CheckedTreeNode root = new CheckedTreeNode(rootField);
        root.setChecked(false);
        buildCheckboxTree(rootField.getChildrenFields(), root);
        return root;
    }

    private static void buildCheckboxTree(List<EntityField> fields, CheckedTreeNode node) {
        fields.sort((o1, o2) -> {
            if (o1.getChildrenFields() == null && o2.getChildrenFields() != null) {
                return -1;
            }
            if (o1.getChildrenFields() != null && o2.getChildrenFields() == null) {
                return 1;
            }
            return 0;
        });
        for (EntityField field : fields) {
            CheckedTreeNode newChild = new CheckedTreeNode(field);
            newChild.setChecked(false);
            if (field.isDisabledToAvoidLoop()) {
                newChild.setEnabled(false);
            }
            node.add(newChild);
            if (field.getChildrenFields() != null) {
                buildCheckboxTree(field.getChildrenFields(), newChild);
            }
        }
    }

    public String getClassNameSuffix() {
        return classNameSuffix.getText().trim();
    }

    public List<EntityField> getSelectedFields() {
        return Arrays.asList(checkboxTree.getCheckedNodes(EntityField.class, null));
    }

    public boolean isInnerClass() {
        return innerClass.isSelected();
    }
}
