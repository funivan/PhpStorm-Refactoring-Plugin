package com.funivan.phpstorm.refactoring.FindMagicMethods.Visitors;

import com.funivan.phpstorm.refactoring.FindMagicMethods.ElementResultCollector;
import com.funivan.phpstorm.refactoring.FindMagicMethods.MagicElementVisitor;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;

/**
 * Created by ivan
 */
abstract public class BaseElementVisitor extends PsiRecursiveElementWalkingVisitor implements MagicElementVisitor {
    private ElementResultCollector filter;

    @Override
    public void setResultCollector(ElementResultCollector filter) {
        this.filter = filter;
    }

    public ElementResultCollector getResultCollector() {
        return filter;
    }
}
