package com.funivan.phpstorm.refactoring.UnimportClass;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpWorkaroundUtil;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.jetbrains.php.lang.psi.visitors.PhpRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by ivan on 19.01.16.
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

        if (fqn.equals(baseElement.getText())) {
            return false;
        }

        return true;
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

        PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(classReference);

        assert scopeForUseOperator != null;


        String fqn = classReference.getFQN();
        ClassReference newClassRef = PhpPsiElementFactory.createClassReference(project, fqn);


        scopeForUseOperator.acceptChildren(new PhpRecursiveElementVisitor() {
            public void visitPhpElement(PhpPsiElement element) {
                if (!PhpCodeInsightUtil.isScopeForUseOperator(element)) {
                    super.visitPhpElement(element);
                }

            }

            public void visitPhpDocType(PhpDocType phpDocType) {

                if (phpDocType.isAbsolute() == true) {
                    return;
                }

                if (phpDocType.getText().equals(classReference.getText())) {
                    // probably idea byg. FQN of docElement consist of Current namespace + ClassName
                    replace(phpDocType);
                }
            }

            public void visitPhpClassReference(ClassReference classReference) {
                if (classReference.getParent() instanceof PhpUse) {
                    return;
                }

                if (!classReference.getFQN().equals(fqn)) {
                    return;
                }

                replace(classReference);
            }

            private void replace(PsiElement currentElement) {
                if (currentElement.getText().equals(newClassRef.getText())) {
                    return;
                }
                currentElement.replace(newClassRef);
            }
        });

    }

}
