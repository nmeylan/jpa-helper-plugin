package ch.nmeylan.plugin.jpa.generator;

import ch.nmeylan.plugin.jpa.generator.model.ClassToGenerate;
import ch.nmeylan.plugin.jpa.generator.model.EntityField;
import com.intellij.openapi.application.PluginPathManager;
import com.intellij.openapi.application.ReadAction;
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
                actual.put(field.getName(), Helper.isCollection(field.getType()));
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
            EntityField root = Helper.entityFields(classes.get("InventoryEntity"));
            Helper.iterateEntityFields(root.getRelationFields(), (entityField, depth) -> {
                if (entityField.getName().equals("jobWizard")) {
                    jobWizard.set(entityField);
                }
                System.out.println(entityField);
            });
        });
        assertThat(jobWizard.get()).isNotNull();
        assertThat(jobWizard.get().getOwnerClass().getName()).isEqualTo("ItemEntity");
        assertThat(jobWizard.get().getParentRelation().getName()).isEqualTo("items");
        assertThat(jobWizard.get().getParentRelation().getOwnerClass().getName()).isEqualTo("InventoryEntity");
    }

    @Test
    public void classesToGenerate_shouldBuildAGraphOfClasses() {
        Map<String, ClassToGenerate> classToGenerates = new HashMap<>();
        AtomicReference<EntityField> expectationItemsField = new AtomicReference<>();
        ReadAction.run(() -> {
            EntityField root = Helper.entityFields(classes.get("InventoryEntity"));
            List<EntityField> selectedFields = new ArrayList<>();
            Helper.iterateEntityFields(root.getRelationFields(), (entityField, depth) -> {
                if (entityField.getName().equals("jobWizard")) {
                    selectedFields.add(entityField);
                }
                if (entityField.getName().equals("items")) {
                    expectationItemsField.set(entityField);
                }
            });
            classToGenerates.putAll(ProjectionModelGenerator.classesToGenerate("Projection", root, selectedFields));

        });
        assertThat(classToGenerates).isNotEmpty();
        assertThat(classToGenerates.get("fixtures.InventoryEntity")).isNotNull();
        assertThat(classToGenerates.get("fixtures.ItemEntity")).isNotNull();
        assertThat(classToGenerates.get("fixtures.InventoryEntity").getFields()).hasSize(1);
        assertThat(classToGenerates.get("fixtures.InventoryEntity").getFields()).contains(expectationItemsField.get());
        assertThat(classToGenerates.get("fixtures.InventoryEntity").getParentRelation()).isNull();
        assertThat(classToGenerates.get("fixtures.InventoryEntity").getChildrenRelation()).contains(classToGenerates.get("fixtures.ItemEntity"));
        assertThat(classToGenerates.get("fixtures.ItemEntity").getFields()).hasSize(1);
        assertThat(classToGenerates.get("fixtures.ItemEntity").getFields().iterator().next().getName()).isEqualTo("jobWizard");
        assertThat(classToGenerates.get("fixtures.ItemEntity").getParentRelation()).isEqualTo(classToGenerates.get("fixtures.InventoryEntity"));
        assertThat(classToGenerates.get("fixtures.ItemEntity").getChildrenRelation()).isNull();
    }

    @Test
    public void shouldBuildPsiClass() {
        Map<String, ClassToGenerate> classToGenerates = new HashMap<>();
        AtomicReference<EntityField> expectationItemsField = new AtomicReference<>();
        ReadAction.run(() -> {
            EntityField root = Helper.entityFields(classes.get("InventoryEntity"));
            List<EntityField> selectedFields = new ArrayList<>();
            Helper.iterateEntityFields(root.getRelationFields(), (entityField, depth) -> {
                if (entityField.getName().equals("jobWizard")) {
                    selectedFields.add(entityField);
                }
                if (entityField.getName().equals("items")) {
                    expectationItemsField.set(entityField);
                }
            });
            classToGenerates.putAll(ProjectionModelGenerator.classesToGenerate("Projection", root, selectedFields));
            ProjectionModelGenerator projectionModelGenerator = new ProjectionModelGenerator(JavaPsiFacade.getInstance(getProject()));
            PsiClass psiClass = projectionModelGenerator.psiClassToCreate(classToGenerates.get("fixtures.InventoryEntity"), true);
            CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(getProject());
            codeStyleManager.reformat(psiClass);
            System.out.println(psiClass.getText());

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
