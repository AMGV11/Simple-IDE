package org.ide.code.debugger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Acción encargada de mandar la opción "StepInto" al componente Debug.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
@ActionID(
        category = "Debug",
        id = "org.ide.code.debugger.DebugStepIntoAction"
)
@ActionRegistration(
        displayName = "#CTL_DebugStepIntoAction"
)
@ActionReference(path = "Menu/Debug", position = 9003)
@Messages("CTL_DebugStepIntoAction=Step Into")
public final class DebugStepIntoAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        DebugLauncher.getInstance().stepInto();
    }
}
