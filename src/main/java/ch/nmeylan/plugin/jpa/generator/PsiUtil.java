package ch.nmeylan.plugin.jpa.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;

public class PsiUtil {
    public static PsiClass findPsiClass(String className, Project project) {
        if (className == null || className.isEmpty() || project == null) {
            return null;
        }

        // Use JavaPsiFacade to find the class
        return JavaPsiFacade.getInstance(project).findClass(
                className,
                GlobalSearchScope.allScope(project) // Specify the search scope
        );
    }
}
