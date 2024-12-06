package ch.nmeylan.plugin.jpa.generator;

import ch.nmeylan.plugin.jpa.generator.model.ClassToGenerate;
import ch.nmeylan.plugin.jpa.generator.model.EntityField;
import ch.nmeylan.plugin.jpa.generator.model.MultiStringStyle;
import com.intellij.openapi.application.PluginPathManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.util.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PluginTest extends LightJavaCodeInsightFixtureTestCase {
    private static final String BASE_PATH = "testData/entities/";
    private Map<String, PsiClass> classes;

    @BeforeAll
    @Override
    public void setUp() throws Exception {
        super.setUp();
        classes = new HashMap<>();
        classes.put("CharacterEntity", myFixture.addClass(ResourceUtil.loadText(getClass().getClassLoader().getResourceAsStream("fixtures/CharacterEntity.java"))));
        classes.put("InventoryEntity", myFixture.addClass(ResourceUtil.loadText(getClass().getClassLoader().getResourceAsStream("fixtures/InventoryEntity.java"))));
        classes.put("ItemEntity", myFixture.addClass(ResourceUtil.loadText(getClass().getClassLoader().getResourceAsStream("fixtures/ItemEntity.java"))));
        classes.put("AuthorEntity", myFixture.addClass(ResourceUtil.loadText(getClass().getClassLoader().getResourceAsStream("fixtures/bookstore/AuthorEntity.java"))));
        classes.put("EditorEntity", myFixture.addClass(ResourceUtil.loadText(getClass().getClassLoader().getResourceAsStream("fixtures/bookstore/EditorEntity.java"))));
        classes.put("BookDetailsEntity", myFixture.addClass(ResourceUtil.loadText(getClass().getClassLoader().getResourceAsStream("fixtures/bookstore/BookDetailsEntity.java"))));
        classes.put("BookEntity", myFixture.addClass(ResourceUtil.loadText(getClass().getClassLoader().getResourceAsStream("fixtures/bookstore/BookEntity.java"))));
        classes.put("CategoryEntity", myFixture.addClass(ResourceUtil.loadText(getClass().getClassLoader().getResourceAsStream("fixtures/bookstore/CategoryEntity.java"))));
        classes.put("CustomerEntity", myFixture.addClass(ResourceUtil.loadText(getClass().getClassLoader().getResourceAsStream("fixtures/bookstore/CustomerEntity.java"))));
        classes.put("OrderEntity", myFixture.addClass(ResourceUtil.loadText(getClass().getClassLoader().getResourceAsStream("fixtures/bookstore/OrderEntity.java"))));
        classes.put("OrderItemEntity", myFixture.addClass(ResourceUtil.loadText(getClass().getClassLoader().getResourceAsStream("fixtures/bookstore/OrderItemEntity.java"))));
    }

    @AfterAll
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected @NotNull String getTestDataPath() {
        return PluginPathManager.getPluginHome("jpa-projection-generator") + "/";
    }

    @NotNull
    protected LightProjectDescriptor getProjectDescriptor() {
        return new DefaultLightProjectDescriptor() {
            @Override
            public Sdk getSdk() {
                try {
                    return JavaSdk.getInstance().createJdk("java 1.17", new File(System.getProperty("java.home")).getCanonicalPath(), false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }


    @Override
    public String getName() {
        return "CollectFieldsForProjectionTest";
    }

    @Test
    public void testJdkClassAvailability() {
        ReadAction.run(() -> {
            PsiClass stringClass = JavaPsiFacade.getInstance(getProject()).findClass("java.lang.String", GlobalSearchScope.allScope(getProject()));
            assertNotNull("java.lang.String should be available", stringClass);
        });
    }

    @Test
    public void isCollection() throws Exception {
        PsiFile psiFile = myFixture.configureByText("A.java", """
                import java.util.Collection;
                import java.util.HashMap;
                import java.util.List;
                import java.util.Map;
                import java.util.Queue;
                import java.util.Set;
                import java.util.concurrent.ConcurrentHashMap;
                public class A {
                    List<String> a1;
                    Collection<String> a2;
                    Set<String> a3;
                    Map<String, Object> a4;
                    ConcurrentHashMap<String, Object> a5;
                    Queue<String> a6;
                    HashMap<String, Object> a7;
                
                    String b1;
                    Integer b2;
                    long b3;
                }
                """);
        ReadAction.run(() -> {
            PsiClass psiClass = findClassInFile(psiFile, "A");
            HashMap<String, Boolean> actual = new HashMap<>();
            Map<String, Boolean> expected = Map.of("a1", true, "a2", true, "a3", true, "a4", true, "a5", true, "a6", true, "b1", false, "b2", false, "b3", false);
            for (PsiField field : psiClass.getFields()) {
                actual.put(field.getName(), Graph.isCollection(field.getType()));
            }
            for (Map.Entry<String, Boolean> entry : expected.entrySet()) {
                Boolean res = actual.get(entry.getKey());
                assertThat(res).overridingErrorMessage("Expected field <%s> to be <%s> but was <%s>", entry.getKey(), entry.getValue(), res).isEqualTo(entry.getValue());
            }
        });
    }

    @Test
    public void entityFields_shouldBuildAGraphOfEntityField() throws Exception {
        AtomicReference<EntityField> jobWizard = new AtomicReference<>();
        ReadAction.run(() -> {
            EntityField root = Graph.entityFields(classes.get("InventoryEntity"));
            Graph.iterateEntityFields(root.getChildrenFields(), (entityField, depth) -> {
                if (entityField.getName().equals("jobWizard")) {
                    jobWizard.set(entityField);
                }
                System.out.println(entityField);
            });
        });
        assertThat(jobWizard.get()).isNotNull();
        assertThat(jobWizard.get().getOwnerClass().getName()).isEqualTo("ItemEntity");
        assertThat(jobWizard.get().getParentField().getName()).isEqualTo("items");
        assertThat(jobWizard.get().getParentField().getOwnerClass().getName()).isEqualTo("InventoryEntity");
    }

    @Test
    public void classesToGenerate_shouldBuildAGraphOfClasses() {
        Map<String, ClassToGenerate> classToGenerates = new HashMap<>();
        AtomicReference<EntityField> expectationItemsField = new AtomicReference<>();
        ReadAction.run(() -> {
            EntityField root = Graph.entityFields(classes.get("InventoryEntity"));
            List<EntityField> selectedFields = new ArrayList<>();
            Graph.iterateEntityFields(root.getChildrenFields(), (entityField, depth) -> {
                if (entityField.getName().equals("jobWizard")) {
                    selectedFields.add(entityField);
                }
                if (entityField.getName().equals("items")) {
                    expectationItemsField.set(entityField);
                }
            });
            classToGenerates.putAll(ProjectionModelGenerator.classesToGenerate("Projection", root, selectedFields, true));

        });
        assertThat(classToGenerates).isNotEmpty();
        assertThat(classToGenerates.get("root-fixtures.InventoryEntity")).isNotNull();
        assertThat(classToGenerates.get("root.items-fixtures.ItemEntity")).isNotNull();
        assertThat(classToGenerates.get("root-fixtures.InventoryEntity").getFields()).hasSize(1);
        assertThat(classToGenerates.get("root-fixtures.InventoryEntity").getFields()).contains(expectationItemsField.get());
        assertThat(classToGenerates.get("root-fixtures.InventoryEntity").getParentRelation()).isNull();
        assertThat(classToGenerates.get("root-fixtures.InventoryEntity").getChildrenRelation().values()).contains(classToGenerates.get("root.items-fixtures.ItemEntity"));
        assertThat(classToGenerates.get("root.items-fixtures.ItemEntity").getFields()).hasSize(1);
        assertThat(classToGenerates.get("root.items-fixtures.ItemEntity").getFields().iterator().next().getName()).isEqualTo("jobWizard");
        assertThat(classToGenerates.get("root.items-fixtures.ItemEntity").getParentRelation()).isEqualTo(classToGenerates.get("root-fixtures.InventoryEntity"));
        assertThat(classToGenerates.get("root.items-fixtures.ItemEntity").getChildrenRelation()).isNull();
    }

    @Test
    public void shouldBuildPsiClass() {
        Map<String, ClassToGenerate> classToGenerates = new HashMap<>();
        AtomicReference<EntityField> expectationItemsField = new AtomicReference<>();
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            EntityField root = Graph.entityFields(classes.get("InventoryEntity"));
            List<EntityField> selectedFields = new ArrayList<>();
            Graph.iterateEntityFields(root.getChildrenFields(), (entityField, depth) -> {
                if (entityField.getName().equals("jobWizard")) {
                    selectedFields.add(entityField);
                }
                if (entityField.getName().equals("items")) {
                    expectationItemsField.set(entityField);
                }
            });
            classToGenerates.putAll(ProjectionModelGenerator.classesToGenerate("Projection", root, selectedFields, true));
            ProjectionModelGenerator projectionModelGenerator = new ProjectionModelGenerator(getProject());
            PsiClass psiClass = projectionModelGenerator.psiClassToCreate(classToGenerates.get("root-fixtures.InventoryEntity"), true);
            CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(getProject());
            codeStyleManager.reformat(psiClass);
            System.out.println(psiClass.getText());
        });
    }

    @Test
    public void projectionJPACriteriaBuilder() {
        projectionTest((classToGenerates, projectionSQLGenerator) -> projectionSQLGenerator.generateJPACriteriaBuilderQuery(classToGenerates.get("root-ch.nmeylan.blog.example.bookstore.BookEntity")));
        projectionTest((classToGenerates, projectionSQLGenerator) -> projectionSQLGenerator.generateJPACriteriaBuilderQuery(classToGenerates.get("root-ch.nmeylan.blog.example.bookstore.BookEntity")));
    }

    @Test
    public void projectionJPQL() {
        projectionTest((classToGenerates, projectionSQLGenerator) -> projectionSQLGenerator.generateJPQL(classToGenerates.get("root-ch.nmeylan.blog.example.bookstore.BookEntity"), MultiStringStyle.TEXT_BLOCK));
        projectionTest((classToGenerates, projectionSQLGenerator) -> projectionSQLGenerator.generateJPQL(classToGenerates.get("root-ch.nmeylan.blog.example.bookstore.BookEntity"), MultiStringStyle.CONCAT));
    }

    @Test
    public void OneToManyRelation() {
         Map<String, ClassToGenerate> classToGenerates = new HashMap<>();
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            EntityField root = Graph.entityFields(classes.get("BookEntity"));
            List<EntityField> selectedFields = new ArrayList<>();
            Graph.iterateEntityFields(root.getChildrenFields(), (entityField, depth) -> {
                selectedFields.add(entityField);
            });
            classToGenerates.putAll(ProjectionModelGenerator.classesToGenerate("Projection", root, selectedFields, true));
            ProjectionModelGenerator projectionModelGenerator = new ProjectionModelGenerator(getProject());
            ProjectionSQLGenerator projectionSQLGenerator = new ProjectionSQLGenerator(getProject());
            for (Map.Entry<String, ClassToGenerate> entry : classToGenerates.entrySet()) {
                PsiClass psiClass = projectionModelGenerator.psiClassToCreate(entry.getValue(), true);
                CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(getProject());
                codeStyleManager.reformat(psiClass);
                System.out.println(psiClass.getText());
            }
            String generated = projectionSQLGenerator.generateJPQL(classToGenerates.get("root-ch.nmeylan.blog.example.bookstore.BookEntity"), MultiStringStyle.TEXT_BLOCK);
            System.out.println(generated);
            generated = projectionSQLGenerator.generateJPACriteriaBuilderQuery(classToGenerates.get("root-ch.nmeylan.blog.example.bookstore.BookEntity"));
            System.out.println(generated);
        });
    }

    private void projectionTest(BiFunction<Map<String, ClassToGenerate>, ProjectionSQLGenerator, String> generateProjection) {
        Map<String, ClassToGenerate> classToGenerates = new HashMap<>();
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            EntityField root = Graph.entityFields(classes.get("BookEntity"));
            List<EntityField> selectedFields = new ArrayList<>();
            Graph.iterateEntityFields(root.getChildrenFields(), (entityField, depth) -> {
                if (entityField.getOwnerClass().getName().equals("BookEntity")) {
                    if (entityField.getName().equals("id") || entityField.getName().equals("title") || entityField.getName().equals("isbn") || entityField.getName().equals("price") || entityField.getName().equals("author") || entityField.getName().equals("secondaryAuthor")) {
                        selectedFields.add(entityField);
                    }
                }
                if (entityField.getOwnerClass().getName().equals("AuthorEntity")) {
                    if (entityField.getName().equals("firstName") || entityField.getName().equals("lastName") || entityField.getName().equals("editor")) {
                        selectedFields.add(entityField);
                    }
                }
                if (entityField.getOwnerClass().getName().equals("EditorEntity")) {
                    if (entityField.getName().equals("name") || entityField.getName().equals("email")) {
                        selectedFields.add(entityField);
                    }
                }
            });
            classToGenerates.putAll(ProjectionModelGenerator.classesToGenerate("Projection", root, selectedFields, true));
            ProjectionModelGenerator projectionModelGenerator = new ProjectionModelGenerator(getProject());
            ProjectionSQLGenerator projectionSQLGenerator = new ProjectionSQLGenerator(getProject());
            for (Map.Entry<String, ClassToGenerate> entry : classToGenerates.entrySet()) {
                PsiClass psiClass = projectionModelGenerator.psiClassToCreate(entry.getValue(), true);
                CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(getProject());
                codeStyleManager.reformat(psiClass);
                System.out.println(psiClass.getText());
            }
            String generated = generateProjection.apply(classToGenerates, projectionSQLGenerator);
            System.out.println(generated);
        });

    }

    private static PsiClass findClassInFile(PsiFile psiFile, String className) {
        // Iterate through all children in the file
        PsiElement[] children = psiFile.getChildren();
        for (PsiElement child : children) {
            if (child instanceof PsiClass) {
                PsiClass psiClass = (PsiClass) child;
                if (psiClass.getName().equals(className)) {
                    return psiClass;
                }
            }
        }
        return null;  // Return null if no matching class is found
    }
}
