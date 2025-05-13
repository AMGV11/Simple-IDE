/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.arbol.proyectos;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

//Con ServiceProvider registramos este tipo de Fabrica de Proyectos en nuestro sistema.
@ServiceProvider(service=ProjectFactory.class)
public class JavaProjectFactory implements  ProjectFactory {

    public static final String PROJECT_FILE = "src";

    //Specifies when a project is a project, i.e.,
    //if "customer.txt" is present in a folder:*
    @Override
    public boolean isProject(FileObject projectDirectory) {
        return projectDirectory.getFileObject(PROJECT_FILE) != null;
    }

    //Specifies when the project will be opened, i.e., if the project exists:*
    @Override
    public Project loadProject(FileObject dir,  ProjectState state) throws IOException {
        return isProject(dir) ? new JavaProject(dir, state) : null;
    }

    @Override
    public void saveProject(final Project project) throws IOException, ClassCastException {
        // leave unimplemented for the moment
    }

    /*@Override //Por si implementamos ProjectFactory2
    public ProjectManager.Result isProject2(FileObject fo) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }*/

}
