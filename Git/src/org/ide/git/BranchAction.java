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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

@ActionID(
        category = "Git",
        id = "org.ide.git.BranchAction"
)
@ActionRegistration(
        displayName = "#CTL_BranchAction"
)
@ActionReference(path = "Menu/Git", position = 9001)
@NbBundle.Messages("CTL_BranchAction=Git Branch")
// Comando GIT Branch
public class BranchAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        handleGitBranch(AuxGit.selectProject());
    }

    private void branchGitAction(Project selectedProject, String... args) {
        if (selectedProject != null) {
            FileObject root = selectedProject.getProjectDirectory();
            File projectDir = FileUtil.toFile(root);

            try {
                ProcessBuilder pb = new ProcessBuilder();
                List<String> commands = new ArrayList<>();
                commands.add("git");
                commands.addAll(Arrays.asList(args));

                pb.command(commands);
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
                    System.out.println("[GIT] El comando BRANCH se ha ejecutado con exito");
                } else {
                    AuxGit.printProcessErrors(p);
                    System.out.println("[GIT] El comando BRANCH ha tenido un error en su ejecucion, mire el mensaje de consola");
                }

                p.waitFor();
            } catch (IOException | InterruptedException ex) {
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void handleGitBranch(Project project) {
        String[] options = {"Ver ramas", "Crear rama", "Eliminar rama"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "¿Qué operación quieres realizar?",
                "Git Branch",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        switch (choice) {
            case 0:
                showBranches(project);
                break;
            case 1:
                createBranch(project);
                break;
            case 2:
                deleteBranch(project);
                break;
            default:
                System.out.println("Operación cancelada");
        }
    }

    private void showBranches(Project project) {
        branchGitAction(project, "branch");
    }

    private void createBranch(Project project) {
        String branchName = JOptionPane.showInputDialog("Nombre de la nueva rama:");
        if (branchName != null && !branchName.trim().isEmpty()) {
            branchGitAction(project, "branch", branchName.trim());
        } else {
            System.out.println("Nombre inválido o cancelado");
        }
    }

    private void deleteBranch(Project project) {
        String branchName = JOptionPane.showInputDialog("Nombre de la rama a eliminar:");
        if (branchName != null && !branchName.trim().isEmpty()) {
            branchGitAction(project, "branch", "-d", branchName.trim());
        } else {
            System.out.println("Operación cancelada");
        }
    }

}
