/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.arbol.proyectos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.openide.windows.OnShowing;

@OnShowing
public class ProjectDirectory implements Runnable {
    
    private static final File CONFIG_DIR = new File(System.getProperty("user.home"), ".Simple-IDE");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "directorioProyectos.txt");

    // Mostrar selector y guardar la ruta elegida
    @Override
    public void run() {
        
        if (!CONFIG_FILE.exists()){
            
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Selecciona la carpeta base para tus proyectos");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int resultado = chooser.showOpenDialog(null);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                File folder = chooser.getSelectedFile();

                if (!CONFIG_DIR.exists()) {
                    CONFIG_DIR.mkdirs();
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE, false))) {
                    writer.write(folder.getAbsolutePath());
                    JOptionPane.showMessageDialog(null, "Ruta guardada correctamente:\n" + folder.getAbsolutePath());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error al guardar la ruta: " + ex.getMessage());
                }
            }
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

