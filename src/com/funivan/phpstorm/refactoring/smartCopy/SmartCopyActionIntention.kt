package com.funivan.phpstorm.refactoring.smartCopy

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.jetbrains.php.lang.PhpLanguage
import com.jetbrains.php.lang.psi.elements.ClassReference

class SmartCopyActionIntention : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val dataContext = event.dataContext
        val editor = PlatformDataKeys.EDITOR.getData(dataContext)
        if (editor == null) {
            return
        }
        val project = editor.project
        if (project == null) {
            return
        }
        val virtualFile = PlatformDataKeys.VIRTUAL_FILE.getData(dataContext)
        if (virtualFile == null) {
            return
        }
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
        if (psiFile == null) {
            return
        }

        val offset = editor.caretModel.offset - 1
        val el = psiFile.findElementAt(offset)
        if (el == null) {
            return
        }

        var languageId = el.language.id
        // check if we have injected part of string
        val injectedElementAt = InjectedLanguageManager.getInstance(psiFile.project).findInjectedElementAt(psiFile, offset)
        if (injectedElementAt != null) {
            languageId = injectedElementAt.language.id
        }
        if (languageId != PhpLanguage.INSTANCE.id) {
            return
        }
        val selectionModel = editor.selectionModel
        val start = selectionModel.selectionStart
        val end = selectionModel.selectionEnd
        if (start > 0 && end > start) {
            println("Selection from $start to $end")
            val base = psiFile.findElementAt(0)
            if (base != null) {
                base.accept(object : PsiRecursiveElementVisitor() {
                    override fun visitElement(element: PsiElement) {
                        println("start : ${element.textOffset}")
                        println("el    : ${element.text}")
                        if (element.textOffset > start && element is ClassReference) {
                            println("")
                            println("start: ${element.textOffset}")
                            println("element: ${element.fqn}")
                        }
                        super.visitElement(element)
                    }
                })
            }
        }
    }
}