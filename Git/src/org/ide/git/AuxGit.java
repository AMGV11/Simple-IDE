/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.git;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JOptionPane;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

// Clase auxiliar para el modulo Git
public class AuxGit {

    public static Project selectProject() {
        Project[] open = OpenProjects.getDefault().getOpenProjects();

        // Devolvemos el unico proyecto abierto sin necesidad de preguntar al usuario
        if (open.length == 1) {
            return open[0];
        }

        String[] names = new String[open.length];
        for (int i = 0; i < open.length; i++) {
            names[i] = open[i].getProjectDirectory().getNameExt();
        }

        System.out.println("ACTIVOS " + open.length + " " + names[0]);

        Object selected = JOptionPane.showInputDialog(null, "Selecciona proyecto:",
                "Proyectos abiertos", JOptionPane.PLAIN_MESSAGE, null, names, names[0]);

        if (selected != null) {
            for (Project p : open) {
                if (p.getProjectDirectory().getNameExt().equals(selected.toString())) {
                    return p;
                }
            }
        }
        return null;
    }

    public static String getRemoteUrl(File projectDir) {
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "remote", "get-url", "origin");
            pb.directory(projectDir);
            Process p = pb.start();
            int exit = p.waitFor();

            if (exit == 0) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                return reader.readLine();
            }
        } catch (IOException | InterruptedException ex) {
            // Si no existe el remote, no hacemos nada
        }
        return null;
    }

    public static String getRemoteUrl(Project project) {
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "remote", "get-url", "origin");
            pb.directory(getProjectDir(project));
            Process p = pb.start();
            int exit = p.waitFor();

            if (exit == 0) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                return reader.readLine();
            }
        } catch (IOException | InterruptedException ex) {
            // Si no existe el remote, no hacemos nada
        }
        return null;
    }

    public static File getProjectDir(Project project) {
        FileObject root = project.getProjectDirectory();
        return FileUtil.toFile(root);
    }

    public static void printProcessErrors(Process p) throws Exception {
        BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line;
        while ((line = err.readLine()) != null) {
            System.out.println(line);
        }
    }

}
