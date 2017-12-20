package com.funivan.phpstorm.refactoring.UnimportClass;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.PhpIndex;
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
 * @author Ivan Shcherbak <alotofall@gmail.com>
 */
public class UnimportClassIntention extends PsiElementBaseIntentionAction {


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

    public boolean isAvailable(@NotNull Project project, Editor editor, @Nullable PsiElement element) {
        if (element != null && element.getParent() != null) {
            PsiElement baseElement = element.getParent();
            if ((baseElement instanceof ClassReference)) {
                String fqn = ((ClassReference) baseElement).getFQN();
                if (baseElement.getParent() instanceof PhpUseImpl) {
                    return true;
                }
                if (!baseElement.getText().equals(fqn)) {
                    Collection<PhpClass> classes = PhpIndex.getInstance(project).getClassesByFQN(fqn);
                    return classes.size() != 0;
                }
            }
        }
        return false;
    }

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
        if (scopeForUseOperator == null) {
            return;
        }
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
                    // Skip import use statements
                    final ClassReference classReference = (ClassReference) element;
                    if (!(classReference.getParent() instanceof PhpUseImpl)) {
                        String classFqn = classReference.getFQN();
                        if (classFqn != null) {
                            if (classFqn.equals(fqn)) {
                                if (!classReference.getText().equals(newClassRef.getText())) {
                                    replaceElements.add(classReference);
                                }
                            }
                        }
                    }
                }
                if (element instanceof PhpDocType) {
                    final PhpDocType phpDocType = (PhpDocType) element;
                    if (!phpDocType.isAbsolute() && phpDocType.getText().equals(searchClassName)) {
                        if (!phpDocType.getText().equals(newClassRef.getText())) {
                            replaceElements.add(phpDocType);
                        }
                    }
                }
                super.visitElement(element);
            }
        });
        for (PsiElement oldReference : replaceElements) {
            oldReference.replace(newClassRef);
        }
    }

}
