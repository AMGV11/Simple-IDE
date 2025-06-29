/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.code.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

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

