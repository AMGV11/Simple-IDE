/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.code.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

@ActionID(
    category = "Build",
    id = "org.ide.editor.actions.CompileProjectAction"
)
@ActionRegistration(
    //iconBase = "org/ide/code/editor/CompAndExec.png",
    displayName = "Compilar"
)
@ActionReference(path = "Toolbars/File", position = 600)
public class CompileAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        Compile.getInstance().compile();
    }
}

