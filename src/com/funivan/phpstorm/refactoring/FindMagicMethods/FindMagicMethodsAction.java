package com.funivan.phpstorm.refactoring.FindMagicMethods;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.usages.PsiElementUsageTarget;
import com.intellij.usages.UsageTarget;
import com.intellij.usages.UsageView;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;

/**
 * Created by ivan on 20.01.16.
 */
public class FindMagicMethodsAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        DataContext dataContext = anActionEvent.getDataContext();

        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }

        PsiDocumentManager.getInstance(project).commitAllDocuments();
        final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);

        if (editor == null) {
            return;
        }

        UsageTarget[] usageTargets = (UsageTarget[]) anActionEvent.getData(UsageView.USAGE_TARGETS_KEY);

        if (usageTargets == null) {
            return;

        }

        UsageTarget findTarget = usageTargets[0];

        if (findTarget == null) {
            return;
        }


        String targetName = findTarget.getName();

        boolean isCall = targetName.equals("__call") == true;
        boolean isCallStatic = targetName.equals("__callStatic") == true;

        if (!isCall && !isCallStatic) {
            return;
        }


        Boolean findStaticMethods = isCallStatic;


        if (!(findTarget instanceof PsiElementUsageTarget)) {
            return;
        }

        PsiElement findTargetElement = ((PsiElementUsageTarget) findTarget).getElement();
        PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(findTargetElement);

        assert scopeForUseOperator != null;

        if (!(findTargetElement.getContext() instanceof PhpClass)) {
            return;
        }


        PhpClass searchClass = (PhpClass) findTargetElement.getContext();


        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
            @Override
            public void run() {
                FindElementRunnable runnable = new FindElementRunnable(project, searchClass, targetName, findStaticMethods);
                ApplicationManager.getApplication().runReadAction(runnable);
            }
        }, "Find magic methods", "Find magic methods");


    }


}
