/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/NetBeansModuleDevelopment-files/actionListener.java to edit this template
 */
package org.ide.code.debugger;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Stack;
import org.ide.code.editor.CodeEditorTopComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@ActionID(
        category = "Debug",
        id = "org.ide.code.debugger.DebugLauncherAction"
)
@ActionRegistration(
        displayName = "#CTL_DebugLauncherAction"
)
@ActionReference(path = "Menu/Debug", position = 9000, separatorAfter = 9001)
@Messages("CTL_DebugLauncherAction=Debug")
public final class DebugLauncherAction implements ActionListener {
    private CodeEditorTopComponent codeEditor;

    @Override
    public void actionPerformed(ActionEvent e) {
        String JavaFileRoot = "";
        TopComponent activeTC = TopComponent.getRegistry().getActivated();

        if (activeTC instanceof CodeEditorTopComponent) {
            CodeEditorTopComponent codeEditor = (CodeEditorTopComponent) activeTC;
            this.codeEditor = codeEditor;
            FileObject currentFile = codeEditor.getCurrentFO();
            JavaFileRoot = findProjectName(FileUtil.toFile(currentFile)) + "." + currentFile.getName();
            System.out.println(JavaFileRoot);
        }
        
        try {
            DebugLauncher.getInstance().launchAndDebug(JavaFileRoot, codeEditor);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalConnectorArgumentsException ex) {
            Exceptions.printStackTrace(ex);
        } catch (VMStartException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (AbsentInformationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IncompatibleThreadStateException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
        
    //Metodo que nos proporciona el directorio del archivo ya formateado
    private String findProjectName(File javaFile) {
        Stack<String> names = new Stack<>();
        String directory;
        
        while (javaFile != null && !javaFile.getName().equals("src")) {
            if (javaFile.isDirectory()){
                names.push(javaFile.getName());
            } 
            javaFile = javaFile.getParentFile();
        }
                
        directory = names.pop();
        
        while(!names.isEmpty()){
            directory = directory + "." + names.pop();
        }
        
        return directory;
    }
}
