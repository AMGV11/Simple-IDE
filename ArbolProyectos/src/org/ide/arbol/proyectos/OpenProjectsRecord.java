/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.arbol.proyectos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.windows.OnShowing;

// Esta clase la usaremos para guardar los proyectos abiertos antes de cerrar el IDE y cargarlos al iniciar el IDE
@OnShowing
public class OpenProjectsRecord implements Runnable {
    private static final File CONFIG_DIR = new File(System.getProperty("user.home"), ".Simple-IDE");
    
    public void run() {
        File archivo = getLastSession();
        if (!archivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            List<Project> proyectosAReabrir = new ArrayList<>();
            String linea;
            while ((linea = reader.readLine()) != null) {
                File f = new File(linea);
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
                if (fo != null && ProjectManager.getDefault().isProject(fo)) {
                    Project p = ProjectManager.getDefault().findProject(fo);
                    if (p != null) {
                        proyectosAReabrir.add(p);
                    }
                }
            }
            if (!proyectosAReabrir.isEmpty()) {
                OpenProjects.getDefault().open(proyectosAReabrir.toArray(new Project[0]), false);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static File getLastSession() {
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        return new File(CONFIG_DIR, "proyectos_abiertos.txt");
    }
}

 
    
   

