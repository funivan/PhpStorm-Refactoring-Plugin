package com.funivan.phpstorm.refactoring.FindMagicMethods;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.*;
import com.intellij.util.Query;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;

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


        if (findTarget.getName().equals("__call") == false) {
            return;
        }

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

            containingFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {

                public void visitPhpMethodReference(MethodReference reference) {

                    MethodReference methodRef = reference;
                    PsiElement resolve = methodRef.resolve();

                    if ((resolve instanceof Method)) {
                        // our method cant be resolved and thats why it is magic)
                        return;
                    }
                    PhpType type = methodRef.getType();

                    System.out.println(type);

                    if (type == null) {
                        return;
                    }


                    String classFqn = type.toString().replaceAll("^#M#C(.+)\\." + methodRef.getName() + ".+$", "$1");


                    if (!searchClassFQN.equals(classFqn)) {
                        return;
                    }


                    System.out.println("");
                    System.out.println("type");
                    System.out.println("text:" + methodRef.getText());
                    System.out.println("name: " + methodRef.getName());
                    System.out.println("fqn: " + classFqn);


                    final UsageInfo usageInfo = new UsageInfo(reference.getElement());
                    Usage usage = new UsageInfo2UsageAdapter(usageInfo);
                    usages.add(usage);

                }

                @Override
                public void visitElement(PsiElement element) {

                    if (element instanceof MethodReference) {
                        visitPhpMethodReference((MethodReference) element);
                    }

                    super.visitElement(element);

                }

            });

        }


        UsageViewManager.getInstance(project).showUsages(UsageTarget.EMPTY_ARRAY, usages.toArray(new Usage[usages.size()]), new UsageViewPresentation());

        System.out.println(usages.size());

    }


}
