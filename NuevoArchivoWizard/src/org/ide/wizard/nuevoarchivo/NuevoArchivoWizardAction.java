/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/NetBeansModuleDevelopment-files/sampleAction.java to edit this template
 */
package org.ide.wizard.nuevoarchivo;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.ide.code.editor.CodeEditorTopComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

// Clase encargada del boton "Nuevo archivo" del IDE
// Hay que expandirla para que haga paquetes en los proyectos
// Se puede hacer que se use un paquete llamado (Default) en el proyecto si no hay ningun paquete

// An example action demonstrating how the wizard could be called from within
// your code. You can move the code below wherever you need, or register an action:
@ActionID(category="...", id="org.ide.wizard.nuevoarchivo.NuevoArchivoWizardAction")
@ActionRegistration(displayName="Nuevo Archivo", iconBase = "org/ide/wizard/nuevoarchivo/NewFileWizardIcon.png")
@ActionReferences({
    @ActionReference(path="Menu/Tools", position=10),
    @ActionReference(path="Toolbars/File", position=10)
})
public final class NuevoArchivoWizardAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (!anyProjectOpen()){return;}
        
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new NuevoArchivoWizardPanel1());
        panels.add(new NuevoArchivoWizardPanel2());
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
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("Nuevo Archivo");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            // AQUI ES LO QUE SE HACE AL FINALIZAR EL DIALOGO
            // COGEMOS DATOS DE AMBOS PANELES MEDIANTE LAS PROPIEDADES
            FileObject template = (FileObject) wiz.getProperty("plantilla");
            FileObject carpeta = (FileObject) wiz.getProperty("carpeta");
            String nombre = (String) wiz.getProperty("nombre");
            String path = (String) wiz.getProperty("path");
            
            // Reunimos los datos recogidos en el Wizard para crear el fichero que se quiera
            createFileFromTemplate(template, carpeta, nombre, path);
            
        }
    }
    
        private void createFileFromTemplate(FileObject templateFO, FileObject targetFolder, String name, String path) {
        try {
            String templateText = readTemplateContent(templateFO);
            String finalText = templateText
                .replace("${name}", name)
                .replace("${package}", path);
            
            FileObject newFile = targetFolder.createData(name, templateFO.getExt());

            try (OutputStream os = newFile.getOutputStream();
                 OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8")) {
                writer.write(finalText);
            }

            DataObject newDO = DataObject.find(newFile); 
            
            SwingUtilities.invokeLater(() -> {
            CodeEditorTopComponent editor = null;
                try {
                    editor = new CodeEditorTopComponent();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            editor.open();
            editor.requestActive();
                try {
                    editor.loadFile(newFile);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
        });
            
      
            
            // Esto abre el editor por defecto
            /*OpenCookie open = newDO.getLookup().lookup(OpenCookie.class);
            if (open != null) {
                open.open();
            }*/

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private String readTemplateContent(FileObject templateFO) throws IOException {
        try (InputStream is = templateFO.getInputStream();
             InputStreamReader reader = new InputStreamReader(is, "UTF-8");
             BufferedReader br = new BufferedReader(reader)) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
    
    private boolean anyProjectOpen (){
        Project[] open = OpenProjects.getDefault().getOpenProjects();
        if (open.length == 0) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                            "No hay proyectos abiertos",
                            NotifyDescriptor.WARNING_MESSAGE
                    )
            );
            return false;
        }
        return true;
    }

}
