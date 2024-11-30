package ch.nmeylan.plugin.jpa.generator;

import ch.nmeylan.plugin.jpa.generator.model.ClassToGenerate;
import ch.nmeylan.plugin.jpa.generator.model.EntityField;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElementFactory;

public class ProjectionSQLGenerator {
    private JavaPsiFacade javaPsiFacade;
    private Project project;
    private PsiElementFactory elementFactory;
    private static final String INDENT = "    ";
    private static final String INDENT2 = INDENT + INDENT;
    private static final String EOL = "\n";
    private static final String EOL2 = EOL + EOL;

    public ProjectionSQLGenerator(JavaPsiFacade javaPsiFacade, Project project) {
        this.javaPsiFacade = javaPsiFacade;
        this.elementFactory = javaPsiFacade.getElementFactory();
        this.project = project;
    }

    public String generateJPACriteriaBuilderQuery(ClassToGenerate root) {

        StringBuilder code = new StringBuilder();
        code.append("public List<").append(root.getName()).append(">").append(" fetchAll() {").append(EOL);
        code.append(INDENT).append("CriteriaBuilder cb = entityManager.getCriteriaBuilder();").append(EOL);
        code.append(INDENT).append("CriteriaQuery<").append(root.getName()).append("> query = cb.createQuery(" + root.getName() + ".class);").append(EOL2);
        code.append(INDENT).append("Root<").append(root.getExistingClass().getName()).append("> root = query.getRoot();").append(EOL2);

        generateJoin(root, code, "root");
        code.append(EOL);
        code.append(INDENT).append("query.select(cb.construct(").append(EOL);
        code.append(INDENT2).append(root.getName()).append(".class").append(",").append(EOL);
        for (EntityField field : root.getFields()) {
            code.append(INDENT2).append("root.").append(field.getName()).append(",").append(EOL);
            // TODO: handle nested relation
        }
        code.delete(code.length() - 2, code.length() - 1);
        code.append(INDENT).append("));").append(EOL);
        code.append(INDENT).append("entityManager.createQuery(query).getResultList()").append(EOL);
        code.append("}");
        return code.toString();
    }

    private static void generateJoin(ClassToGenerate root, StringBuilder code, String joinName) {
        for (ClassToGenerate relation : root.getChildrenRelation()) {
            code.append(INDENT).append("Join<").append(root.getExistingClass().getName()).append(", ").append(relation.getExistingClass().getName()).append("> ")
                    .append(relation.getJoinNameForParent()).append(" = ")
                    .append(joinName).append(".join(\"").append(relation.getFieldNameForInParentRelation()).append("\");").append(EOL);

            if (relation.getChildrenRelation() != null) {
                generateJoin(relation, code, relation.getJoinNameForParent());
            }
        }
    }
}
