/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/NetBeansModuleDevelopment-files/sampleAction.java to edit this template
 */
package org.ide.wizard.packages;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.ide.arbol.proyectos.ExploradorTopComponent;
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
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

// An example action demonstrating how the wizard could be called from within
// your code. You can move the code below wherever you need, or register an action:
@ActionID(category="...", id="org.ide.wizard.packages.NewPackageWizardAction")
@ActionRegistration(displayName="Nuevo Paquete", iconBase = "org/ide/wizard/packages/NewPackage.png")
@ActionReferences({
    @ActionReference(path="Menu/Tools", position=20),
    @ActionReference(path="Toolbars/File", position=20)
})
public final class NewPackageWizardAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!anyProjectOpen()){return;}
        
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new NewPackageWizardPanel1());
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
        wiz.setTitle("Nuevo paquete");
        
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            
            FileObject srcFolder = (FileObject) wiz.getProperty("srcFolder");
            String name = (String) wiz.getProperty("name");
            
            if (isValidName(name)){
                try {
                    newPackage(srcFolder, name);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
            } else {
                showErrorDialog("El nombre de paquete no es válido. Intentelo de nuevo.");
            }
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
    

        public FileObject newPackage(FileObject srcFolder, String packageName) throws IOException {
        String route = packageName.replace('.', '/');
        FileObject paqueteExistente = srcFolder.getFileObject(route);

        if (paqueteExistente != null && paqueteExistente.isFolder()) {
            // Ya existe, lo devolvemos directamente
            showErrorDialog("El paquete ya existe, intente otro nombre.");
            return paqueteExistente;
        }

        // Si no existe, lo creamos parte por parte
        String[] partes = packageName.split("\\.");
        FileObject carpetaActual = srcFolder;

        for (String parte : partes) {
            FileObject subcarpeta = carpetaActual.getFileObject(parte);
            if (subcarpeta == null) {
                subcarpeta = carpetaActual.createFolder(parte);
            }
            carpetaActual = subcarpeta;
        }
        //Refrescamos el project explorer para que refleje los cambios
        refreshProjectExplorer();
        
        return carpetaActual;
    }
        
    public boolean isValidName(String nombrePaquete) {
        if (nombrePaquete == null || nombrePaquete.isEmpty()) {
            return false;
        }

        // No puede comenzar ni terminar con punto
        if (nombrePaquete.startsWith(".") || nombrePaquete.endsWith(".")) {
            return false;
        }

        // Cada parte del paquete debe cumplir las reglas
        String[] partes = nombrePaquete.split("\\.");
        for (String parte : partes) {
            if (!parte.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
                return false;
            }
        }

        return true;
    }
    
    private void showErrorDialog(String msg){
        // Mostrar advertencia
        DialogDisplayer.getDefault().notify(
            new NotifyDescriptor.Message(
                msg,
                NotifyDescriptor.ERROR_MESSAGE
            )
        );
    }
    
        private void refreshProjectExplorer() {
        TopComponent.Registry registry = TopComponent.getRegistry();
        for (TopComponent tc : registry.getOpened()) {
                if(tc instanceof ExploradorTopComponent explorer){
                    try {
                        explorer.refreshExplorer();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

        }
    }
}

    

