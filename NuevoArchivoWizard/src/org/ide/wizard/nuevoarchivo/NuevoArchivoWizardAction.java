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
import org.ide.arbol.proyectos.ExploradorTopComponent;
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
import org.openide.windows.TopComponent;

/**
 * Wizard encargado de ayudar a crear nuevos ficheros a los usuarios. Esta clase unifica todas
 * las partes del Wizard y es la responsable del botón "Nuevo Archivo".
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
@ActionID(category = "...", id = "org.ide.wizard.nuevoarchivo.NuevoArchivoWizardAction")
@ActionRegistration(displayName = "Nuevo Archivo", iconBase = "org/ide/wizard/nuevoarchivo/NewFileWizardIcon.png")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 10),
    @ActionReference(path = "Toolbars/File", position = 10)
})
public final class NuevoArchivoWizardAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {

        if (!anyProjectOpen()) {
            return;
        }

        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new NuevoArchivoWizardPanel1());
        panels.add(new NuevoArchivoWizardPanel2());
        String[] steps = new String[panels.size()];

        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            steps[i] = c.getName();
            if (c instanceof JComponent) {
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("Nuevo Archivo");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            FileObject template = (FileObject) wiz.getProperty("plantilla");
            FileObject carpeta = (FileObject) wiz.getProperty("carpeta");
            String nombre = (String) wiz.getProperty("nombre");
            String path = (String) wiz.getProperty("path");

            createFileFromTemplate(template, carpeta, nombre, path);
            refreshProjectExplorer();

        }
    }

    private void createFileFromTemplate(FileObject templateFO, FileObject targetFolder, String name, String path) {
        try {
            String templateText = readTemplateContent(templateFO);
            String finalText = templateText
                    .replace("${name}", name)
                    .replace("${package}", path);

            FileObject newFile = targetFolder.createData(name, templateFO.getExt());

            try (OutputStream os = newFile.getOutputStream(); OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8")) {
                writer.write(finalText);
            }

            DataObject newDO = DataObject.find(newFile);

            SwingUtilities.invokeLater(() -> {
                newCodeEditor(newFile);
            });

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private String readTemplateContent(FileObject templateFO) throws IOException {
        try (InputStream is = templateFO.getInputStream(); InputStreamReader reader = new InputStreamReader(is, "UTF-8"); BufferedReader br = new BufferedReader(reader)) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    private boolean anyProjectOpen() {
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

    private void refreshProjectExplorer() {
        TopComponent.Registry registry = TopComponent.getRegistry();
        for (TopComponent tc : registry.getOpened()) {
            if (tc instanceof ExploradorTopComponent explorer) {
                try {
                    explorer.refreshExplorer();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private void newCodeEditor(FileObject fileObject) {
        try {
            CodeEditorTopComponent newEditor = new CodeEditorTopComponent(fileObject);
            newEditor.open();
            newEditor.requestActive();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
