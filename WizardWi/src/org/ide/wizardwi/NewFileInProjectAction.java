/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.wizardwi;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.ide.code.editor.EditorTopComponent;
import org.openide.windows.TopComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
@ActionID(
    category = "File",
    id = "org.miide.actions.NewFileInProjectAction"
)
@ActionRegistration(
    displayName = "#CTL_NewFileInProjectAction"
    //iconBase = "org/miide/resources/icons/new-file.png"
)
@ActionReference(path = "Toolbars/File", position = 300)
@Messages("CTL_NewFileInProjectAction=Nuevo Archivo en Proyecto…")
public final class NewFileInProjectAction extends AbstractAction {

    public NewFileInProjectAction() {
        putValue(NAME, Bundle.CTL_NewFileInProjectAction());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
            try {
                Project[] open = OpenProjects.getDefault().getOpenProjects();
                if (open.length == 0) {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(
                                    "No hay proyectos abiertos",
                                    NotifyDescriptor.WARNING_MESSAGE
                            )
                    );
                    return;
                }
                
                // 1) Selección de proyecto
                String[] names = new String[open.length];
                for (int i = 0; i < open.length; i++) {
                    names[i] = open[i].getProjectDirectory().getNameExt();
                }
                Object sel = JOptionPane.showInputDialog(
                        null,
                        "Elige el proyecto destino:",
                        "Nuevo Archivo en Proyecto",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        names,
                        names[0]
                );
                if (sel == null) {
                    return; // cancelado
                }
                int idx = -1;
                for (int i = 0; i < names.length; i++) {
                    if (names[i].equals(sel)) {
                        idx = i; break;
                    }
                }
                Project project = open[idx];
                
                // 2) Nombre del nuevo archivo
                String nombreSinExtension = JOptionPane.showInputDialog(
                        "Nombre de archivo (sin extensión):"
                );
                if (nombreSinExtension == null || nombreSinExtension.trim().isEmpty()) {
                    return;
                }
                
                
                // 3) Determinar carpeta destino: primero buscar src, si existe
                Sources sources = org.netbeans.api.project.ProjectUtils.getSources(project);
                SourceGroup[] javaGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                FileObject targetFolderFO;
                if (javaGroups.length > 0) {
                    targetFolderFO = javaGroups[0].getRootFolder();
                } else {
                    targetFolderFO = project.getProjectDirectory();
                }
                
                // 4) Cargar plantilla desde layer.xml
                FileObject tplFO = FileUtil.getConfigFile(
                        "Templates/Other/HTML.html"
                );
                
                if (tplFO == null) {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(
                                    "No se encontró la plantilla Templates/MyIDE/MyTemplate.txt",
                                    NotifyDescriptor.ERROR_MESSAGE
                            )
                    );
                    return;
                }
                System.out.println("Se encontro bien la plantilla");

                DataObject templateDO = DataObject.find(tplFO);
                
                DataObject created = safeCreateFromTemplate(
                    templateDO,
                    targetFolderFO,
                    nombreSinExtension,
                    false // abre en tu EditorTopComponent
                );
                if (created == null) {
                    // manejar fallo...
                }

                
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

}


/**
 * Crea un archivo nuevo desde una plantilla, asegurando:
 * 1) Refresco de la carpeta para liberar locks residuales.
 * 2) Eliminación manual y segura de un archivo previo con el mismo nombre.
 * 3) Creación atómica desde plantilla.
 * 4) (Opcional) apertura del archivo en tu editor.
 *
 * @param tplDO        El DataObject de la plantilla.
 * @param targetFolder La carpeta (FileObject) donde crear.
 * @param name         El nombre de archivo (sin extensión).
 * @param openEditor   Si true abre luego el archivo en el EditorTopComponent.
 * @return             El DataObject recién creado, o null si falló.
 */
public static DataObject safeCreateFromTemplate(
        DataObject tplDO,
        FileObject targetFolder,
        String name,
        boolean openEditor
) {
    try {
        AtomicReference<DataObject> result = new AtomicReference<>(null);
        FileSystem fs = targetFolder.getFileSystem();
        
        try {
            fs.runAtomicAction(() -> {
                try {
                    // 1) Refrescar carpeta en disco para invalidar locks
                    File folderOnDisk = FileUtil.toFile(targetFolder);
                    if (folderOnDisk != null) {
                        FileUtil.refreshFor(new File[]{ folderOnDisk });
                    }
                    targetFolder.refresh();
                    
                    // 2) Eliminar manualmente un posible archivo previo
                    String ext = tplDO.getPrimaryFile().getExt();
                    FileObject oldFO = targetFolder.getFileObject(name, ext);
                    if (oldFO != null && oldFO.isValid()) {
                        DataObject oldDO = DataObject.find(oldFO);
                        EditorCookie ec = oldDO.getLookup().lookup(EditorCookie.class);
                        if (ec != null) ec.close();

                        for (int i = 0; i < 5; i++) {
                            FileLock lock = null;
                            try {
                                lock = oldFO.lock();
                                oldFO.delete(lock);
                                break;
                            } catch (IOException ex) {
                                try {
                                    Thread.sleep(100); // Espera y reintenta si aún está bloqueado
                                } catch (InterruptedException ex1) {
                                    Exceptions.printStackTrace(ex1);
                                }
                            } finally {
                                if (lock != null) lock.releaseLock();
                            }
                        }
                    }
                    
                    // 3) Crear desde la plantilla
                                                try {
                                    Thread.sleep(100); // Espera y reintenta si aún está bloqueado
                                } catch (InterruptedException ex1) {
                                    Exceptions.printStackTrace(ex1);
                                }
                    DataFolder df = DataFolder.findFolder(targetFolder);
                    DataObject created = tplDO.createFromTemplate(df, name);
                    result.set(created);
                    
                    // 4) Abrir en editor si se pide
                    if (openEditor) {
                        FileObject foCreated = created.getPrimaryFile();
                        SwingUtilities.invokeLater(() -> {
                            try {
                                EditorTopComponent ed = new EditorTopComponent();
                                ed.open(); ed.requestActive();
                                ed.loadFile(foCreated);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        });
                    }
                    
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            });
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        return result.get();
    } catch (FileStateInvalidException ex) {
        Exceptions.printStackTrace(ex);
    }
    return null;
}

}