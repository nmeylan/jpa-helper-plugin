package ch.nmeylan.plugin.jpa.generator.ui;

import ch.nmeylan.plugin.jpa.generator.model.ComboboxGenerateItem;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public class SqlGeneratorDialog   extends DialogWrapper {

    Project project;
    Editor editor;

    public SqlGeneratorDialog(@Nullable Project project) {
        super(project);
        setTitle("Generate SQL");
        setSize(800, 600);
        this.project = project;
        setModal(false);
        init(); // Initialize the dialog
        setOKButtonText("Copy to clipboard");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel topFormPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbConstraints = new GridBagConstraints();
        topFormPanel.setBorder(IdeBorderFactory.createBorder());
        gbConstraints.anchor = GridBagConstraints.WEST;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        // Inner class checkbox label
        gbConstraints.insets = JBUI.insets(4, 8);
        gbConstraints.gridx = 0;
        gbConstraints.weightx = 0;
        gbConstraints.gridy = 0;
        gbConstraints.gridwidth = 1;
        topFormPanel.add(new JLabel("Generate"), gbConstraints);
        // Generate combobox
        gbConstraints.insets = JBUI.insets(4, 8);
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 1;
        gbConstraints.gridwidth = GridBagConstraints.REMAINDER;
        ComboboxGenerateItem[] items = {
                ComboboxGenerateItem.PROJECTION_CB,
                ComboboxGenerateItem.PROJECTION_JDBC,
                ComboboxGenerateItem.PROJECTION_JPQL,
                ComboboxGenerateItem.PROJECTION_SPRING_JDBC,
                ComboboxGenerateItem.INSERT_JDBC,
                ComboboxGenerateItem.INSERT_SPRING_JDBC
        };

        // Create the combo box
        JComboBox<ComboboxGenerateItem> comboBox = new ComboBox<>(items);
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
            ) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ComboboxGenerateItem) {
                    ComboboxGenerateItem item = (ComboboxGenerateItem) value;
                    setText(item.text());
                }
                return c;
            }
        });
        topFormPanel.add(comboBox, gbConstraints);

        // Add top form to main panel
        mainPanel.add(topFormPanel, BorderLayout.NORTH);
        Document document = EditorFactory.getInstance().createDocument("public void Main(String args[]) {}");
        FileType fileTypeInstance = FileTypeManager.getInstance().getFileTypeByExtension("java");

        // Create the editor
        editor = EditorFactory.getInstance().createEditor(document, project, fileTypeInstance, true);

        // Customize editor settings
        EditorSettings settings = editor.getSettings();
        settings.setLineNumbersShown(true);
        settings.setFoldingOutlineShown(true);
        settings.setWhitespacesShown(true);
        settings.setUseSoftWraps(true);

        JBScrollPane scrollPane = new JBScrollPane();
//        scrollPane.add(new JLabel("hello"));
//        scrollPane.add(editor.getComponent());
        scrollPane.setPreferredSize(new Dimension(600, 40));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(editor.getComponent(), BorderLayout.CENTER);

        return mainPanel;
    }

    @Override
     public void dispose() {
        super.dispose();
        EditorFactory.getInstance().releaseEditor(editor);
    }
}