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

/**
 * Acción que manda el comando "git status" por consola.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
@ActionID(
        category = "Git",
        id = "org.ide.git.StatusAction"
)
@ActionRegistration(
        displayName = "#CTL_StatusAction"
)
@ActionReference(path = "Menu/Git", position = 9014)
@Messages("CTL_StatusAction=Git Status")
public class StatusAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        statusGitAction(AuxGit.selectProject());

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
                    System.out.println("[GIT] El comando Status se ha ejecutado con éxito");
                } else {
                    AuxGit.printProcessErrors(p);
                    System.out.println("[GIT] El comando Status ha tenido un error en su ejecución, mire el mensaje de consola");
                }

                p.waitFor();
            } catch (IOException | InterruptedException ex) {
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
