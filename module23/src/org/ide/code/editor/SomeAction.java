/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/NetBeansModuleDevelopment-files/contextAction.java to edit this template
 */
package org.ide.code.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.loaders.DataObject;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "File",
        id = "org.ide.code.editor.SomeAction"
)
@ActionRegistration(
        displayName = "#CTL_SomeAction"
)
@ActionReference(path = "Toolbars/File", position = 1550)
@Messages("CTL_SomeAction=GuardarAction")
public final class SomeAction implements ActionListener {

    private final DataObject context;

    public SomeAction(DataObject context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        // TODO use context
    }
}
