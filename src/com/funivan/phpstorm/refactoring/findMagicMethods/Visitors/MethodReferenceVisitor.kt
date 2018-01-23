package com.funivan.phpstorm.refactoring.findMagicMethods.Visitors

import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.MethodReference

/**
 * @author Ivan Shcherbak alotofall@gmail.com
 */
class MethodReferenceVisitor(private val findStaticMethods: Boolean) : BaseElementVisitor() {

    override fun visitElement(element: PsiElement) {
        if (element is MethodReference) {
            val resolve = element.resolve()
            if (resolve !is Method) {

                if ((findStaticMethods && element.isStatic) || (!findStaticMethods && !element.isStatic)) {
                    val type = element.type
                    //@todo get method type
                    val classFqn = type.toString().replace(("^#M#C(.+)\\." + element.name + ".+$").toRegex(), "$1")
                    collector.add(element, classFqn)
                }
            }
        }
        super.visitElement(element)
    }

}

