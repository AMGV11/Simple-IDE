package org.ide.wizard.nuevoarchivo;

import java.io.IOException;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

/**
 * Wizard encargado de ayudar a crear nuevos ficheros a los usuarios. Esta clase
 * se encarga de una de las partes lógicas del Wizard.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class NuevoArchivoWizardPanel1 implements WizardDescriptor.Panel<WizardDescriptor> {
        
    private NuevoArchivoVisualPanel1 component;

    @Override
    public NuevoArchivoVisualPanel1 getComponent() {
        if (component == null) {
            try {
                component = new NuevoArchivoVisualPanel1();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        wiz.putProperty("proyecto", getComponent().getProyecto());
        wiz.putProperty("plantilla", getComponent().getPlantilla());
    }
    
}
