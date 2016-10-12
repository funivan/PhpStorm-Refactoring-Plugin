package com.funivan.phpstorm.refactoring.FindMagicMethods;

/**
 * @author Ivan Scherbak <dev@funivan.com>
 */
public interface MagicElementVisitor {

    ElementResultCollector getResultCollector();

    void setResultCollector(ElementResultCollector filter);

}
