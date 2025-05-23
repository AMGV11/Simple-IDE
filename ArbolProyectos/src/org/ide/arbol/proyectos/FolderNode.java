/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.arbol.proyectos;

import java.awt.Image;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;
import org.openide.util.ImageUtilities;

public class FolderNode extends AbstractNode {

    private final FileObject folder;

    public FolderNode(FileObject folder) {
        super(new FolderChildren(folder), Lookups.singleton(folder));
        this.folder = folder;
        setDisplayName(folder.getName());
        setName(folder.getName());
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/ide/arbol/proyectos/Package.png"); // Cambia la ruta del icono
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getHtmlDisplayName() {
        return "<b>" + folder.getName() + "</b>";
    }

    // Clase interna que gestiona los hijos del nodo (archivos y carpetas)
    private static class FolderChildren extends Children.Keys<FileObject> {
        private final FileObject folder;

        public FolderChildren(FileObject folder) {
            this.folder = folder;
        }

        @Override
        protected void addNotify() {
            refreshKeys();
        }

        private void refreshKeys() {
            setKeys(folder.getChildren());
        }

        @Override
        protected Node[] createNodes(FileObject key) {
            if (key.isFolder()) {
                return new Node[]{new FolderNode(key)};
            } else {
                return new Node[]{new FileNode(key)}; 
            }
        }
    }
}
