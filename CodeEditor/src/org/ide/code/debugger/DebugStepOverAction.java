package org.ide.code.debugger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Acción encargada de mandar la opción "StepOver" al componente Debug.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
@ActionID(
        category = "Debug",
        id = "org.ide.code.debugger.DebugStepOverAction"
)
@ActionRegistration(
        displayName = "#CTL_DebugStepOverAction"
)
@ActionReference(path = "Menu/Debug", position = 9005)
@Messages("CTL_DebugStepOverAction=Step Over")
public final class DebugStepOverAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        DebugLauncher.getInstance().stepOver();
    }
}
