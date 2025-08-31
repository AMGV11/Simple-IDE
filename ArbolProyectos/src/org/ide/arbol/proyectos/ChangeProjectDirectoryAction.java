package org.ide.arbol.proyectos;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

/**
 * Acción encargada de cambiar el directorio de proyectos
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
@ActionID(
        category = "Tools",
        id = "org.ide.arbol.proyectos.ChangeProjectDirectoryAction"
)
@ActionRegistration(
        displayName = "Cambiar Directorio de Proyectos"
)
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 201)
})
public class ChangeProjectDirectoryAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        // Mostrar directorio actual si existe
        File currentDir = ProjectDirectory.getSavedProjectDirectory();
        String currentPath = (currentDir != null) ? currentDir.getAbsolutePath() : "No configurado";

        int option = JOptionPane.showConfirmDialog(
                null,
                "Directorio actual de proyectos:\n" + currentPath + "\n\n¿Desea cambiarlo?",
                "Cambiar Directorio de Proyectos",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            selectAndSaveProjectDirectory(true);
        }
    }

    // Método estático que puede ser llamado desde ProjectDirectory y desde la acción
    public static void selectAndSaveProjectDirectory(boolean forceSelection) {
        File currentDir = ProjectDirectory.getSavedProjectDirectory();

        // Si no hay directorio guardado o se fuerza la selección
        if (currentDir == null || forceSelection) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Selecciona la carpeta base para tus proyectos");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            // Si hay un directorio previo, empezar desde ahí
            if (currentDir != null) {
                chooser.setCurrentDirectory(currentDir);
            }

            int resultado = chooser.showOpenDialog(null);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                File folder = chooser.getSelectedFile();

                // Crear directorio de configuración si no existe
                File configDir = new File(System.getProperty("user.home"), ".Simple-IDE");
                if (!configDir.exists()) {
                    configDir.mkdirs();
                }

                // Guardar la nueva ruta
                File configFile = new File(configDir, "directorioProyectos.txt");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile, false))) {
                    writer.write(folder.getAbsolutePath());
                    JOptionPane.showMessageDialog(
                            null,
                            "Ruta guardada correctamente:\n" + folder.getAbsolutePath(),
                            "Directorio Actualizado",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Error al guardar la ruta: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }
}
