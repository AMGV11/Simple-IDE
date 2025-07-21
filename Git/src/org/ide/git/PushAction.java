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
        id = "org.ide.git.PushAction"
)
@ActionRegistration(
        displayName = "#CTL_PushAction"
)
@ActionReference(path = "Menu/Git/Remote", position = 9020)
@Messages("CTL_PushAction=Git Push")
// Comando remote Git Push
public class PushAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        Project selectedProject = AuxGit.selectProject();
        String remoteUrl = AuxGit.getRemoteUrl(selectedProject);

        if (remoteUrl != null && !remoteUrl.isBlank()) {
            pushGitAction(selectedProject);
        } else {
            System.out.println("[GIT] No se encontro ningun remoto configurado. Configurelo antes de usar PULL. (Configure Remote)");
        }
    }

    private void pushGitAction(Project selectedProject) {
        if (selectedProject != null) {
            FileObject root = selectedProject.getProjectDirectory();
            File projectDir = FileUtil.toFile(root);

            try {
                ProcessBuilder pb = new ProcessBuilder("git", "push");
                pb.directory(projectDir);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                System.out.println("[GIT] Mensaje en consola:");

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }

                int exitCode = p.waitFor();

                if (exitCode == 0) {
                    System.out.println("[GIT] El comando PUSH se ha ejecutado con exito");
                } else {
                    AuxGit.printProcessErrors(p);
                    System.out.println("[GIT] El comando PUSH ha tenido un error en su ejecucion, mire el mensaje de consola");
                }
                p.waitFor();

            } catch (IOException | InterruptedException ex) {
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
