/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.arbol.proyectos;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.ide.code.editor.CodeEditorTopComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.FilterNode;
import org.openide.util.Exceptions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

public class FileNode extends FilterNode {

    private final FileObject fileObject;

    public FileNode(FileObject fileObject) throws DataObjectNotFoundException {
            super(
        new AbstractNode(Children.LEAF, Lookups.fixed(
            fileObject,
            DataObject.find(fileObject)  
        )),
        Children.LEAF,
        Lookups.fixed(
            fileObject,
            DataObject.find(fileObject)
        )
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
            new AbstractAction("Abrir") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    abrirEditor();
                }
            },
                new AbstractAction("Borrar") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    borrarArchivo();
                }
            }
        };
    }

    private void abrirEditor() {
        TopComponent.Registry registry = TopComponent.getRegistry();
        
        for (TopComponent tc : registry.getOpened()) {

            //Intentamos reutilizar un editor vacio 
                if(tc instanceof CodeEditorTopComponent editor){
                    FileObject file = editor.getCurrentFO();
                    
                    if(file.equals(fileObject)){
                        System.out.println("Ya esta abierto el archivo.");
                        return;
                    }
                }
        }
        //Abrimos un nuevo editor si no se reutiliza ninguno
        newCodeEditor(fileObject);
    }
    
    private void borrarArchivo() {
    DataObject dob = getLookup().lookup(DataObject.class);
    Node parentFolder = this.getParentNode();
    if (dob != null) {
        try {
            boolean confirmado = DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation(
                    "¿Seguro que quieres eliminar el archivo " + dob.getPrimaryFile().getNameExt() + "?",
                    NotifyDescriptor.YES_NO_OPTION)
            ) == NotifyDescriptor.YES_OPTION;

            if (confirmado) {
                dob.delete();
                // Opcional: refrescar el nodo padre para actualizar el árbol
                // Esto suele pasar automáticamente pero depende del Children que uses
            }
        } catch (IOException ex) {
            // Mostrar error al usuario
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message("Error al eliminar el archivo: " + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE)
            );
        }
    }
    //Actualizamos la carpeta en el explorador
    if (parentFolder instanceof FolderNode) {
        ((FolderNode.FolderChildren) parentFolder.getChildren()).refreshKeys();
    }
    
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
    

