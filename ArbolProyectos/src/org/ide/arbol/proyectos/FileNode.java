/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.arbol.proyectos;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.ide.code.editor.CodeEditorTopComponent;
import org.openide.nodes.FilterNode;
import org.openide.util.Exceptions;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

public class FileNode extends FilterNode {

    private final FileObject fileObject;

    public FileNode(FileObject fileObject) {
        super(
            new AbstractNode(Children.LEAF, Lookups.singleton(fileObject)), // Nodo original
            Children.LEAF,
            Lookups.singleton(fileObject)
        );
        setDisplayName(fileObject.getNameExt());
        this.fileObject = fileObject;
    }
    
    //Para ponerle imagen
    /*@Override
    public Image getIcon(int type) {
        // Usa un icono desde el classpath (por ejemplo, dentro de tu paquete de recursos)
        return ImageUtilities.loadImage("org/ide/arbol/proyectos/file-icon.png"); // Cambia esta ruta
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type); // Puedes usar el mismo u otro icono si lo deseas
    }*/
    
    @Override
    public Action getPreferredAction() {
        return new AbstractAction("Abrir en mi editor") {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirEditor();
            }
        };
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            new AbstractAction("Abrir en el editor") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    abrirEditor();
                }
            },
            // Aquí puedes agregar más acciones si quieres
        };
    }

    private void abrirEditor() {
        TopComponent.Registry registry = TopComponent.getRegistry();
        
        for (TopComponent tc : registry.getOpened()) {

            //Intentamos reutilizar un editor vacio 
                if(tc instanceof CodeEditorTopComponent editor){
                    FileObject file = editor.getCurrentFO();
                    if(file == null){
                        try {
                            editor.loadFile(fileObject);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        editor.requestFocus();
                        return;
                    }
                }
        }
        //Abrimos un nuevo editor si no se reutiliza ninguno
        newCodeEditor(fileObject);
    }
    
    private void newCodeEditor(FileObject fileObject){
        try {
            CodeEditorTopComponent newEditor = new CodeEditorTopComponent(fileObject);
            newEditor.open();
            newEditor.requestActive();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
    

