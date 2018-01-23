package com.funivan.phpstorm.refactoring.findMagicMethods.Visitors

import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.FieldReference
import com.jetbrains.php.lang.psi.elements.PhpReference

/**
 * @author Ivan Shcherbak alotofall@gmail.com
 */

class FieldReferenceVisitor(private val writeAccess: Boolean) : BaseElementVisitor() {

    override fun visitElement(element: PsiElement?) {
        if (element is FieldReference) {
            if (element.resolve() == null) {
                if (element.isWriteAccess == writeAccess) {
                    val classReference = element.classReference
                    if (classReference is PhpReference && !classReference.multiResolve(false).isEmpty()) {
                        val type = classReference.type.global(element.project)
                        val types = type.types
                        if (types.size == 1) {
                            collector.add(
                                    element,
                                    types.iterator().next()
                            )
                        }
                    }
                }
            }
        }
        super.visitElement(element)
    }

}

