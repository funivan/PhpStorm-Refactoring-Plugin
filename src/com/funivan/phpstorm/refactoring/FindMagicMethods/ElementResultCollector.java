package com.funivan.phpstorm.refactoring.FindMagicMethods;

import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ivan
 */
public class ElementResultCollector {
    private final HashMap<String, PhpClass> searchClassFQN;
    private final List<Usage> usages;

    public ElementResultCollector(HashMap<String, PhpClass> searchClassFQN, List<Usage> usages) {
        this.searchClassFQN = searchClassFQN;
        this.usages = usages;
    }

    public void add(PsiElement element, String classFqn) {

        if (searchClassFQN.get(classFqn) == null) {
            return;
        }
        final UsageInfo usageInfo = new UsageInfo(element);
        Usage usage = new UsageInfo2UsageAdapter(usageInfo);
        usages.add(usage);
    }

}
