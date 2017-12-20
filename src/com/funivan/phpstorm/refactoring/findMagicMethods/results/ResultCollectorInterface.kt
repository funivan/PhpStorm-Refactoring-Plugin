package com.funivan.phpstorm.refactoring.findMagicMethods.results

import com.intellij.psi.PsiElement


interface ResultCollectorInterface {
    fun add(element: PsiElement, classFqn: String)
}