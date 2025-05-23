/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.code.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.windows.OnShowing;
import org.openide.windows.TopComponent;

/**
 *
 * @author anton
 */
@OnShowing
public class OpenFilesRecord implements Runnable{

    private static final File CONFIG_DIR = new File(System.getProperty("user.home"), ".Simple-IDE");
    
    @Override
    public void run() {
        File archivo = getFileLastSession();
        TopComponent.Registry registry = TopComponent.getRegistry();
        if (!archivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                File f = new File(line);
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
                
                if (fo != null && fo.isData()) {
                    
                    for (TopComponent tc : registry.getOpened()) {
                        
                        if(tc instanceof CodeEditorTopComponent editor){
                            FileObject file = editor.getCurrentFO();
                            
                            if(file == null){
                                editor.loadFile(fo);
                                
                            } else {
                                CodeEditorTopComponent newEditor = new CodeEditorTopComponent(fo);
                                newEditor.open();
                                newEditor.requestActive();
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
        
    public static File getFileLastSession(){
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        return new File(CONFIG_DIR, "archivos_abiertos.txt");
    }
}
