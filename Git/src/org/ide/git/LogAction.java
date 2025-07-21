/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.git;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Git",
        id = "org.ide.git.LogAction"
)
@ActionRegistration(
        displayName = "#CTL_LogAction"
)
@ActionReference(path = "Menu/Git", position = 9002)
@Messages("CTL_LogAction=Git Log")
// Comando GIT Log
public class LogAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        commitGitAction(AuxGit.selectProject());
    }

    private void commitGitAction(Project selectedProject) {
        if (selectedProject != null) {
            FileObject root = selectedProject.getProjectDirectory();
            File projectDir = FileUtil.toFile(root);

            try {
                ProcessBuilder pb = new ProcessBuilder("git", "log"); // Comando para mostrar color en codigo "--color=always"
                pb.directory(projectDir);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                System.out.println("[GIT] Mensaje en consola:");

                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("    " + line);
                }

                BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((line = errReader.readLine()) != null) {
                    System.err.println(line);
                }

                int exitCode = p.waitFor();
                if (exitCode == 0) {
                    System.out.println("[GIT] El comando LOG se ha ejecutado con exito");
                } else {
                    AuxGit.printProcessErrors(p);
                    System.out.println("[GIT] El comando LOG ha tenido un error en su ejecucion, mire el mensaje de consola");
                }

                p.waitFor();
            } catch (IOException | InterruptedException ex) {
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
