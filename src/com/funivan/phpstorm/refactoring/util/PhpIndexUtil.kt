package com.funivan.phpstorm.refactoring.util

import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpClass

/**
 * @author Ivan Shcherbak alotofall@gmail.com
 */
object PhpIndexUtil {

    fun getClassMethod(phpClass: PhpClass, methodName: String): Method? {
        val classMethods = phpClass.methods
        for (m in classMethods) {
            if (m.name == methodName) {
                return m
            }
        }

        return null
    }

}
