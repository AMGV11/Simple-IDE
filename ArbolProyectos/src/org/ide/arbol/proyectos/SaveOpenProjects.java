/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.arbol.proyectos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.OnStop;
import org.openide.util.Exceptions;

//Clase utilizada para guardar en un archivo txt los proyectos abiertos antes de cerrar el IDE
@OnStop
public class SaveOpenProjects implements Runnable {
    public void run() {
        File file = OpenProjectsRecord.getLastSession();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (Project p : OpenProjects.getDefault().getOpenProjects()) {
                FileObject fo = p.getProjectDirectory();
                File aux = FileUtil.toFile(fo);
                if (aux != null) {
                    writer.write(aux.getAbsolutePath());
                    writer.newLine();
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}  

