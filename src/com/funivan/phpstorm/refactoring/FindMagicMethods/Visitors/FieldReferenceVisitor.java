package com.funivan.phpstorm.refactoring.FindMagicMethods.Visitors;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.PhpReference;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;

import java.util.Set;

/**
 * @author Ivan Shcherbak <alotofall@gmail.com>
 */

public class FieldReferenceVisitor extends BaseElementVisitor {

    private final Boolean writeAccess;

    public FieldReferenceVisitor(Boolean writeAccess) {
        this.writeAccess = writeAccess;
    }

    @Override
    public void visitElement(PsiElement element) {

        if (element instanceof FieldReference) {
            visitFieldReference((FieldReference) element);
        }

        super.visitElement(element);

    }


    private void visitFieldReference(FieldReference element) {
        PsiElement resolve = element.resolve();
        if (resolve != null) {
            return;
        }
        if (element.isWriteAccess() != writeAccess) {
            return;
        }
        PhpExpression classReference = element.getClassReference();

        if (!(classReference instanceof PhpReference) || ((PhpReference) classReference).multiResolve(false).length <= 0) {
            return;
        }
        PhpType type = classReference.getType().global(element.getProject());
        Set<String> types = type.getTypes();
        if (types.size() != 1) {
            return;
        }
        String typeFqn = types.iterator().next();
        getResultCollector().add(element, typeFqn);
    }

}

