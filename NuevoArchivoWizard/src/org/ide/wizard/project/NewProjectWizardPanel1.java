package org.ide.wizard.project;

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Wizard encargado de ayudar a crear nuevos proyectos a los usuarios. Esta
 * clase se encarga de una de las partes lógicas del Wizard.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class NewProjectWizardPanel1 implements WizardDescriptor.Panel<WizardDescriptor> {

    private NewProjectVisualPanel1 component;

    @Override
    public NewProjectVisualPanel1 getComponent() {
        if (component == null) {
            component = new NewProjectVisualPanel1();
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
        wiz.putProperty("projectName", getComponent().getProjectName());
        wiz.putProperty("directory", getComponent().getDirectory());
    }
}
