package org.ide.wizard.nuevoarchivo;

import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Wizard encargado de ayudar a crear nuevos ficheros a los usuarios. Esta clase
 * se encarga de una de las partes lógicas del Wizard.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class NuevoArchivoWizardPanel2 implements WizardDescriptor.Panel<WizardDescriptor> {
    
    private boolean valid = true;
    private NuevoArchivoVisualPanel2 component;

    @Override
    public NuevoArchivoVisualPanel2 getComponent() {
        if (component == null) {
            component = new NuevoArchivoVisualPanel2();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        Project proyecto = (Project) wiz.getProperty("proyecto"); 
        if (getComponent().cargarCarpetas(proyecto) == false){
            valid = false;
        } 
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        wiz.putProperty("nombre", getComponent().getNombre());
        wiz.putProperty("carpeta", getComponent().getCarpeta());    
        wiz.putProperty("path", getComponent().getPath());    
    }

}
