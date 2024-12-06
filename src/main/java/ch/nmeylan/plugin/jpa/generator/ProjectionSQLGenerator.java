package ch.nmeylan.plugin.jpa.generator;

import ch.nmeylan.plugin.jpa.generator.model.ClassToGenerate;
import ch.nmeylan.plugin.jpa.generator.model.EntityField;
import ch.nmeylan.plugin.jpa.generator.model.MultiStringStyle;
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

    public ProjectionSQLGenerator(Project project) {
        this.javaPsiFacade = JavaPsiFacade.getInstance(project);
        this.elementFactory = javaPsiFacade.getElementFactory();
        this.project = project;
    }

    public String generateJPACriteriaBuilderQuery(ClassToGenerate root) {
        StringBuilder code = new StringBuilder();
        code.append("public List<").append(root.getImportableName()).append(">").append(" fetchAll() {").append(EOL);
        code.append(INDENT).append("CriteriaBuilder cb = entityManager.getCriteriaBuilder();").append(EOL);
        code.append(INDENT).append("CriteriaQuery<").append(root.getImportableName()).append("> query = cb.createQuery(" + root.getImportableName() + ".class);").append(EOL2);
        code.append(INDENT).append("Root<").append(root.getExistingClass().getName()).append("> root = query.from(").append(root.getExistingClass().getName()).append(".class);").append(EOL2);

        generateJoinCriteriaBuilder(root, code, "root");
        code.append(EOL);
        code.append(INDENT).append("query.select(cb.construct(").append(EOL);
        code.append(INDENT2).append(root.getImportableName()).append(".class").append(",").append(EOL);
        selectCriteriaBuilder(root, code, "root", 1);
        code.append(INDENT).append("));").append(EOL);
        code.append(INDENT).append("return entityManager.createQuery(query).getResultList();").append(EOL);
        code.append("}");
        return code.toString();
    }

    private static void selectCriteriaBuilder(ClassToGenerate classToGenerate, StringBuilder code, String joinVariableName, int level) {
        String indentation = INDENT;
        for (int i = 0; i < level; i++) {
            indentation += INDENT;
        }
        for (EntityField field : classToGenerate.getFields()) {
            if (field.isRelation()) {
                ClassToGenerate relation = classToGenerate.getChildrenRelation().get(field.getName());
                code.append(indentation).append("cb.construct(").append(relation.getImportableName()).append(".class").append(",").append(EOL);
                selectCriteriaBuilder(relation, code, relation.getJoinVariableName(), level + 1);
                code.append(indentation).append("),").append(EOL);
            } else {
                code.append(indentation).append(joinVariableName).append(".get(\"").append(field.getName()).append("\"),").append(EOL);
            }
        }
        code.delete(code.length() - 2, code.length() - 1);
    }

    private void generateJoinCriteriaBuilder(ClassToGenerate root, StringBuilder code, String joinName) {
        if (root.getChildrenRelation() == null) {
            return;
        }
        for (ClassToGenerate relation : root.getChildrenRelation().values()) {

            code.append(INDENT).append("Join<").append(root.getExistingClass().getName()).append(", ").append(relation.getExistingClass().getName()).append("> ")
                    .append(relation.getJoinVariableName()).append(" = ")
                    .append(joinName).append(".join(\"").append(relation.getFieldNameForInParentRelation()).append("\");").append(EOL);

            if (relation.getChildrenRelation() != null) {
                generateJoinCriteriaBuilder(relation, code, relation.getJoinVariableName());
            }
        }
    }

    public String generateJPQL(ClassToGenerate root, MultiStringStyle multiStringStyle) {
        StringBuilder code = new StringBuilder();
        if (multiStringStyle.isTextBlock()) {
            code.append("\"\"\"").append(EOL);
        }
        String endOfStr = endOfStr(multiStringStyle);
        String startOfStr = startOfStr(0, multiStringStyle);
        String rootAlias = lowerFirstChar(root);
        code.append(multiStringStyle.isTextBlock() ? "" : '"')
                .append("SELECT new ").append(root.getPackageName()).append(".").append(root.getImportableName()).append("(").append(endOfStr);
        selectJPQL(root, code, rootAlias, 1, multiStringStyle);
        code.append(startOfStr).append(")").append(endOfStr);
        code.append(startOfStr).append("FROM ").append(root.getImportableName()).append(" ").append(rootAlias).append(endOfStr);
        generateJoinJPQL(root, code, rootAlias, multiStringStyle);

        code.append(";").append(EOL);
        if (multiStringStyle.isTextBlock()) {
            code.append("\"\"\"").append(EOL);
        }
        return code.toString();
    }

    private void selectJPQL(ClassToGenerate classToGenerate, StringBuilder code, String joinVariableName, int level, MultiStringStyle multiStringStyle) {
        for (EntityField field : classToGenerate.getFields()) {
            if (field.isRelation()) {
                if (field.isDisabledToAvoidLoop()) continue;
                if (classToGenerate.getChildrenRelation() == null) {
                    // This should not happen. unless we forget to set disabled to avoid loop
                    // It used to happen because we prevent user to create loop in projection:
                    // AuthorEntity has one editor (EditorEntity) has many (authors) AuthorEntity <- this would loop
                    // we prevent user from selecting this entity
                    continue;
                }
                ClassToGenerate relation = classToGenerate.getChildrenRelation().get(field.getName());
                if (relation == null) continue; // same as above
                code.append(startOfStr(level, multiStringStyle)).append("new ").append(relation.getPackageName()).append(".").append(relation.getImportableName()).append("(").append(endOfStr(multiStringStyle));
                selectJPQL(relation, code, relation.getJoinVariableName(), level + 1, multiStringStyle);
                code.append(startOfStr(level, multiStringStyle)).append("),").append(endOfStr(multiStringStyle));
            } else {
                code.append(startOfStr(level, multiStringStyle)).append(joinVariableName).append(".").append(field.getName()).append(",").append(endOfStr(multiStringStyle));
            }
        }
        // removing trailing comma
        if (multiStringStyle.isTextBlock()) {
            code.delete(code.length() - 2, code.length() - 1);
        } else {
            code.delete(code.length() - 3, code.length() - 2);
        }
    }

    private void generateJoinJPQL(ClassToGenerate root, StringBuilder code, String joinName, MultiStringStyle multiStringStyle) {
        if (root.getChildrenRelation() == null) {
            return;
        }
        for (ClassToGenerate relation : root.getChildrenRelation().values()) {

            code.append(startOfStr(0, multiStringStyle)).append("JOIN ").append(joinName != null ? joinName + "." : "").append(relation.getFieldNameForInParentRelation()).append(" ")
                    .append(relation.getJoinNameForParent()).append(" ").append(endOfStr(multiStringStyle));

            if (relation.getChildrenRelation() != null) {
                generateJoinJPQL(relation, code, relation.getJoinVariableName(), multiStringStyle);
            }
        }
    }

    private String endOfStr(MultiStringStyle multiStringStyle) {
        return multiStringStyle.isTextBlock() ? EOL : '"' + EOL;
    }

    private String startOfStr(int level, MultiStringStyle multiStringStyle) {
        String indentation = (multiStringStyle.isTextBlock() ? "" : "+ \"");
        for (int i = 0; i < level; i++) {
            indentation += INDENT;
        }
        return indentation;
//        return multiStringStyle >= 15 ? indentation : indentation + "\"";
    }

    private static String lowerFirstChar(ClassToGenerate root) {
        return Character.toLowerCase(root.getExistingClass().getName().charAt(0)) + root.getExistingClass().getName().substring(1);
    }
}
