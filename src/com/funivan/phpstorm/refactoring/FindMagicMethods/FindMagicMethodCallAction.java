package com.funivan.phpstorm.refactoring.FindMagicMethods;

import com.funivan.phpstorm.refactoring.FindMagicMethods.Visitors.BaseElementVisitor;
import com.funivan.phpstorm.refactoring.FindMagicMethods.Visitors.FieldReferenceVisitor;
import com.funivan.phpstorm.refactoring.FindMagicMethods.Visitors.MethodReferenceVisitor;
import com.funivan.phpstorm.refactoring.util.PhpIndexUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
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
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Ivan Scherbak <dev@funivan.com>
 */
public class FindMagicMethodCallAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        DataContext dataContext = anActionEvent.getDataContext();

        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project == null) {
            return;
        }

        PsiDocumentManager.getInstance(project).commitAllDocuments();
        final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);

        if (editor == null) {
            return;
        }

        UsageTarget[] usageTargets = anActionEvent.getData(UsageView.USAGE_TARGETS_KEY);

        if (usageTargets == null) {
            return;

        }

        UsageTarget findTarget = usageTargets[0];

        if (findTarget == null) {
            return;
        }


        if (!(findTarget instanceof PsiElementUsageTarget)) {
            return;
        }

        final String targetName = findTarget.getName();

        if (targetName == null) {
            return;
        }

        PsiElement findTargetElement = ((PsiElementUsageTarget) findTarget).getElement();
        PhpPsiElement scopeForUseOperator = PhpCodeInsightUtil.findScopeForUseOperator(findTargetElement);

        assert scopeForUseOperator != null;

        if (!(findTargetElement.getContext() instanceof PhpClass)) {
            return;
        }

        BaseElementVisitor visitor = null;


        boolean isCall = targetName.equals("__call");
        boolean isCallStatic = targetName.equals("__callStatic");
        if (isCall || isCallStatic) {
            visitor = new MethodReferenceVisitor(isCallStatic);
        }

        boolean isGet = targetName.equals("__get");
        boolean isSet = targetName.equals("__set");

        if (isGet || isSet) {
            visitor = new FieldReferenceVisitor(isSet);
        }


        if (visitor == null) {
            return;
        }


        final List<Usage> usages = new ArrayList<>();
        final PhpClass searchClass = (PhpClass) findTargetElement.getContext();
        if (searchClass == null) {
            return;
        }

        final BaseElementVisitor elementVisitor = visitor;

        new Task.Backgroundable(project, "Find magic method call: " + targetName, true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {


                ApplicationManager.getApplication().runReadAction(new Runnable() {

                    @Override
                    public void run() {

                        HashMap<String, PhpClass> searchForClassesFQNs = new HashMap<>();

                        // Find all subclasses
                        String baseClassFqn = searchClass.getFQN();
                        searchForClassesFQNs.put(baseClassFqn, searchClass);
                        PhpIndex phpIndex = PhpIndex.getInstance(searchClass.getProject());

                        Collection<PhpClass> subClasses = phpIndex.getAllSubclasses(baseClassFqn);


                        for (PhpClass el : subClasses) {
                            Method classMethod = PhpIndexUtil.getClassMethod(el, targetName);

                            if (classMethod == null || classMethod.getContainingClass() == null) {
                                continue;
                            }

                            if (classMethod.getContainingClass().equals(searchClass)) {
                                searchForClassesFQNs.put(el.getFQN(), el);
                            }
                        }

                        ElementResultCollector filter = new ElementResultCollector(searchForClassesFQNs, usages);


                        Map<String, Boolean> processedFiles = new HashMap<>();
                        VirtualFile baseDir = project.getBaseDir();

                        GlobalSearchScope projectScope = GlobalSearchScope.projectScope(project);
                        elementVisitor.setResultCollector(filter);


                        for (PhpClass target : searchForClassesFQNs.values()) {
                            indicator.setText2(target.getPresentableFQN());

                            // Find class references
                            Query<PsiReference> search = ReferencesSearch.search(target, projectScope);
                            System.out.println("Size:" + search);
                            for (PsiReference classUsage : search) {

                                //#   find usages inside this files
                                PsiFile containingFile = classUsage.getElement().getContainingFile();
                                VirtualFile file = containingFile.getVirtualFile();


                                String path = VfsUtil.getRelativePath(file, baseDir, '/');
                                if (processedFiles.get(path) != null) {
                                    continue;
                                }

                                processedFiles.put(path, true);
                                containingFile.acceptChildren(elementVisitor);
                            }


                        }

                    }

                });

            }

            @Override
            public void onCancel() {
                super.onCancel();
                usages.clear();
            }

            @Override
            public void onError(@NotNull Exception error) {
                super.onError(error);
                usages.clear();
            }


            @Override
            public void onSuccess() {

                super.onSuccess();

                //@todo show bubble: empty usages
                UsageViewPresentation usageViewPresentation = new UsageViewPresentation();
                usageViewPresentation.setTabText("magic methods " + targetName);
                UsageViewManager.getInstance(project).showUsages(UsageTarget.EMPTY_ARRAY, usages.toArray(new Usage[usages.size()]), usageViewPresentation);

                usages.clear();
            }
        }.queue();


    }


}
