package ch.nmeylan.plugin.jpa.generator.ui;

import ch.nmeylan.plugin.jpa.generator.ProjectionSQLGenerator;
import ch.nmeylan.plugin.jpa.generator.model.ClassToGenerate;
import ch.nmeylan.plugin.jpa.generator.model.ComboboxGenerateItem;
import ch.nmeylan.plugin.jpa.generator.model.MultiStringStyle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiFile;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class SqlGeneratorDialog extends DialogWrapper {

    MultiStringStyle multiStringStyle;
    ComboboxGenerateItem selectedProjection;
    Project project;
    Editor editor;
    Document document;
    ProjectionSQLGenerator projectionSQLGenerator;
    ClassToGenerate rootClassToGenerate;
    Map<ComboboxGenerateItem, String> codeCache;
    CopyAndNotifyAction copyToClipboard;

    public SqlGeneratorDialog(@Nullable Project project, ClassToGenerate classToGenerate, PsiFile psiFile) {
        super(project);
        setTitle("Generate SQL");
        setSize(1200, 600);
        this.project = project;
        setModal(false);
        this.projectionSQLGenerator = new ProjectionSQLGenerator(project);
        this.rootClassToGenerate = classToGenerate;
        codeCache = new HashMap<>();
        selectedProjection = ComboboxGenerateItem.PROJECTION_CB;
        this.document = EditorFactory.getInstance().createDocument(generateCode(selectedProjection));
        multiStringStyle = MultiStringStyle.TEXT_BLOCK;
        copyToClipboard = new CopyAndNotifyAction("Copy to Clipboard");
        init();
        getButton(copyToClipboard).setIcon(AllIcons.Actions.Copy);
    }

    private String generateCode(ComboboxGenerateItem comboboxGenerateItem) {
        String cachedCode = codeCache.get(comboboxGenerateItem);
        if (cachedCode != null) {
            return cachedCode;
        }
        String code;
        switch (comboboxGenerateItem) {
            case PROJECTION_JPQL:
                code = projectionSQLGenerator.generateJPQL(rootClassToGenerate, multiStringStyle);
                break;
            case PROJECTION_CB:
            default:
                code = projectionSQLGenerator.generateJPACriteriaBuilderQuery(rootClassToGenerate);
        }
        codeCache.put(comboboxGenerateItem, code);
        return code;
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
        gbConstraints.gridwidth = GridBagConstraints.RELATIVE;
        topFormPanel.add(new JLabel("Generate"), gbConstraints);
        // Generate combobox
        gbConstraints.insets = JBUI.insets(4, 8);
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 2;
        gbConstraints.gridwidth = GridBagConstraints.REMAINDER;
        ComboboxGenerateItem[] items = {
                ComboboxGenerateItem.PROJECTION_CB,
//                ComboboxGenerateItem.PROJECTION_JDBC,
                ComboboxGenerateItem.PROJECTION_JPQL,
//                ComboboxGenerateItem.PROJECTION_SPRING_JDBC,
//                ComboboxGenerateItem.INSERT_JDBC,
//                ComboboxGenerateItem.INSERT_SPRING_JDBC
        };

        // Create the combo box
        JComboBox<ComboboxGenerateItem> comboBox = new ComboBox<>(items);
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
            ) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ComboboxGenerateItem item) {
                    setText(item.text());
                }
                return c;
            }
        });
        comboBox.addActionListener(e -> {
            if (e.getActionCommand().equals("comboBoxChanged")) {
                ComboboxGenerateItem selectedItem = (ComboboxGenerateItem) comboBox.getSelectedItem();
                if (selectedItem != null) {
                    selectedProjection = selectedItem;
                    WriteCommandAction.writeCommandAction(project).run(() -> {
                        document.setText(generateCode(selectedProjection));
                    });
                }
            }
        });
        topFormPanel.add(comboBox, gbConstraints);

        gbConstraints.insets = JBUI.insets(4, 8);
        gbConstraints.gridy = 1;
        gbConstraints.gridx = 0;
        gbConstraints.weightx = 0;
        gbConstraints.gridwidth = GridBagConstraints.RELATIVE;
        topFormPanel.add(new JLabel("Multi line String style"), gbConstraints);
        gbConstraints.insets = JBUI.insets(4, 8);
        gbConstraints.gridy = 1;
        gbConstraints.gridx = 1;
        gbConstraints.weightx = 1;
        gbConstraints.gridwidth = GridBagConstraints.REMAINDER;
        JComboBox<MultiStringStyle> comboBoxStringStyle = new ComboBox<>(new MultiStringStyle[]{MultiStringStyle.TEXT_BLOCK, MultiStringStyle.CONCAT});
        comboBoxStringStyle.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
            ) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof MultiStringStyle item) {
                    setText(item.text());
                }
                return c;
            }
        });
        comboBoxStringStyle.addActionListener(e -> {
            if (e.getActionCommand().equals("comboBoxChanged")) {
                multiStringStyle = (MultiStringStyle) comboBoxStringStyle.getSelectedItem();
                codeCache.remove(selectedProjection);
                WriteCommandAction.writeCommandAction(project).run(() -> {
                    document.setText(generateCode(selectedProjection));
                });
            }
        });
        topFormPanel.add(comboBoxStringStyle, gbConstraints);

        // Add top form to main panel
        mainPanel.add(topFormPanel, BorderLayout.NORTH);
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
    protected void doOKAction() {
        CopyPasteManager.getInstance().setContents(new StringSelection(document.getText()));
    }

    @Override
    public void dispose() {
        super.dispose();
        EditorFactory.getInstance().releaseEditor(editor);
    }

    @Override
    protected Action[] createActions() {
        return new Action[]{
                copyToClipboard,
                getCancelAction()
        };
    }

    private class CopyAndNotifyAction extends AbstractAction {
        public CopyAndNotifyAction(String name) {
            super(name);
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            CopyPasteManager.getInstance().setContents(new StringSelection(document.getText()));

            JButton button = (JButton) e.getSource();
            Color originalColor = button.getBackground();
            String originalText = button.getText();

            button.setText("Copied to clipboard!");
            button.setIcon(AllIcons.Actions.Checked);

            Timer timer = new Timer(1000, event -> {
                button.setText(originalText);
                button.setToolTipText("");
                button.setIcon(null);
            });
            timer.setRepeats(false);
            timer.start();
        }
    }
}
