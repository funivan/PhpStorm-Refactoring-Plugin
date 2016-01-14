package com.funivan.phpstorm.refactoring.EditUsages.Structures;

import java.util.Comparator;

/**
 * @author  Ivan Scherbak <dev@funivan.com>
 */
public class ReplacesItemsmComparator implements Comparator<ReplaceStructure> {

    @Override
    public int compare(ReplaceStructure o1, ReplaceStructure o2) {
        return (o1.getLine() < o2.getLine()) ? 1 : -1;
    }
}
