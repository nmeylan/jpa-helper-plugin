package ch.nmeylan.plugin.jpa.generator;

import com.intellij.psi.PsiClass;

public class PsiUtil {

    public static boolean hasSuperclass(PsiClass psiClass) {
        if (psiClass.getSuperClass() == null) {
            return false;
        }
        String superClassName = psiClass.getSuperClass().getQualifiedName();
        return !"java.lang.Object".equals(superClassName);
    }
}
