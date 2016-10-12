package com.funivan.phpstorm.refactoring.EditUsages;

import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbModePermission;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.usages.UsageView;
import com.intellij.usages.impl.UsageViewImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Ivan Scherbak <dev@funivan.com>
 */
public class CreateCustomPatchFromUsages extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();

        if (project == null) {
            return;
        }
        DataContext dataContext = e.getDataContext();


        UsageView usageView = UsageView.USAGE_VIEW_KEY.getData(dataContext);

        if (!(usageView instanceof UsageViewImpl)) {
            return;
        }

        Set<Usage> usages = usageView.getSelectedUsages();
        if (usages == null || usages.size() == 0) {
            usages = usageView.getUsages();
        }


        VirtualFile baseDir = project.getBaseDir();


        StringBuilder buf = new StringBuilder();

        Map<String, Boolean> processedLines = new HashMap<>();


        final Set<Usage> processUsages = usages;

        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
            @Override
            public void run() {
                DumbService.allowStartingDumbModeInside(DumbModePermission.MAY_START_BACKGROUND, new Runnable() {
                    @Override
                    public void run() {
                        ApplicationManager.getApplication().runWriteAction(new Runnable() {
                            @Override
                            public void run() {
                                Language language = null;

                                for (Usage usage : processUsages) {

                                    if (!(usage instanceof UsageInfo2UsageAdapter)) {
                                        continue;
                                    }


                                    UsageInfo2UsageAdapter usageInfo = (UsageInfo2UsageAdapter) usage;

                                    if (usageInfo.getElement() == null) {
                                        continue;
                                    }
                                    language = usageInfo.getElement().getLanguage();


                                    VirtualFile file = usageInfo.getFile();

                                    int line = usageInfo.getLine();
                                    String path = VfsUtil.getRelativePath(file, baseDir, '/');

                                    String key = path + ":" + line;

                                    if (processedLines.get(key) != null) {
                                        continue;
                                    }

                                    Document fileDocument = FileDocumentManager.getInstance().getDocument(file);

                                    if (fileDocument == null) {
                                        continue;
                                    }
                                    int startOffset = fileDocument.getLineStartOffset(line);
                                    int endOffset = fileDocument.getLineEndOffset(line);

                                    String text = fileDocument.getText(new TextRange(startOffset, endOffset));


                                    buf.append("\n");
                                    buf.append("//file:").append(path).append(':').append(line + 1).append("\n");
                                    buf.append(text).append("\n");
                                    buf.append("\n");


                                    processedLines.put(key, true);
                                }

                                String text = buf.toString();
                                if (language != null && language.getID().equals("PHP")) {
                                    text = "<?php\n" + text;
                                }


                                VirtualFile f = ScratchRootType.getInstance().createScratchFile(project, "scratch", language, text, ScratchFileService.Option.create_new_always);
                                if (f != null) {
                                    FileEditorManager.getInstance(project).openFile(f, true);
                                }
                            }
                        });
                    }
                });
            }
        }, "Create custom patch", "Create custom patch");

//        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
//            @Override
//            public void run() {
//                ApplicationManager.getApplication().runWriteAction(new Runnable() {
//                    @Override
//                    public void run() {
//                        Language language = null;
//
//                        for (Usage usage : usages) {
//
//                            if (!(usage instanceof UsageInfo2UsageAdapter)) {
//                                continue;
//                            }
//
//
//                            UsageInfo2UsageAdapter usageInfo = (UsageInfo2UsageAdapter) usage;
//
//                            language = usageInfo.getElement().getLanguage();
//
//
//                            VirtualFile file = usageInfo.getFile();
//
//                            int line = usageInfo.getLine();
//                            String path = VfsUtil.getRelativePath(file, baseDir, '/');
//
//                            String key = path + ":" + line;
//
//                            if (processedLines.get(key) != null) {
//                                continue;
//                            }
//
//                            Document fileDocument = FileDocumentManager.getInstance().getDocument(file);
//
//
//                            int startOffset = fileDocument.getLineStartOffset(line);
//                            int endOffset = fileDocument.getLineEndOffset(line);
//
//                            String text = fileDocument.getText(new TextRange(startOffset, endOffset));
//
//                            buf.append("\n");
//                            buf.append("//file:" + path + ':' + (line + 1) + "\n");
//                            buf.append(text + "\n");
//                            buf.append("\n");
//
//                            processedLines.put(key, true);
//                        }
//
//                        String text = buf.toString();
//                        if (language != null && language.getID().equals("PHP")) {
//                            text = "<?php\n" + text;
//                        }
//
//
//                        VirtualFile f = ScratchRootType.getInstance().createScratchFile(project, "scratch", language, text, ScratchFileService.Option.create_new_always);
//                        if (f != null) {
//                            FileEditorManager.getInstance(project).openFile(f, true);
//                        }
//
//                    }
//                });
//            }
//        }, "Create custom patch", "Create custom patch");


    }


}