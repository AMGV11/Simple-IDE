/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.arbol.proyectos;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.PasteType;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.TopComponent;

public class FolderNode extends AbstractNode {

    private final FileObject folder;

    public FolderNode(FileObject folder) {
        super(new FolderChildren(folder), createLookup(folder));

        this.folder = folder;
        
        
        
    }

    private static Lookup createLookup(FileObject folder) {
        try {
            DataObject dobj = DataObject.find(folder);
            return Lookups.fixed(folder, dobj);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
            return Lookups.singleton(folder);
        }
    }

    public int getAllowedDropActions(Transferable t) {
        return DnDConstants.ACTION_COPY_OR_MOVE;
    }

    public int getAllowedDragActions() {
        return DnDConstants.ACTION_COPY_OR_MOVE;
    }
    
    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/ide/arbol/proyectos/Package.png"); // Cambia la ruta del icono
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        try {
            DataObject draggedDO = NodeTransfer.cookie(t, index, DataObject.class);
            if (draggedDO != null) {
                return new PasteType() {
                    @Override
                    public Transferable paste() throws IOException {
                        FileObject targetFolder = folder;
                        FileObject sourceFO = draggedDO.getPrimaryFile();

                        // Evita sobrescribir archivos existentes
                        String name = sourceFO.getNameExt();
                        FileObject existing = targetFolder.getFileObject(name);
                        if (existing != null) {
                            // Ya existe un archivo con ese nombre
                            return null;
                        }

                        // Mueve el archivo
                        FileLock lock = null;
                        try {
                            lock = sourceFO.lock(); // Necesario para moverlo de forma segura
                            sourceFO.move(lock, targetFolder, sourceFO.getName(), sourceFO.getExt());
                        } finally {
                            if (lock != null) {
                                lock.releaseLock();
                            }
                        }

                        // Refrescar este nodo destino
                        if (getChildren() instanceof FolderNode.FolderChildren folderChildren) {
                            folderChildren.refreshKeys();
                        }

                        FileObject sourceFolder = sourceFO.getParent();
                        Node sourceNode = findNodeForFolder(sourceFolder);
                        if (sourceNode instanceof FolderNode) {
                            ((FolderChildren) sourceNode.getChildren()).refreshKeys();
                        }
                        return null;
                    }
                };
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    @Override
    public String getDisplayName() {
        return "NombrePrueba";
    }
    
        @Override
    public String getName() {
        return "NombrePrueba";
    }

    @Override
    public String getHtmlDisplayName() {
        return "<b>" + folder.getName() + "</b>";
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            new AbstractAction("Borrar") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    borrarArchivo();
                }
            }
        };
    }
    
 

    private Node findNodeForFolder(FileObject folderFO) {
        // Teniendo referencia a la raiz del arbol
        Node aux = this;

        while (aux.getParentNode() != null && !aux.getParentNode().getName().equals("src")) {
            aux = aux.getParentNode();
        }
        Node root = aux; // Nodo raiz del arbol proyecto
        return findNodeRecursively(root, folderFO);
    }

    private Node findNodeRecursively(Node current, FileObject targetFO) {
        if (current == null) {
            System.out.println("Null en findNodeRecursively");
            return null;
        }
        FileObject fo = current.getLookup().lookup(FileObject.class);
        if (fo != null && fo.equals(targetFO)) {
            return current;
        }
        for (Node child : current.getChildren().getNodes(true)) {
            Node result = findNodeRecursively(child, targetFO);
            if (result != null) {
                return result;
            }
        }
        return null;
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
                    //Actualizamos la carpeta en el explorador NO FUNCIONA
                    if (this instanceof FolderNode) {
                        ((FolderNode.FolderChildren) getChildren()).refreshKeys();
                    }
                    if (parentFolder instanceof FolderNode) {
                        ((FolderNode.FolderChildren) parentFolder.getChildren()).refreshKeys();
                    }
                }
                
                refreshProjectExplorer();
            } catch (IOException ex) {
                // Mostrar error al usuario
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message("Error al eliminar el archivo: " + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE)
                );
            }
        }
    }

    // Clase interna que gestiona los hijos del nodo (archivos y carpetas)
    public static class FolderChildren extends Children.Keys<FileObject> {

        private final FileObject folder;

        public FolderChildren(FileObject folder) {
            this.folder = folder;
        }

        @Override
        protected void addNotify() {
            refreshKeys();
        }

        public void refreshKeys() {
            setKeys(folder.getChildren());
        }


        @Override
        protected Node[] createNodes(FileObject key) {
            if (key.isFolder()) {
                return new Node[]{new FolderNode(key)};
            } else {
                try {
                    return new Node[]{new FileNode(key)};
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return null;
        }
    }
    
    private void refreshProjectExplorer() {
        TopComponent.Registry registry = TopComponent.getRegistry();
        for (TopComponent tc : registry.getOpened()) {
            if(tc instanceof ExploradorTopComponent explorer){
                try {
                    explorer.refreshExplorer();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

}
 