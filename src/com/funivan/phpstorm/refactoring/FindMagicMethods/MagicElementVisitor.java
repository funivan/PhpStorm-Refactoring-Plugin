package com.funivan.phpstorm.refactoring.FindMagicMethods;

/**
 * Created by ivan
 */
public interface MagicElementVisitor {

    ElementResultCollector getResultCollector();

    void setResultCollector(ElementResultCollector filter);

}
