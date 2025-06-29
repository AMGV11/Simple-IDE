/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.code.debugger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(
    category = "Debug",
    id = "org.ide.code.debugger.DebugStepOutAction"
)
@ActionRegistration(
    displayName = "#CTL_DebugStepOutAction"
)
@ActionReference(path = "Menu/Debug", position = 9999)
@NbBundle.Messages("CTL_DebugStepOutAction=Step Out")
public final class DebugStepOutAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        DebugLauncher.getInstance().stepOut();
    }
}
