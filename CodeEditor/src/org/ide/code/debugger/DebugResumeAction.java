package org.ide.code.debugger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Acción encargada de mandar la opción "Resume" al componente Debug.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
@ActionID(
        category = "Debug",
        id = "org.ide.code.debugger.DebugResumeAction"
)
@ActionRegistration(
        displayName = "#CTL_DebugResumeAction"
)
@ActionReference(path = "Menu/Debug", position = 9002)
@NbBundle.Messages("CTL_DebugResumeAction=Resume")
public final class DebugResumeAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        DebugLauncher.getInstance().resume();
    }
}
