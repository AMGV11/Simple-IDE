package org.ide.code.debugger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Acción encargada de mandar la opción "StepOut" al componente Debug.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
@ActionID(
        category = "Debug",
        id = "org.ide.code.debugger.DebugStepOutAction"
)
@ActionRegistration(
        displayName = "#CTL_DebugStepOutAction"
)
@ActionReference(path = "Menu/Debug", position = 9004)
@NbBundle.Messages("CTL_DebugStepOutAction=Step Out")
public final class DebugStepOutAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        DebugLauncher.getInstance().stepOut();
    }
}
