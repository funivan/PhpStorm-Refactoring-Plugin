package com.funivan.phpstorm.refactoring.FindMagicMethods.Visitors;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;

/**
 * Created by ivan on 22.01.16.
 */

public class MethodReferenceVisitor extends BaseElementVisitor {


    private final Boolean findStaticMethods;

    public MethodReferenceVisitor(Boolean findStaticMethods) {
        this.findStaticMethods = findStaticMethods;
    }

    @Override
    public void visitElement(PsiElement element) {

        if (element instanceof MethodReference) {
            PsiElement resolve = ((MethodReference) element).resolve();

            if ((resolve instanceof Method)) {
                // Our method cant be resolved and that`s why it is magic)
                return;
            }

            if ((findStaticMethods && !((MethodReference) element).isStatic()) || (!findStaticMethods && ((MethodReference) element).isStatic())) {
                return;
            }

            PhpType type = ((MethodReference) element).getType();

            if (type == null) {
                return;
            }

            //@todo get method type
            String classFqn = type.toString().replaceAll("^#M#C(.+)\\." + ((MethodReference) element).getName() + ".+$", "$1");
            getResultCollector().add(element, classFqn);

        }

        super.visitElement(element);

    }

}

