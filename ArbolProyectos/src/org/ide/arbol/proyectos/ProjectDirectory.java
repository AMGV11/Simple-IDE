package org.ide.arbol.proyectos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.openide.windows.OnShowing;

/**
 * Pregunta por la ruta que queremos elegir como directorio de proyectos y lo
 * guarda en el sistema para recordarlo al abrir el IDE de nuevo.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
@OnShowing
public class ProjectDirectory implements Runnable {

    private static final File CONFIG_DIR = new File(System.getProperty("user.home"), ".Simple-IDE");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "directorioProyectos.txt");

    @Override
    public void run() {
        // Solo ejecutar al inicio si no existe el archivo de configuración
        if (!CONFIG_FILE.exists()) {
            // Usar el método estático de la acción
            ChangeProjectDirectoryAction.selectAndSaveProjectDirectory(false);
        }
    }

    // Recuperar la ruta guardada
    public static File getSavedProjectDirectory() {
        if (CONFIG_FILE.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
                String path = reader.readLine();
                if (path != null && !path.isBlank()) {
                    return new File(path);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
