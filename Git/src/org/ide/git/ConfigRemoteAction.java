/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.git;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Git",
        id = "org.ide.git.ConfigRemoteAction"
)
@ActionRegistration(
        displayName = "#CTL_ConfigRemoteAction"
)
@ActionReference(path = "Menu/Git/Remote", position = 9018)
@Messages("CTL_ConfigRemoteAction=Config Remote")
// Configuracion del remoto en GIT
public class ConfigRemoteAction implements ActionListener{

    @Override
    public void actionPerformed(ActionEvent e) {
        File projectDir = AuxGit.getProjectDir(AuxGit.selectProject());        
        configureRemote(projectDir);
    }
    
    public void configureRemote(File projectDir) {
        try {
            String currentUrl = AuxGit.getRemoteUrl(projectDir);

            if (currentUrl != null && !currentUrl.isBlank()) {
                int res = JOptionPane.showConfirmDialog(null,
                        "Configuracion de remote actual:\n" + currentUrl + "\n\n¿Quieres cambiarlo?",
                        "Configuracion de remote existente", JOptionPane.YES_NO_OPTION);

                if (res != JOptionPane.YES_OPTION) {
                    System.out.println("Configuración de remote conservada: " + currentUrl);
                    return;
                }
            }

            String newUrl = JOptionPane.showInputDialog(null,
                    "Introduce la URL del repositorio remoto (GitHub, GitLab, etc):",
                    "Configurar remote", JOptionPane.PLAIN_MESSAGE);

            if (newUrl == null || newUrl.isBlank()) {
                System.out.println("[GIT] Operacion cancelada. No se configuro el remote.");
                return;
            }

            List<String> command = new ArrayList<>();
            if (currentUrl != null && !currentUrl.isBlank()) {
                command.add("git");
                command.add("remote");
                command.add("set-url");
                command.add("origin");
                command.add(newUrl);
            } else {
                command.add("git");
                command.add("remote");
                command.add("add");
                command.add("origin");
                command.add(newUrl);
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(projectDir);
            Process p = pb.start();
            int exit = p.waitFor();

            if (exit == 0) {
                System.out.println("[GIT] Remote se ha configurado con exito");
            } else {
                AuxGit.printProcessErrors(p);
                System.out.println("[GIT] La configuracion de remote ha tenido un fallo en su ejecucion");
            }

        } catch (Exception ex) {
        }
    }
}
