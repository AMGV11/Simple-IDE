package org.ide.arbol.proyectos;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;


public class FileNode extends AbstractNode {

    private final FileObject fileObject;

    private class MyAction extends AbstractAction {

    public MyAction () {
        putValue (NAME, "Do Something");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FileObject obj = getLookup().lookup(FileObject.class);
        JOptionPane.showMessageDialog(null, "Hello from " + obj.getName());
    }

}
    
    public FileNode(FileObject fileObject) {
        super(fileObject.isFolder() 
              ? new Children.Array() // Si es carpeta, puede tener hijos
              : Children.LEAF, // Si es archivo, no tiene hijos
              Lookups.singleton(fileObject));

        this.fileObject = fileObject;
        setDisplayName(fileObject.getNameExt()); // Nombre del archivo/carpeta
        //setIconBaseWithExtension(fileObject.isFolder() ? "org/myorg/icons/folder.png" : "org/myorg/icons/file.png");

        if (fileObject.isFolder()) {
            loadChildren();
        }
    }
    
    @Override
    public Action[] getActions (boolean popup) {
        return new Action[] { new MyAction() };
    }
    

    private void loadChildren() {
        Children.Array children = (Children.Array) getChildren();
        List<Node> nodes;
        nodes = new ArrayList<>();
        
        for (FileObject child : fileObject.getChildren()) {
            nodes.add(new FileNode(child));
        }
        
        children.add(nodes.toArray(new Node[0]));
    }
}