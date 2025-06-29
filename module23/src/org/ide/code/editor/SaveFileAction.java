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
import org.openide.awt.ActionReferences;
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
@ActionReferences({
    @ActionReference(path = "Toolbars/File", position = 250), // Icono en Toolbars
    @ActionReference(path = "Shortcuts", name = "C-S") // Atajo con Ctrl + S
})
public class SaveFileAction implements ActionListener {
        
    private final SaveCookie context;
    
    public SaveFileAction(SaveCookie context){
        this.context = context;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // Obtener el editor activo
        TopComponent codeEditor = TopComponent.getRegistry().getActivated();
        context.save();

        if (codeEditor instanceof CodeEditorTopComponent) {
            try {
                ((CodeEditorTopComponent) codeEditor).saveFile();
                String currentFile = ((CodeEditorTopComponent) codeEditor).getCurrentFO().getNameExt();
                ((CodeEditorTopComponent) codeEditor).setState(false);
                System.out.println("[INFO] Archivo " + currentFile + " guardado con exito.");
                Compile.getInstance().compile();
                
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            StatusDisplayer.getDefault().setStatusText("[ERROR] No hay un editor activo.");
        }    
    }
}
