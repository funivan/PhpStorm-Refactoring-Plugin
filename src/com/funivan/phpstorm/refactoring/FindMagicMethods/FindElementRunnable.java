package com.funivan.phpstorm.refactoring.FindMagicMethods;

import com.funivan.phpstorm.refactoring.util.PhpIndexUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageTarget;
import com.intellij.usages.UsageViewManager;
import com.intellij.usages.UsageViewPresentation;
import com.intellij.util.Query;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by ivan on 27.01.16.
 */
public class FindElementRunnable implements Runnable {

    private Project project;
    private PhpClass searchClass;
    private String targetName;
    private Boolean findStaticMethods;


    public FindElementRunnable(Project project, PhpClass searchClass, String targetName, Boolean findStaticMethods) {
        this.project = project;
        this.searchClass = searchClass;
        this.targetName = targetName;
        this.findStaticMethods = findStaticMethods;

    }


    @Override

    public void run() {

        HashMap<String, PhpClass> searchForClasses = getSearchClasses(targetName, searchClass);


        final List<Usage> usages = new ArrayList<>();
        Map<String, Boolean> processedFiles = new HashMap<>();

        List<PsiReference> classUsages = getClassesUsages(project, searchForClasses);

        VirtualFile baseDir = project.getBaseDir();

        FindMethodRecursiveElementWalkingVisitor psiElementVisitor = new FindMethodRecursiveElementWalkingVisitor(searchForClasses, usages, findStaticMethods);

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


        UsageViewPresentation usageViewPresentation = new UsageViewPresentation();
        usageViewPresentation.setTabText("magic methods " + targetName);
        UsageViewManager.getInstance(project).showUsages(UsageTarget.EMPTY_ARRAY, usages.toArray(new Usage[usages.size()]), usageViewPresentation);

    }


    @NotNull
    private List<PsiReference> getClassesUsages(Project project, HashMap<String, PhpClass> searchClass) {
        GlobalSearchScope projectScope = GlobalSearchScope.projectScope(project);
        List<PsiReference> items = new ArrayList<>();

        for (PhpClass target : searchClass.values()) {
            Query<PsiReference> search = ReferencesSearch.search(target, projectScope);
            for (PsiReference reference : search) {
                items.add(reference);
            }

        }

        return items;
    }

    @NotNull
    private HashMap<String, PhpClass> getSearchClasses(String targetName, PhpClass targetClass) {
        HashMap<String, PhpClass> searchForClassesFQNs = new HashMap<>();


        String baseClassFqn = targetClass.getFQN();

        searchForClassesFQNs.put(baseClassFqn, targetClass);

        PhpIndex phpIndex = PhpIndex.getInstance(targetClass.getProject());


        Collection<PhpClass> subClasses = phpIndex.getAllSubclasses(baseClassFqn);


        for (PhpClass el : subClasses) {
            Method classMethod = PhpIndexUtil.getClassMethod(el, targetName);

            if (classMethod == null) {
                continue;
            }

            if (classMethod.getContainingClass().equals(targetClass)) {
                searchForClassesFQNs.put(el.getFQN(), el);
            }
        }

        return searchForClassesFQNs;
    }

}
