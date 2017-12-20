package com.funivan.phpstorm.refactoring.findMagicMethods.Visitors

import com.funivan.phpstorm.refactoring.findMagicMethods.MagicElementVisitor
import com.funivan.phpstorm.refactoring.findMagicMethods.results.ResultCollectorInterface
import com.funivan.phpstorm.refactoring.findMagicMethods.results.NullableResultCollector
import com.intellij.psi.PsiRecursiveElementWalkingVisitor

/**
 * @author Ivan Shcherbak alotofall@gmail.com>
 */
abstract class BaseElementVisitor : PsiRecursiveElementWalkingVisitor(), MagicElementVisitor {
    override var collector: ResultCollectorInterface = NullableResultCollector()
}
