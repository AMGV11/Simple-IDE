/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.code.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
public class OpenFilesRecord implements Runnable {

    private static final File CONFIG_DIR = new File(System.getProperty("user.home"), ".Simple-IDE");

    @Override
    public void run() {
        File archivo = getFileLastSession();
        if (!archivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String line;
            List<FileObject> abiertos = new ArrayList<>();

            TopComponent.Registry registry = TopComponent.getRegistry();
            Collection<? extends TopComponent> abiertosTC = registry.getOpened();

            while ((line = reader.readLine()) != null) {
                File f = new File(line);
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));

                if (fo == null || !fo.isData()) continue;

                boolean yaEstaAbierto = false;

                for (TopComponent tc : abiertosTC) {
                    if (tc instanceof CodeEditorTopComponent editor) {
                        FileObject actual = editor.getCurrentFO();
                        if (fo.equals(actual)) {
                            yaEstaAbierto = true;
                            break;
                        }
                    }
                }

                if (yaEstaAbierto) continue;

                // Buscar un editor vacío
                boolean cargado = false;
                for (TopComponent tc : abiertosTC) {
                    if (tc instanceof CodeEditorTopComponent editor) {
                        FileObject actual = editor.getCurrentFO();
                        if (actual == null) {
                            editor.loadFile(fo);
                            cargado = true;
                            break;
                        }
                    }
                }

                if (!cargado) {
                    CodeEditorTopComponent nuevo = new CodeEditorTopComponent(fo);
                    nuevo.open();
                    nuevo.requestActive();
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
