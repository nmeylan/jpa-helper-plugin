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
        code.append("public List<").append(root.getImportableName()).append(">").append(" fetchAll() {").append(EOL);
        code.append(INDENT).append("CriteriaBuilder cb = entityManager.getCriteriaBuilder();").append(EOL);
        code.append(INDENT).append("CriteriaQuery<").append(root.getImportableName()).append("> query = cb.createQuery(" + root.getImportableName() + ".class);").append(EOL2);
        code.append(INDENT).append("Root<").append(root.getExistingClass().getName()).append("> root = query.from(").append(root.getExistingClass().getName()).append(".class);").append(EOL2);

        generateJoin(root, code, "root");
        code.append(EOL);
        code.append(INDENT).append("query.select(cb.construct(").append(EOL);
        code.append(INDENT2).append(root.getImportableName()).append(".class").append(",").append(EOL);
        select(root, code, "root", 1);
        code.append(INDENT).append("));").append(EOL);
        code.append(INDENT).append("return entityManager.createQuery(query).getResultList();").append(EOL);
        code.append("}");
        return code.toString();
    }

    private static void select(ClassToGenerate classToGenerate, StringBuilder code, String joinVariableName, int level) {
        String indentation = INDENT;
        for (int i = 0; i < level; i++) {
            indentation += INDENT;
        }
        for (EntityField field : classToGenerate.getFields()) {
            if (field.isRelation()) {
                ClassToGenerate relation = classToGenerate.getChildrenRelation().get(field.getName());
                code.append(indentation).append("cb.construct(").append(relation.getImportableName()).append(".class").append(",").append(EOL);
                select(relation, code, relation.getJoinVariableName(), level + 1);
                code.append(indentation).append("),").append(EOL);
            } else {
                code.append(indentation).append(joinVariableName).append(".get(\"").append(field.getName()).append("\"),").append(EOL);
            }
        }
        code.delete(code.length() - 2, code.length() - 1);
    }

    private void generateJoin(ClassToGenerate root, StringBuilder code, String joinName) {
        if (root.getChildrenRelation() == null) {
            return;
        }
        for (ClassToGenerate relation : root.getChildrenRelation().values()) {
            String varJoinName = relation.getJoinNameForParent();
            ClassToGenerate parent = relation.getParentRelation();
            while (parent != null) {
                varJoinName = (parent.getFieldNameForInParentRelation() != null ? parent.getFieldNameForInParentRelation() + Character.toUpperCase(varJoinName.charAt(0)) + varJoinName.substring(1) : "" + varJoinName);
                parent = parent.getParentRelation();
            }
            relation.setJoinVariableName(varJoinName);

            code.append(INDENT).append("Join<").append(root.getExistingClass().getName()).append(", ").append(relation.getExistingClass().getName()).append("> ")
                    .append(varJoinName).append(" = ")
                    .append(joinName).append(".join(\"").append(relation.getFieldNameForInParentRelation()).append("\");").append(EOL);

            if (relation.getChildrenRelation() != null) {
                generateJoin(relation, code, varJoinName);
            }
        }
    }
}
