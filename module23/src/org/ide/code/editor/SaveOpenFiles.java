/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.code.editor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.OnStop;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

/**
 *
 * @author anton
 */
@OnStop
public class SaveOpenFiles implements Runnable {

    public void run() {
        File file = OpenFilesRecord.getFileLastSession();
        TopComponent.Registry registry = TopComponent.getRegistry();
        List<FileObject> openFiles = new ArrayList<>();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (TopComponent tc : registry.getOpened()) {
                if (tc instanceof CodeEditorTopComponent editor) {
                    FileObject fo = editor.getCurrentFO();
                    if (fo != null) {
                        File aux = FileUtil.toFile(fo);
                        writer.write(aux.getAbsolutePath());
                        writer.newLine();
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
