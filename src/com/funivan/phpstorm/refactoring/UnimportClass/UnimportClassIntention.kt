package com.funivan.phpstorm.refactoring.UnimportClass

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.psi.PhpPsiElementFactory
import com.jetbrains.php.lang.psi.elements.ClassReference
import com.jetbrains.php.lang.psi.elements.impl.PhpUseImpl
import java.util.*

/**
 * @author Ivan Shcherbak alotofall@gmail.com
 */
class UnimportClassIntention : PsiElementBaseIntentionAction() {


    override fun getText(): String {
        return "Unimport class"
    }

    /**
     * Get intent family name.
     *
     * @return Family name.
     */
    override fun getFamilyName(): String {
        return text
    }

    override fun isAvailable(p0: Project, p1: Editor?, element: PsiElement): Boolean {
        var result = false
        if (element.parent != null) {
            val baseElement = element.parent
            if (baseElement is ClassReference) {
                val fqn = baseElement.fqn
                if (baseElement.getParent() is PhpUseImpl) {
                    result = true
                } else if (baseElement.getText() != fqn) {
                    result = PhpIndex.getInstance(p0).getClassesByFQN(fqn).isNotEmpty()
                }
            }
        }
        return result
    }

    override fun invoke(project: Project, editor: Editor, element: PsiElement) {
        val el = element.parent
        val classReference = el as ClassReference
        var searchClassName = classReference.text
        val parentElement = classReference.parent
        if (parentElement is PhpUseImpl) {
            val alias = parentElement.aliasName
            searchClassName = if (alias != null) alias else parentElement.name
        }
        val scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(classReference) ?: return
        val fqn = classReference.fqn
        if (fqn != null) {
            val newClassRef = PhpPsiElementFactory.createClassReference(project, fqn)
            val replaceElements = ArrayList<PsiElement>()
            scopeForUseOperator.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement?) {
                    if (element is ClassReference) {
                        // Skip import use statements
                        if (element.parent !is PhpUseImpl) {
                            val classFqn = classReference.fqn
                            if (classFqn != null) {
                                if (classFqn == fqn) {
                                    if (classReference.text != newClassRef.text) {
                                        replaceElements.add(classReference)
                                    }
                                }
                            }
                        }
                    }
                    if (element is PhpDocType) {
                        if (!element.isAbsolute && element.text == searchClassName) {
                            if (element.text != newClassRef.text) {
                                replaceElements.add(element)
                            }
                        }
                    }
                    super.visitElement(element)
                }
            })
            for (oldReference in replaceElements) {
                oldReference.replace(newClassRef)
            }
        } else {
            return
        }
    }

}
