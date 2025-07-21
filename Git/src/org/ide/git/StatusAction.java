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
        id = "org.ide.git.StatusAction"
)
@ActionRegistration(
        displayName = "#CTL_StatusAction"
)
@ActionReference(path = "Menu/Git", position = 9009)
@Messages("CTL_StatusAction=Git Status")
// Comando Git Status
public class StatusAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        statusGitAction(AuxGit.selectProject());

        // Para implementar el cambio en el arbol
        /*TopComponent explorer = WindowManager.getDefault().findTopComponent("ExploradorTopComponent");
            if (explorer instanceof ExploradorTopComponent) {
            ExploradorTopComponent ex = (ExploradorTopComponent) explorer;
            ex.getList();      // O acceder a tu Map si tienes getter
            }*/
    }

    private void statusGitAction(Project selectedProject) {
        if (selectedProject != null) {
            FileObject root = selectedProject.getProjectDirectory();
            File projectDir = FileUtil.toFile(root);

            try {
                ProcessBuilder pb = new ProcessBuilder("git", "status");
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
                    System.out.println("[GIT] El comando Status se ha ejecutado con exito");
                } else {
                    AuxGit.printProcessErrors(p);
                    System.out.println("[GIT] El comando Status ha tenido un error en su ejecucion, mire el mensaje de consola");
                }

                p.waitFor();
            } catch (IOException | InterruptedException ex) {
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
