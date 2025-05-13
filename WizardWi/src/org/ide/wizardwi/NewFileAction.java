/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.wizardwi;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
    category = "File",
    id = "org.miide.actions.NewFileAction"
)
@ActionRegistration(
    displayName = "#CTL_NewFileAction"
    //iconBase = "org/miide/resources/icons/new-file.png"
)
@ActionReference(path = "Toolbars/File", position = 400)
@Messages("CTL_NewFileAction=Nuevo archivo Java...")
public class NewFileAction extends AbstractAction {

    public NewFileAction() {
        putValue(NAME, Bundle.CTL_NewFileAction());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            FileObject templatesFolderFO = FileUtil.getConfigFile("Templates/Other");
            if (templatesFolderFO == null || !templatesFolderFO.isFolder()) {
                throw new IOException("No se encontró Templates/Other");
            }

            FileObject[] templates = templatesFolderFO.getChildren();
            List<FileObject> templateList = new ArrayList<>();
            for (FileObject fo : templates) {
                if (fo.isData()) {
                    templateList.add(fo);
                }
            }

            if (templateList.isEmpty()) {
                throw new IOException("No hay plantillas en Templates/Java");
            }

            // Crear el panel del diálogo
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            DefaultListModel<FileObject> listModel = new DefaultListModel<>();
            for (FileObject fo : templateList) {
                listModel.addElement(fo);
            }

            JList<FileObject> templateJList = new JList<>(listModel);
            templateJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            templateJList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
                JLabel label = new JLabel(value.getName());
                if (isSelected) {
                    label.setOpaque(true);
                    label.setBackground(list.getSelectionBackground());
                    label.setForeground(list.getSelectionForeground());
                }
                return label;
            });

            JTextField nameField = new JTextField();

            panel.add(new JLabel("Plantilla:"), BorderLayout.NORTH);
            panel.add(new JScrollPane(templateJList), BorderLayout.CENTER);
            panel.add(new JLabel("Nombre del archivo:"), BorderLayout.SOUTH);

            JPanel southPanel = new JPanel(new BorderLayout());
            southPanel.add(nameField, BorderLayout.CENTER);
            panel.add(southPanel, BorderLayout.SOUTH);

            DialogDescriptor dd = new DialogDescriptor(panel, "Nuevo archivo desde plantilla");
            Object result = DialogDisplayer.getDefault().notify(dd);

            if (result == DialogDescriptor.OK_OPTION) {
                FileObject selectedTemplate = templateJList.getSelectedValue();
                String name = nameField.getText().trim();

                if (selectedTemplate == null || name.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Debes seleccionar una plantilla y escribir un nombre válido.");
                    return;
                }

                // Carpeta destino (ajustala según tu lógica)
                FileObject destino = FileUtil.toFileObject(new File(System.getProperty("user.home")));
                DataFolder targetFolder = DataFolder.findFolder(destino);

                createFileFromTemplate(selectedTemplate, targetFolder, name);
            }

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void createFileFromTemplate(FileObject templateFO, DataFolder targetFolder, String name) {
        try {
            String templateText = readTemplateContent(templateFO);
            String finalText = templateText.replace("${name}", name);

            FileObject targetFO = targetFolder.getPrimaryFile();
            FileObject newFile = targetFO.createData(name, templateFO.getExt());

            try (OutputStream os = newFile.getOutputStream();
                 OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8")) {
                writer.write(finalText);
            }

            DataObject newDO = DataObject.find(newFile);
            OpenCookie open = newDO.getLookup().lookup(OpenCookie.class);
            if (open != null) {
                open.open();
            }

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
}
