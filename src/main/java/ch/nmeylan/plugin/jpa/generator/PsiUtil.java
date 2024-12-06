package ch.nmeylan.plugin.jpa.generator;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
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

    public static int getJavaVersion(PsiFile psiFile) {
        Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
        Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
        if (sdk != null && "JavaSDK".equals(sdk.getSdkType().getName())) {
            String[] parts = sdk.getVersionString().split("\\.");
            try {
                if (parts[0].equals("1")) {
                    return Integer.parseInt(parts[1]);
                } else {
                    return Integer.parseInt(parts[0]);
                }
            } catch (Exception e) {
            }
        }

        return 8;
    }

    public static boolean hasSuperclass(PsiClass psiClass) {
        if (psiClass.getSuperClass() == null) {
            return false;
        }
        String superClassName = psiClass.getSuperClass().getQualifiedName();
        return !"java.lang.Object".equals(superClassName);
    }
}
