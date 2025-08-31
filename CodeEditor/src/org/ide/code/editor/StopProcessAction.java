package org.ide.code.editor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.ImageUtilities;

/**
 * Acción para parar la ejecución de código actual.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
@ActionID(
        category = "Build",
        id = "org.ide.editor.actions.StopProcessAction"
)
@ActionRegistration(
        displayName = "Parar ejecución",
        lazy = false
)
@ActionReferences({
    @ActionReference(path = "Toolbars/File", position = 700),
    @ActionReference(path = "Menu/Tools", position = 20)
})
public class StopProcessAction extends AbstractAction {

    private static StopProcessAction instance;

    public StopProcessAction() {
        super("Detener Proceso");
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon("org/ide/code/editor/StopIcon.png", false));
        setEnabled(false); // Inicialmente deshabilitado
        instance = this;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CompileRunProjectAction compileAction = CompileRunProjectAction.getInstance();

        if (compileAction != null && compileAction.isProcessRunning()) {
            compileAction.stopCurrentProcess();
            StatusDisplayer.getDefault().setStatusText("Proceso Java detenido por el usuario.");
        } else {
            StatusDisplayer.getDefault().setStatusText("No hay procesos ejecutándose.");
        }
    }

    public static void setButtonEnabled(boolean enabled) {
        if (instance != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                instance.setEnabled(enabled);
            });
        }
    }

    public static StopProcessAction getInstance() {
        return instance;
    }
}
