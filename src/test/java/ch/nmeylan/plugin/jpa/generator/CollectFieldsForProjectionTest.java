package ch.nmeylan.plugin.jpa.generator;

import ch.nmeylan.plugin.jpa.generator.model.EntityField;
import com.intellij.openapi.application.PluginPathManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.util.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectFieldsForProjectionTest extends LightJavaCodeInsightFixtureTestCase {
    private static final String BASE_PATH = "testData/entities/";

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
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
                    return JavaSdk.getInstance()
                            .createJdk(
                                    "java 1.17", new File(System.getProperty("java.home")).getCanonicalPath(), false);
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
            PsiClass stringClass = JavaPsiFacade.getInstance(getProject())
                    .findClass("java.lang.String", GlobalSearchScope.allScope(getProject()));
            assertNotNull("java.lang.String should be available", stringClass);
        });
    }

    @Test
    public void testCollectFieldsForProjection() throws Exception {
//        ReadAction.run(() -> {
//                    configureByFile(BASE_PATH + "CharacterEntity.java", BASE_PATH);
//                });
        myFixture.addClass( ResourceUtil.loadText(getClass().getClassLoader().getResourceAsStream("fixtures/CharacterEntity.java")));
        PsiClass psiClass =   myFixture.addClass(ResourceUtil.loadText(getClass().getClassLoader().getResourceAsStream("fixtures/InventoryEntity.java")));
        myFixture.addClass( ResourceUtil.loadText(getClass().getClassLoader().getResourceAsStream("fixtures/ItemEntity.java")));
        ReadAction.run(() -> {
//        myFixture.findClass("CharacterEntity");
//        PsiClass psiClass =   myFixture.findClass("InventoryEntity");
//         myFixture.findClass("ItemEntity");

        List<EntityField> entityFields = Helper.entityFields(psiClass);
        assertThat(entityFields).isNotEmpty();
        for (EntityField entityField : entityFields) {
            System.out.println(entityField);
        }
        });
    }

    private PsiClass findClassInFile(PsiFile psiFile, String className) {
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
