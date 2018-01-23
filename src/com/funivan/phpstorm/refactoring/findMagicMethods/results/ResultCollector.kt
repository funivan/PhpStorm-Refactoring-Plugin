package com.funivan.phpstorm.refactoring.findMagicMethods.results

import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageInfo
import com.intellij.usages.Usage
import com.intellij.usages.UsageInfo2UsageAdapter
import com.jetbrains.php.lang.psi.elements.PhpClass
import java.util.*

/**
 * @author Ivan Shcherbak alotofall@gmail.com
 */
class ResultCollector internal constructor(
        private val searchClassFQN: HashMap<String, PhpClass>,
        private val usages: MutableList<Usage>
) : ResultCollectorInterface {

    override fun add(element: PsiElement, classFqn: String) {
        if (searchClassFQN[classFqn] != null) {
            val usageInfo = UsageInfo(element)
            val usage = UsageInfo2UsageAdapter(usageInfo)
            usages.add(usage)
        }
    }

}
