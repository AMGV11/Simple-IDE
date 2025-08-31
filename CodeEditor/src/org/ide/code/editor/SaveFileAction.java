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
 * Acción encargada de guardar los cambios en los ficheros que se estén editando
 * en el editor de código y encargado de llamar al compilador si son archivos
 * .java
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
@ActionID(
        category = "File",
        id = "org.ide.editor.actions.GuardarArchivoAction"
)
@ActionRegistration(
        iconBase = "org/ide/code/editor/SaveIcon.png",
        displayName = "Guardar"
)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 750), // Icono en menu File
    @ActionReference(path = "Toolbars/File", position = 460), // Icono en Toolbar
    @ActionReference(path = "Shortcuts", name = "C-S") // Atajo con Ctrl + S
})
public class SaveFileAction implements ActionListener {

    private final SaveCookie context;

    public SaveFileAction(SaveCookie context) {
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

                // Comprobamos que el archivo sea .java para llamar al compilador o no
                if (((CodeEditorTopComponent) codeEditor).getCurrentFO().getExt().equals("java")) {
                    IncrementalCompiler.getInstance().compile();
                    return;
                }

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            StatusDisplayer.getDefault().setStatusText("[ERROR] No hay un editor activo.");
        }
    }
}
