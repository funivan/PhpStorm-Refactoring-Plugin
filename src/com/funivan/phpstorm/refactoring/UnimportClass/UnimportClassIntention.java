package com.funivan.phpstorm.refactoring.UnimportClass;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.PhpWorkaroundUtil;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.impl.PhpUseImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Ivan Scherbak <dev@funivan.com>
 */
public class UnimportClassIntention extends PsiElementBaseIntentionAction {


    /**
     * Get intent description.
     *
     * @return Description string.
     */
    @NotNull
    public String getText() {
        return "Unimport class";
    }

    /**
     * Get intent family name.
     *
     * @return Family name.
     */
    @NotNull
    public String getFamilyName() {
        return getText();
    }

    /**
     * Check if the intent is available at the current position.
     *
     * @param project Current project.
     * @param editor  Current editor.
     * @param element Element selected by the caret.
     * @return True if we can use the intent, false otherwise.
     */
    public boolean isAvailable(@NotNull Project project, Editor editor, @Nullable PsiElement element) {

        if (element == null || !PhpWorkaroundUtil.isIntentionAvailable(element) || element.getParent() == null) {
            return false;
        }

        PsiElement baseElement = element.getParent();


        if (!(baseElement instanceof ClassReference)) {
            return false;
        }

        String fqn = ((ClassReference) baseElement).getFQN();


        if (baseElement.getParent() instanceof PhpUseImpl) {
            return true;
        }


        if (baseElement.getText().equals(fqn)) {
            return false;
        }

        Collection<PhpClass> classes = PhpIndex.getInstance(project).getClassesByFQN(fqn);
        
        return classes.size() != 0;

    }

    /**
     * The intent is invoked. Do magic.
     *
     * @param project Current project.
     * @param editor  Current editor.
     * @param element Element selected by the caret.
     */
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        element = element.getParent();


        ClassReference classReference = (ClassReference) element;
        String shortFqn = classReference.getText();

        PsiElement parentElement = classReference.getParent();
        if (parentElement instanceof PhpUseImpl) {

            PhpUseImpl useStatement = (PhpUseImpl) parentElement;

            String alias = useStatement.getAliasName();
            if (alias != null) {
                shortFqn = alias;
            } else {
                shortFqn = useStatement.getName();
            }
        }


        final String searchClassName = shortFqn;

        PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(classReference);


        assert scopeForUseOperator != null;


        String fqn = classReference.getFQN();
        if (fqn == null) {
            return;
        }
        ClassReference newClassRef = PhpPsiElementFactory.createClassReference(project, fqn);

        final ArrayList<PsiElement> replaceElements = new ArrayList<>();


        scopeForUseOperator.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {

                if (element instanceof ClassReference) {
                    visitPhpClassReference((ClassReference) element);
                }

                if (element instanceof PhpDocType) {
                    visitPhpDocType((PhpDocType) element);
                }

                super.visitElement(element);

            }

            private void visitPhpDocType(PhpDocType phpDocType) {

                if (phpDocType.isAbsolute()) {
                    return;
                }


                if (phpDocType.getText().equals(searchClassName)) {
                    collectElement(phpDocType);
                }
            }

            private void visitPhpClassReference(ClassReference classReference) {

                // Skip import use statements
                if (classReference.getParent() instanceof PhpUseImpl) {
                    return;
                }

                String classFqn = classReference.getFQN();
                if (classFqn == null) {
                    return;
                }
                if (!classFqn.equals(fqn)) {
                    return;
                }

                collectElement(classReference);
            }

            private void collectElement(PsiElement currentElement) {
                if (currentElement.getText().equals(newClassRef.getText())) {
                    return;
                }

                replaceElements.add(currentElement);
            }


        });


        for (PsiElement oldReference : replaceElements) {
            oldReference.replace(newClassRef);
        }


    }

}
