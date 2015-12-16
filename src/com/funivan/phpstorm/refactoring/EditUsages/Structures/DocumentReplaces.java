package com.funivan.phpstorm.refactoring.EditUsages.Structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.intellij.openapi.editor.Document;

/**
 * @author Ivan Scherbak <dev@funivan.com>
 */
public class DocumentReplaces {

    private Document document;
    private List<ReplaceStructure> replaces = new ArrayList<ReplaceStructure>();

    public DocumentReplaces(Document document) {
        this.document = document;
    }


    public void add(ReplaceStructure replaceStructure) {
        replaces.add(replaceStructure);
    }

    public List<ReplaceStructure> getReplaces() {
        return replaces;
    }

    public Document getDocument() {
        return document;
    }
}
