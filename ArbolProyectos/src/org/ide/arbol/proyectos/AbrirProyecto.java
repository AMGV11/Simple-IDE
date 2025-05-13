/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.arbol.proyectos;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

@ActionID(
    category = "File",
    id = "org.mi.ide.acciones.AbrirProyectoAction"
)
@ActionRegistration(
    displayName = "#CTL_AbrirProyecto"
    //iconBase = null //"org/mi/ide/acciones/icono-abrir.png" // opcional, si quieres un icono
)
@ActionReference(
    path = "Toolbars/File", // Aquí defines que va a la barra de herramientas 'File'
    position = 50 // Cambia el orden (10: cortar, 20: copiar, etc.)
)


public class AbrirProyecto extends AbstractAction implements ActionListener {

    public AbrirProyecto() {
        putValue(NAME, "Abrir Proyecto...");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        
        File carpetaPorDefecto = new File("C:\\Users\\anton\\Desktop\\MisProyectos"); // Cambia esto a tu ruta preferida
        chooser.setCurrentDirectory(carpetaPorDefecto);
        chooser.setDialogTitle("Seleccionar directorio del proyecto");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = chooser.getSelectedFile();
            FileObject dirFO = FileUtil.toFileObject(FileUtil.normalizeFile(selectedDir));
            if (dirFO == null) {
                JOptionPane.showMessageDialog(null, "No se pudo acceder al directorio seleccionado.");
                return;
            }

            try {
                ProjectManager pm = ProjectManager.getDefault();
                if (pm.isProject(dirFO)) {
                    Project proyecto = pm.findProject(dirFO);
                    OpenProjects.getDefault().open(new Project[]{proyecto}, false);
                } else {
                    JOptionPane.showMessageDialog(null, "La carpeta seleccionada no es un proyecto válido.");
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
