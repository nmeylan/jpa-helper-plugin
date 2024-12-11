package ch.nmeylan.plugin.jpa.generator;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;

import static ch.nmeylan.plugin.jpa.generator.GenerateSQLAction.isEntityClass;

public class GenerateProjectionQueryAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        if (psiFile != null) {
            PsiClass psiClass = PsiTreeUtil.getParentOfType(PsiUtilBase.getElementAtCaret(editor), PsiClass.class);
            if (isEntityClass(psiClass)) {
                PsiMethod[] constructors = psiClass.getConstructors();
                if (constructors.length > 0) {
                    int countConstructorWithArgs = 0;
                    for (PsiMethod constructor : constructors) {
                        if (constructor.getParameters().length > 0) {
                            countConstructorWithArgs++;
                        }
                    }
                    event.getPresentation().setEnabledAndVisible(countConstructorWithArgs > 0);
                    return;
                }
            }
            if (PsiUtil.hasSuperclass(psiClass)) {
                PsiClass parentClass = psiClass.getSuperClass();
                if (isEntityClass(parentClass)) {
                    PsiMethod[] constructors = psiClass.getConstructors();
                    if (constructors.length > 0) {
                        int countConstructorWithArgs = 0;
                        for (PsiMethod constructor : constructors) {
                            if (constructor.getParameters().length > 0) {
                                countConstructorWithArgs++;
                            }
                        }
                        event.getPresentation().setEnabledAndVisible(countConstructorWithArgs > 0);
                        return;
                    }
                }
            }

        }
        event.getPresentation().setEnabledAndVisible(false);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);

        if (psiFile != null) {
            PsiClass psiClass = PsiTreeUtil.getParentOfType(PsiUtilBase.getElementAtCaret(editor), PsiClass.class);
            PsiElement elementAtCaret = PsiUtilBase.getElementAtCaret(editor);
            PsiMethod method = findEnclosingMethod(elementAtCaret);
            boolean hoverConstructor = method == null ? false : method.isConstructor();
            if (!hoverConstructor && psiClass.getConstructors().length > 1) {
                JOptionPane.showMessageDialog(null,
                        String.format("Class %s has more than 1 constructor, put your cursor hover the constructor to use for projection and retry", psiClass.getName()));
                return;
            }

            PsiClass parentClass = psiClass;
            if (PsiUtil.hasSuperclass(psiClass)) {
                parentClass = psiClass.getSuperClass();
            }
            List<PsiField> fields = new ArrayList<>(method.getParameterList() != null ? method.getParameterList().getParametersCount() : 0);
            for (PsiParameter param : method.getParameterList().getParameters()) {
                for(PsiField field : parentClass.getFields()) {
                    if (field.getName().equals(param.getName())) {
                        fields.add(field);
                    }
                }
            }

        }
    }

    private PsiMethod findEnclosingMethod(PsiElement element) {
        // Traverse parent hierarchy to find a PsiMethod
        while (element != null && !(element instanceof PsiMethod)) {
            element = element.getParent();
        }
        return (PsiMethod) element;
    }
}
