package com.funivan.phpstorm.refactoring.FindMagicMethods;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ivan on 22.01.16.
 */

class FindMethodRecursiveElementWalkingVisitor extends PsiRecursiveElementWalkingVisitor {

    private final HashMap<String, PhpClass> searchClassFQN;
    private final List<Usage> usages;
    private final Boolean findStaticMethods;

    public FindMethodRecursiveElementWalkingVisitor(HashMap<String, PhpClass> searchClassesFQNs, List<Usage> usages, Boolean findStaticMethods) {
        this.searchClassFQN = searchClassesFQNs;
        this.usages = usages;
        this.findStaticMethods = findStaticMethods;
    }

    public void visitPhpMethodReference(MethodReference reference) {

        MethodReference methodRef = reference;
        PsiElement resolve = methodRef.resolve();

        if ((resolve instanceof Method)) {
            // our method cant be resolved and that`s why it is magic)
            return;
        }


        String text = methodRef.getText();


        if ((findStaticMethods && !methodRef.isStatic()) || (!findStaticMethods && methodRef.isStatic())) {
            return;
        }

        PhpType type = methodRef.getType();

        if (type == null) {
            return;
        }


        //@todo get method type
        String classFqn = type.toString().replaceAll("^#M#C(.+)\\." + methodRef.getName() + ".+$", "$1");

        if (searchClassFQN.get(classFqn) == null) {
            return;
        }

        final UsageInfo usageInfo = new UsageInfo(reference.getElement());
        Usage usage = new UsageInfo2UsageAdapter(usageInfo);
        usages.add(usage);

    }

    @Override
    public void visitElement(PsiElement element) {

        if (element instanceof MethodReference) {
            visitPhpMethodReference((MethodReference) element);
        }

        super.visitElement(element);

    }

}

