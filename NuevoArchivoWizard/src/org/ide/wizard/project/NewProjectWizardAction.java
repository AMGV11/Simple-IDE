/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/NetBeansModuleDevelopment-files/sampleAction.java to edit this template
 */
package org.ide.wizard.project;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

// Con esto, queda registrado para que se vea en nuestro IDE en la barra Herramientas
@ActionID(category="...", id="org.ide.wizard.project.NewProjectWizardAction")
@ActionRegistration(displayName="Nuevo proyecto", iconBase = "org/ide/wizard/project/NewProject.png")
@ActionReferences({
    @ActionReference(path="Menu/Tools", position=43),
    @ActionReference(path="Toolbars/File", position=30)
})
public final class NewProjectWizardAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new NewProjectWizardPanel1());
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<WizardDescriptor>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("...dialog title...");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            // AQUI ES LO QUE SE HACE AL FINALIZAR EL DIALOGO
            // COGEMOS DATOS DE AMBOS PANELES MEDIANTE LAS PROPIEDADES
            FileObject directory = (FileObject) wiz.getProperty("directory");
            String projectName = (String) wiz.getProperty("projectName");
            
            createNewProject(projectName, directory);
        }
    }

    private void createNewProject (String projectName, FileObject directory) {
        try {
            // 1. Crear carpeta del proyecto
            FileObject projectFO = directory.createFolder(projectName);
            
            // 2. Crear carpeta src
            FileObject srcFolder = projectFO.createFolder("src");
            
            // 3. Crear carpeta bin
            FileObject binFolder = projectFO.createFolder("bin");
            
            // 4. Abrimos el proyecto para que salga en el arbol de proyectos
                        try {
                ProjectManager pm = ProjectManager.getDefault();
                if (pm.isProject(projectFO)) {
                    Project proyecto = pm.findProject(projectFO);
                    OpenProjects.getDefault().open(new Project[]{proyecto}, false);
                } else {
                    JOptionPane.showMessageDialog(null, "La carpeta seleccionada no es un proyecto válido.");
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
