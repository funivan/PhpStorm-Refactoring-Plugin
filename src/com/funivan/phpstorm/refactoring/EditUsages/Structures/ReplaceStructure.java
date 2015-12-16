package com.funivan.phpstorm.refactoring.EditUsages.Structures;

/**
 * @author  Ivan Scherbak <dev@funivan.com>
 */
public class ReplaceStructure {
    private String value;
    private int start;
    private int end;

    public ReplaceStructure(String value, int start, int end) {
        this.value = value;
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getValue() {
        return value;
    }
}
