package com.funivan.phpstorm.refactoring.findMagicMethods

import com.funivan.phpstorm.refactoring.findMagicMethods.Visitors.FieldReferenceVisitor
import com.funivan.phpstorm.refactoring.findMagicMethods.Visitors.MethodReferenceVisitor
import com.funivan.phpstorm.refactoring.findMagicMethods.results.ResultCollector
import com.funivan.phpstorm.refactoring.util.PhpIndexUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.usages.*
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.PhpClass
import java.util.*

/**
 * @author Ivan Shcherbak alotofall@gmail.com
 */
class FindMagicMethodCallAction : AnAction() {
    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val dataContext = anActionEvent.dataContext

        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return

        PsiDocumentManager.getInstance(project).commitAllDocuments()
        val usageTargets = anActionEvent.getData(UsageView.USAGE_TARGETS_KEY) ?: return

        val findTarget = (usageTargets[0] ?: return) as? PsiElementUsageTarget ?: return


        val targetName = findTarget.name ?: return

        val findTargetElement = findTarget.element
        if (findTargetElement.context !is PhpClass) {
            return
        }

        // Find all subclasses
        val visitor = when (targetName) {
            "__call" -> MethodReferenceVisitor(false)
            "__callStatic" -> MethodReferenceVisitor(true)
            "__get" -> FieldReferenceVisitor(false)
            "__set" -> FieldReferenceVisitor(true)
            else -> {
                return;
            }
        }

        val usages = ArrayList<Usage>()
        val phpClass = findTargetElement.context
        if (phpClass !is PhpClass) {
            return
        }

        object : Task.Backgroundable(project, "Find magic method call: " + targetName, true) {
            override fun run(indicator: ProgressIndicator) {
                ApplicationManager.getApplication().runReadAction {
                    val searchForClassesFQNs = HashMap<String, PhpClass>()

                    // Find all subclasses
                    val baseClassFqn = phpClass.fqn
                    searchForClassesFQNs.put(baseClassFqn, phpClass)
                    val phpIndex = PhpIndex.getInstance(phpClass.project)

                    val subClasses = phpIndex.getAllSubclasses(baseClassFqn)


                    for (el in subClasses) {
                        val classMethod = PhpIndexUtil.getClassMethod(el, targetName)

                        if (classMethod == null || classMethod.containingClass == null) {
                            continue
                        }

                        if (classMethod.containingClass == phpClass) {
                            searchForClassesFQNs.put(el.fqn, el)
                        }
                    }
                    val processedFiles = HashMap<String, Boolean>()
                    val baseDir = project.baseDir
                    val projectScope = GlobalSearchScope.projectScope(project)
                    visitor.collector = ResultCollector(searchForClassesFQNs, usages)


                    for (target in searchForClassesFQNs.values) {
                        indicator.text2 = target.presentableFQN
                        // Find class references
                        val search = ReferencesSearch.search(target, projectScope)
                        for (classUsage in search) {

                            //#   find usages inside this files
                            val containingFile = classUsage.element.containingFile
                            val file = containingFile.virtualFile


                            val path = VfsUtil.getRelativePath(file, baseDir, '/')
                            if (path != null && processedFiles[path] == null) {
                                processedFiles.put(path, true)
                                containingFile.acceptChildren(visitor)
                            }
                        }


                    }
                }

            }

            override fun onCancel() {
                super.onCancel()
                usages.clear()
            }

            override fun onSuccess() {
                super.onSuccess()
                //@todo show bubble: empty usages
                val usageViewPresentation = UsageViewPresentation()
                usageViewPresentation.tabText = "magic methods " + targetName
                UsageViewManager
                        .getInstance(project)
                        .showUsages(
                                UsageTarget.EMPTY_ARRAY,
                                usages.toTypedArray(),
                                usageViewPresentation
                        )

                usages.clear()
            }
        }.queue()


    }


}
