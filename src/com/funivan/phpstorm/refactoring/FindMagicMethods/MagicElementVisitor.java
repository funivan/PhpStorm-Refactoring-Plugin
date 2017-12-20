package com.funivan.phpstorm.refactoring.FindMagicMethods;

/**
 * @author Ivan Shcherbak <alotofall@gmail.com>
 */
public interface MagicElementVisitor {

    ElementResultCollector getResultCollector();

    void setResultCollector(ElementResultCollector filter);

}
