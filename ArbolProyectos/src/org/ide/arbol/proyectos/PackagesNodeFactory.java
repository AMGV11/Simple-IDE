package org.ide.arbol.proyectos;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

@NodeFactory.Registration(projectType = "org-java-project", position = 100)
public class PackagesNodeFactory implements NodeFactory {

    @Override
    public NodeList<?> createNodes(Project project) {
        return new LogicalPackagesNodeList(project);
    }

    private static class LogicalPackagesNodeList implements NodeList<Node> {

        private final Project project;

        public LogicalPackagesNodeList(Project project) {
            this.project = project;
        }

        @Override
        public List<Node> keys() {
            FileObject srcFolder = project.getProjectDirectory().getFileObject("src");
            List<Node> result = new ArrayList<>();

            if (srcFolder != null) {
                // Busca recursivamente las carpetas que vamos a mostrar en el arbol
                List<FileObject> leafFolders = getProjectFolders(srcFolder);

                for (FileObject leafFolder : leafFolders) {
                    try {
                        result.add(DataObject.find(leafFolder).getNodeDelegate());
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

            return result;
        }

        @Override
        public Node node(Node node) {
            return new FilterNode(node);
        }

        @Override
        public void addNotify() {
        }

        @Override
        public void removeNotify() {
        }

        @Override
        public void addChangeListener(ChangeListener cl) {
        }

        @Override
        public void removeChangeListener(ChangeListener cl) {
        }
    }
    
    // Método auxiliar para encontrar carpetas hoja con .java
    private static List<FileObject> getProjectFolders(FileObject folder) {
        List<FileObject> result = new ArrayList<>();
        boolean hasJavaFiles = false;
        boolean hasSubfolders = false;

        for (FileObject child : folder.getChildren()) {
            if (child.isFolder()) {
                hasSubfolders = true;
                result.addAll(getProjectFolders(child));
            } else if ("java".equalsIgnoreCase(child.getExt())) {
                hasJavaFiles = true;
            }
        }

        if (hasJavaFiles) {
            result.add(folder);
        } else if (!hasSubfolders){
            result.add(folder);
        }

        return result;
    }
}
