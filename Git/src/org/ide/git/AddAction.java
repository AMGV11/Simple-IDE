/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.git;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
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
        id = "org.ide.git.AddAction"
)
@ActionRegistration(
        displayName = "#CTL_AddAction"
)
@ActionReference(path = "Menu/Git", position = 9001)
@Messages("CTL_AddAction=Git Add")
// Comando GIT Add
public class AddAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
            addGitAction(AuxGit.selectProject());
    }
    
    private void addGitAction(Project selectedProject) {
        if(selectedProject!=null){
            FileObject root = selectedProject.getProjectDirectory();
            File projectDir = FileUtil.toFile(root);

            try {
                ProcessBuilder pb = new ProcessBuilder("git", "add", ".");
                pb.directory(projectDir);
                pb.redirectErrorStream(true);
                Process p = pb.start();

                int exitCode = p.waitFor();
                if (exitCode==0){
                    System.out.println("[GIT] El comando ADD se ha ejecutado con exito");
                } else {
                    AuxGit.printProcessErrors(p);
                    System.out.println("[GIT] El comando ADD ha tenido un error en su ejecucion, mire el mensaje de consola");
                }

                p.waitFor();
            } catch (IOException | InterruptedException ex) {
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
}
