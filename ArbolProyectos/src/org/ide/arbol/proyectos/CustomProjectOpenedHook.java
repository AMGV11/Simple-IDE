/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.arbol.proyectos;

import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ProjectServiceProvider(service = ProjectOpenedHook.class, projectType = "*")
public class CustomProjectOpenedHook extends ProjectOpenedHook {

    private final Project project;

    public CustomProjectOpenedHook(Project project) {
        this.project = project;
    }

    @Override
    protected void projectOpened() {
        // Aquí mostramos tu TopComponent
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
        // Cierra el TopComponent si quieres
    }
}
