package org.ide.wizard.packages;

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Wizard encargado de ayudar a crear nuevos paquetes en los proyectos a los
 * usuarios. Esta clase se encarga de una de las partes lógicas del Wizard.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class NewPackageWizardPanel1 implements WizardDescriptor.Panel<WizardDescriptor> {

    private NewPackageVisualPanel1 component;

    @Override
    public NewPackageVisualPanel1 getComponent() {
        if (component == null) {
            component = new NewPackageVisualPanel1();
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
        wiz.putProperty("srcFolder", getComponent().getFolder());
        wiz.putProperty("name", getComponent().getPackageName());
    }

}
