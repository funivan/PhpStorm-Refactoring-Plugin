package com.funivan.phpstorm.refactoring.FindMagicMethods;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.usages.*;
import com.intellij.util.Query;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ivan on 20.01.16.
 */
public class FindMagicMethods extends AnAction {
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
        String searchClassFQN = searchClass.getFQN();


        final List<Usage> usages = new ArrayList<>();

        Map<String, Boolean> processedFiles = new HashMap<>();


        GlobalSearchScope projectScope = GlobalSearchScope.projectScope(project);

        Query<PsiReference> classUsages = ReferencesSearch.search(searchClass, projectScope);

        VirtualFile baseDir = project.getBaseDir();

        FindMethodRecursiveElementWalkingVisitor psiElementVisitor = new FindMethodRecursiveElementWalkingVisitor(searchClassFQN, usages, findStaticMethods);

        for (PsiReference classUsage : classUsages) {

            // find usages inside this files
            PsiFile containingFile = classUsage.getElement().getContainingFile();
            VirtualFile file = containingFile.getVirtualFile();

            String path = VfsUtil.getRelativePath(file, baseDir, '/');
            if (processedFiles.get(path) != null) {
                continue;
            }

            processedFiles.put(path, true);
            System.out.println(path);

            containingFile.acceptChildren(psiElementVisitor);

        }


        UsageViewManager.getInstance(project).showUsages(UsageTarget.EMPTY_ARRAY, usages.toArray(new Usage[usages.size()]), new UsageViewPresentation());

        System.out.println(usages.size());

    }


}
