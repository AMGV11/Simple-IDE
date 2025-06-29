/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.code.debugger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.ide.code.debugger.DebugLauncher;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
    category = "Debug",
    id = "org.ide.code.debugger.DebugStepIntoAction"
)
@ActionRegistration(
    displayName = "#CTL_DebugStepIntoAction"
)
@ActionReference(path = "Menu/Debug", position = 9999)
@Messages("CTL_DebugStepIntoAction=Step Into")
public final class DebugStepIntoAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        DebugLauncher.getInstance().stepInto();
    }
}
