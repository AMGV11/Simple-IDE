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
import javax.swing.JOptionPane;
import javax.swing.JTextField;
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
        id = "org.ide.git.CommitAction"
)
@ActionRegistration(
        displayName = "#CTL_CommitAction"
)
@ActionReference(path = "Menu/Git", position = 9002)
@Messages("CTL_CommitAction=Git Commit")
// Comando remote GIT Commit
public class CommitAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        commitGitAction(AuxGit.selectProject());
    }

    private void commitGitAction(Project selectedProject) {
        if (selectedProject != null) {
            FileObject root = selectedProject.getProjectDirectory();
            File projectDir = FileUtil.toFile(root);
            String message = textCommitModal();

            if (message != null) {

                try {

                    ProcessBuilder pb = new ProcessBuilder("git", "commit", "-m", message);
                    pb.directory(projectDir);
                    pb.redirectErrorStream(true);
                    Process p = pb.start();
                    System.out.println("[GIT] Mensaje en consola:");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                    BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    while ((line = errReader.readLine()) != null) {
                        System.err.println(line);
                    }

                    int exitCode = p.waitFor();
                    if (exitCode == 0) {
                        System.out.println("[GIT] El comando COMMIT se ha ejecutado con exito");
                    } else {
                        AuxGit.printProcessErrors(p);
                        System.out.println("[GIT] El comando COMMIT ha tenido un error en su ejecucion, mire el mensaje de consola");
                    }

                    p.waitFor();
                } catch (IOException | InterruptedException ex) {
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                System.out.println("[GIT] Es necesario que ponga un mensaje de commit. Por favor, intentelo de nuevo.");
            }
        }
    }

    private String textCommitModal() {

        // Crear un campo de texto para la entrada
        JTextField textField = new JTextField(20);

        // Mostrar el cuadro de diálogo con el campo de texto
        Object[] message = {
            "Mensaje para el Commit:",
            textField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Proyectos abiertos", JOptionPane.OK_CANCEL_OPTION);

        // Verificar si el usuario presionó OK
        if (option == JOptionPane.OK_OPTION) {
            String inputText = textField.getText(); // Obtener el texto ingresado
            return inputText;

        } else {
            System.out.println("Operación cancelada.");
            return null;
        }

    }

}
