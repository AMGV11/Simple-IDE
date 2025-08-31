package org.ide.arbol.proyectos;

import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Hace visible los proyectos abiertos en el árbol de proyectos
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
@ProjectServiceProvider(service = ProjectOpenedHook.class, projectType = "*")
public class CustomProjectOpenedHook extends ProjectOpenedHook {

    private final Project project;

    public CustomProjectOpenedHook(Project project) {
        this.project = project;
    }

    @Override
    protected void projectOpened() {
        SwingUtilities.invokeLater(() -> {
            TopComponent tc = WindowManager.getDefault().findTopComponent("ExploradorTopComponent");
            if (tc != null) {
                tc.open();
                tc.requestVisible();
            }
        });
    }

    @Override
    protected void projectClosed() {
    }
}
