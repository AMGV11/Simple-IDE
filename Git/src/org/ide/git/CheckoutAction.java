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
import org.openide.util.NbBundle;

/**
 * Acción que manda el comando "git checkout" por consola.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
@ActionID(
        category = "Git",
        id = "org.ide.git.CheckoutAction"
)
@ActionRegistration(
        displayName = "#CTL_CheckoutAction"
)
@ActionReference(path = "Menu/Git", position = 9010)
@NbBundle.Messages("CTL_CheckoutAction=Git Checkout")
public class CheckoutAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        checkoutGitAction(AuxGit.selectProject());
    }

    private void checkoutGitAction(Project selectedProject) {
        if (selectedProject != null) {
            FileObject root = selectedProject.getProjectDirectory();
            File projectDir = FileUtil.toFile(root);
            String message = textCommitModal();

            if (message != null) {
                try {
                    ProcessBuilder pb;

                    pb = new ProcessBuilder("git", "checkout", message);

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
                        System.out.println("[GIT] El comando Checkout se ha ejecutado con exito");
                    } else {
                        AuxGit.printProcessErrors(p);
                        System.out.println("[GIT] El comando Checkout ha tenido un error en su ejecucion, mire el mensaje de consola");
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

        JTextField textField = new JTextField(20);
        Object[] message = {
            "Introduzca rama a la que cambiar o hash del commit al que volver:",
            textField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Proyectos abiertos", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String inputText = textField.getText();
            return inputText;

        } else {
            System.out.println("[GIT] Operación cancelada.");
            return null;
        }

    }

}
