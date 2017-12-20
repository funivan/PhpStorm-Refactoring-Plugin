package com.funivan.phpstorm.refactoring.findMagicMethods.Visitors

import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.FieldReference
import com.jetbrains.php.lang.psi.elements.PhpReference

/**
 * @author Ivan Shcherbak alotofall@gmail.com>
 */

class FieldReferenceVisitor(private val writeAccess: Boolean?) : BaseElementVisitor() {

    override fun visitElement(element: PsiElement?) {
        if (element is FieldReference) {
            visitFieldReference((element as FieldReference?)!!)
        }
        super.visitElement(element)
    }


    private fun visitFieldReference(element: FieldReference) {
        val resolve = element.resolve()
        if (resolve != null) {
            return
        }
        if (element.isWriteAccess != writeAccess) {
            return
        }
        val classReference = element.classReference

        if (classReference !is PhpReference || classReference.multiResolve(false).isEmpty()) {
            return
        }
        val type = classReference.type.global(element.project)
        val types = type.types
        if (types.size != 1) {
            return
        }
        collector.add(
                element,
                types.iterator().next()
        )
    }

}

