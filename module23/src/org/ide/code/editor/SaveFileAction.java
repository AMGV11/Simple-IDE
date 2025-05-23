/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.code.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent; 

/**
 *
 * @author anton
 */
@ActionID(
    category = "File",
    id = "org.ide.editor.actions.GuardarArchivoAction"
)
@ActionRegistration(
    iconBase = "org/ide/code/editor/Save.png",
    displayName = "Guardar"
)
@ActionReference(path = "Toolbars/File", position = 250)
public class SaveFileAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        // Obtener el editor activo
        TopComponent codeEditor = TopComponent.getRegistry().getActivated();

        if (codeEditor instanceof CodeEditorTopComponent) {
            try {
                ((CodeEditorTopComponent) codeEditor).saveFile(); // implementa este método
                String currentFile = ((CodeEditorTopComponent) codeEditor).getCurrentFO().getNameExt();
                ((CodeEditorTopComponent) codeEditor).setState(false);
                System.out.println("--- IDE --- Archivo " + currentFile + " guardado con exito.");
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            StatusDisplayer.getDefault().setStatusText("No hay un editor activo.");
        }    }
    
}
