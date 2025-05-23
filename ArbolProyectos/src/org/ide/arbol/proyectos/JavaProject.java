/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.arbol.proyectos;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class JavaProject implements Project {

    private final FileObject projectDir;
    private final ProjectState state;
    private Lookup lkp;
    
    JavaProject(FileObject dir, ProjectState state) {
        this.projectDir = dir;
        this.state = state;
    }
    
    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }

    @Override
    public Lookup getLookup() {
        if (lkp == null) {
            lkp = Lookups.fixed(new Object[]{
                
                new Info(),
                new JavaProjectLogicalView(this),
                
            });
        }
        
        return lkp;
    }
    
    private final class Info implements  ProjectInformation {

    @StaticResource()
        public static final String JAVA_ICON = "org/ide/arbol/proyectos/Project.png";

        @Override
        public Icon getIcon() {
            return new ImageIcon(ImageUtilities.loadImage(JAVA_ICON));
        }

        @Override
        public String getName() {
            return getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public Project getProject() {
            return JavaProject.this;
        }

    }
    
    class JavaProjectLogicalView implements  LogicalViewProvider {

@StaticResource()
       public static final String JAVA_ICON = "org/ide/arbol/proyectos/Project.png";

    private final JavaProject project;

    public JavaProjectLogicalView(JavaProject project) {
        this.project = project;
    }

    @Override
    public Node createLogicalView() {
        try {
            //Obtain the project directory's node:
            FileObject projectDirectory = project.getProjectDirectory();
            DataFolder projectFolder = DataFolder.findFolder(projectDirectory);
            Node nodeOfProjectFolder = projectFolder.getNodeDelegate();
            //Decorate the project directory's node:
            return new ProjectNode(nodeOfProjectFolder, project);
        } catch (DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
            //Fallback-the directory couldn't be created -
            //read-only filesystem or something evil happened
            return new AbstractNode(Children.LEAF);
        }
    }

    private final class ProjectNode extends FilterNode {

        final JavaProject project;

        public ProjectNode(Node node, JavaProject project)
            throws DataObjectNotFoundException {
            super(node,
                    new FilterNode.Children(node),
                    new ProxyLookup(
                    new Lookup[]{
                        Lookups.singleton(project),
                        node.getLookup()
                    }));
            this.project = project;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {
                ProjectActions.newFile(project),
                ProjectActions.delete(project),
                ProjectActions.close(project)
            };
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(JAVA_ICON);
            
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getDisplayName() {
            return project.getProjectDirectory().getName();
        }

    }

    @Override
    public Node findPath(Node root, Object target) {
        //leave unimplemented for now
        return null;
    }

    }
    
    public class ProjectActions {

    public static Action newFile(Project project) {
        return new AbstractAction("Nuevo archivo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Lógica para crear nuevo archivo
                // Abrir New Archive Wizard
            }
        };
    }

    public static Action delete(Project project) {
        return new AbstractAction("Eliminar proyecto") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Lógica para eliminar proyecto
                
            }
        };
    }

    public static Action close(Project project) {
        return new AbstractAction("Cerrar proyecto") {
            @Override
            public void actionPerformed(ActionEvent e) {
                OpenProjects.getDefault().close(new Project[]{project});
                StatusDisplayer.getDefault().setStatusText("Proyecto cerrado");
            }
        };
    }
}
    
}
