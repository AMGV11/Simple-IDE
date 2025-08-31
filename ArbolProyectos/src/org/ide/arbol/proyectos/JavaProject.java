package org.ide.arbol.proyectos;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
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
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
import org.openide.windows.TopComponent;

/**
 * Define el nodo proyecto Java, usado para representar los proyectos. Declara
 * el conjunto de acciones que se pueden hacer estos nodos.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
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
                this,
                new Info(),
                new JavaProjectLogicalView(this),});
        }

        return lkp;
    }

    private final class Info implements ProjectInformation {

        @StaticResource()
        public static final String JAVA_ICON = "org/ide/arbol/proyectos/ProjectIcon.png";

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
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pcl) {
        }

        @Override
        public Project getProject() {
            return JavaProject.this;
        }

    }

    class JavaProjectLogicalView implements LogicalViewProvider {

        @StaticResource()
        public static final String JAVA_ICON = "org/ide/arbol/proyectos/ProjectIcon.png";

        private final JavaProject project;

        public JavaProjectLogicalView(JavaProject project) {
            this.project = project;
        }

        @Override
        public Node createLogicalView() {
            try {
                // Obtiene el directorio del proyecto
                FileObject projectDirectory = project.getProjectDirectory();
                DataFolder projectFolder = DataFolder.findFolder(projectDirectory);
                Node nodeOfProjectFolder = projectFolder.getNodeDelegate();
                return new ProjectNode(nodeOfProjectFolder, project);
            } catch (DataObjectNotFoundException donfe) {
                Exceptions.printStackTrace(donfe);

                return new AbstractNode(Children.LEAF);
            }
        }

        private final class ProjectNode extends FilterNode {

            final JavaProject project;

            public ProjectNode(Node node, JavaProject project)
                    throws DataObjectNotFoundException {
                super(node,
                        NodeFactorySupport.createCompositeChildren(
                                project,
                                "Projects/org-java-project/Nodes"),
                        new ProxyLookup(
                                new Lookup[]{
                                    Lookups.singleton(project),
                                    node.getLookup()
                                }));
                this.project = project;
            }

            @Override
            public Action[] getActions(boolean context) {
                return new Action[]{
                    ProjectActions.rename(project),
                    ProjectActions.delete(project),
                    ProjectActions.close(project)
                };
            }

            @Override
            public Image getIcon(int type) {
                return ImageUtilities.loadImage(JAVA_ICON);

            }

            @Override
            public boolean canRename() {
                return false;
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
            return null;
        }

    }

    public class ProjectActions {

        public static Action delete(Project project) {
            return new AbstractAction("Eliminar proyecto") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (project instanceof JavaProject javaProject) {
                        NotifyDescriptor.Confirmation confirm = new NotifyDescriptor.Confirmation(
                                "¿Estás seguro de que deseas eliminar el proyecto '"
                                + javaProject.getProjectDirectory().getNameExt() + "'?",
                                "Confirmar eliminación",
                                NotifyDescriptor.YES_NO_OPTION
                        );
                        Object result = DialogDisplayer.getDefault().notify(confirm);

                        if (result == NotifyDescriptor.YES_OPTION) {
                            javaProject.delete();
                            refreshProjectExplorer();
                        }
                    }
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

        public static Action rename(Project project) {
            return new AbstractAction("Renombrar") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (project instanceof JavaProject javaProject) {
                        String currentName = javaProject.getProjectDirectory().getName();

                        // Mostrar diálogo para pedir el nuevo nombre
                        NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine(
                                "Nuevo nombre del proyecto:",
                                "Renombrar Proyecto"
                        );
                        input.setInputText(currentName);

                        Object result = DialogDisplayer.getDefault().notify(input);

                        if (result == NotifyDescriptor.OK_OPTION) {
                            String newName = input.getInputText().trim();

                            // Validar el nuevo nombre
                            if (newName.isEmpty() || newName.equals(currentName)) {
                                return; // No hacer nada si está vacío o es el mismo nombre
                            }

                            // Verificar que el nombre sea válido (opcional)
                            if (!newName.matches("[a-zA-Z0-9_-]+")) {
                                NotifyDescriptor.Message error = new NotifyDescriptor.Message(
                                        "Nombre inválido. Use solo letras, números, guiones y guiones bajos.",
                                        NotifyDescriptor.ERROR_MESSAGE
                                );
                                DialogDisplayer.getDefault().notify(error);
                                return;
                            }

                            // Verificar que no exista ya una carpeta con ese nombre
                            FileObject parentDir = javaProject.getProjectDirectory().getParent();
                            if (parentDir.getFileObject(newName) != null) {
                                NotifyDescriptor.Message error = new NotifyDescriptor.Message(
                                        "Ya existe un proyecto con ese nombre en el directorio.",
                                        NotifyDescriptor.ERROR_MESSAGE
                                );
                                DialogDisplayer.getDefault().notify(error);
                                return;
                            }

                            javaProject.rename(newName);
                        }
                    }
                }
            };
        }
    }

    public void delete() {
        try {
            // Cierra el proyecto si está abierto
            OpenProjects.getDefault().close(new Project[]{this});

            // Borra recursivamente el contenido del directorio
            deleteRecursively(projectDir);

            // Notifica que el proyecto fue eliminado
            state.notifyDeleted();

            StatusDisplayer.getDefault().setStatusText("Proyecto eliminado: " + projectDir.getNameExt());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void deleteRecursively(FileObject fo) throws IOException {
        for (FileObject child : fo.getChildren()) {
            deleteRecursively(child);
        }
        fo.delete();
    }

    public void rename(String newName) {
        try {
            OpenProjects.getDefault().close(new Project[]{this});

            FileObject parentDir = projectDir.getParent();

            org.openide.filesystems.FileUtil.moveFile(projectDir, parentDir, newName);

            FileObject renamedProjectDir = parentDir.getFileObject(newName);
            if (renamedProjectDir != null) {
                try {
                    JavaProjectFactory factory = new JavaProjectFactory();
                    if (factory.isProject(renamedProjectDir)) {
                        Project renamedProject = factory.loadProject(renamedProjectDir, state);
                        OpenProjects.getDefault().open(new Project[]{renamedProject}, false);
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            StatusDisplayer.getDefault().setStatusText("Proyecto renombrado a: " + newName);
            refreshProjectExplorer();

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            NotifyDescriptor.Message error = new NotifyDescriptor.Message(
                    "Error al renombrar el proyecto: " + ex.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE
            );
            DialogDisplayer.getDefault().notify(error);
        }
    }

    private static void refreshProjectExplorer() {
        TopComponent.Registry registry = TopComponent.getRegistry();
        for (TopComponent tc : registry.getOpened()) {
            if (tc instanceof ExploradorTopComponent explorer) {
                try {
                    explorer.refreshExplorer();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

}
