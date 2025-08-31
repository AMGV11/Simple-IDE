package org.ide.arbol.proyectos;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.ide.code.editor.CodeEditorTopComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.FilterNode;
import org.openide.util.Exceptions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 * Define el nodo archivo, usado para representar los diferentes ficheros.
 * Declara el conjunto de acciones que se pueden hacer con los ficheros.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class FileNode extends FilterNode {

    private final FileObject fileObject;

    public FileNode(FileObject fileObject) throws DataObjectNotFoundException {
        super(
                new AbstractNode(Children.LEAF, Lookups.fixed(
                        fileObject,
                        DataObject.find(fileObject)
                )),
                Children.LEAF,
                Lookups.fixed(
                        fileObject,
                        DataObject.find(fileObject)
                )
        );
        setDisplayName(fileObject.getNameExt());
        this.fileObject = fileObject;
    }

    @Override
    public Action getPreferredAction() {
        return new AbstractAction("Abrir en mi editor") {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirEditor();
            }
        };
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            new AbstractAction("Abrir") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    abrirEditor();
                }
            },
            new AbstractAction("Renombrar") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    renombrarArchivo();
                }
            },
            new AbstractAction("Borrar") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    borrarArchivo();
                }
            }
        };
    }

    private void abrirEditor() {
        TopComponent.Registry registry = TopComponent.getRegistry();

        for (TopComponent tc : registry.getOpened()) {
            if (tc instanceof CodeEditorTopComponent editor) {
                FileObject file = editor.getCurrentFO();

                if (file.equals(fileObject)) {
                    System.out.println("[INFO] Ya esta abierto el archivo.");
                    return;
                }
            }
        }
        //Abrimos un nuevo editor si no se reutiliza ninguno
        newCodeEditor(fileObject);
    }

    private void renombrarArchivo() {
        // Crear un diálogo de entrada para el nuevo nombre
        NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine(
                "Nuevo nombre:",
                "Renombrar archivo"
        );

        // Poner el nombre actual (sin extensión) como valor por defecto
        String nombreActual = fileObject.getName();
        String extension = fileObject.getExt();
        input.setInputText(nombreActual);

        // Mostrar el diálogo
        Object result = DialogDisplayer.getDefault().notify(input);

        if (result == NotifyDescriptor.OK_OPTION) {
            String nuevoNombre = input.getInputText().trim();

            // Validar que el nombre no esté vacío
            if (nuevoNombre.isEmpty()) {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                                "El nombre no puede estar vacío.",
                                NotifyDescriptor.WARNING_MESSAGE
                        )
                );
                return;
            }

            // Validar que el nombre no contenga caracteres inválidos
            if (nuevoNombre.contains("/") || nuevoNombre.contains("\\")
                    || nuevoNombre.contains(":") || nuevoNombre.contains("*")
                    || nuevoNombre.contains("?") || nuevoNombre.contains("\"")
                    || nuevoNombre.contains("<") || nuevoNombre.contains(">")
                    || nuevoNombre.contains("|")) {

                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                                "El nombre contiene caracteres no válidos.",
                                NotifyDescriptor.WARNING_MESSAGE
                        )
                );
                return;
            }

            try {
                Node parentFolder = this.getParentNode();

                // Verificar si ya existe un archivo con ese nombre en la misma carpeta
                FileObject parent = fileObject.getParent();
                String nombreCompleto = extension.isEmpty() ? nuevoNombre : nuevoNombre + "." + extension;

                if (parent.getFileObject(nombreCompleto) != null) {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(
                                    "Ya existe un archivo con ese nombre.",
                                    NotifyDescriptor.WARNING_MESSAGE
                            )
                    );
                    return;
                }

                // Renombrar el archivo
                String nombreAnterior = fileObject.getNameExt();
                DataObject dobj = DataObject.find(fileObject);

                if (extension.isBlank() || extension.isEmpty()) {
                    dobj.rename(nuevoNombre + ".java");
                } else {
                    dobj.rename(nuevoNombre + "." + extension);
                }

                // Actualizar el nombre mostrado en el nodo
                setDisplayName(fileObject.getNameExt());

                // Refrescar el nodo padre para actualizar el árbol
                if (parentFolder instanceof FolderNode) {
                    ((FolderNode.FolderChildren) parentFolder.getChildren()).refreshKeys();
                }

                System.out.println("[INFO] Archivo renombrado de '" + nombreAnterior + "' a '" + fileObject.getNameExt() + "'");

            } catch (IOException ex) {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                                "[ERROR] Fallo al renombrar el archivo: " + ex.getMessage(),
                                NotifyDescriptor.ERROR_MESSAGE
                        )
                );
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void borrarArchivo() {
        DataObject dob = getLookup().lookup(DataObject.class);
        Node parentFolder = this.getParentNode();
        if (dob != null) {
            try {
                boolean confirmado = DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Confirmation(
                                "¿Seguro que quieres eliminar el archivo " + dob.getPrimaryFile().getNameExt() + "?",
                                NotifyDescriptor.YES_NO_OPTION)
                ) == NotifyDescriptor.YES_OPTION;

                if (confirmado) {
                    dob.delete();

                }
            } catch (IOException ex) {
                // Mostrar error al usuario
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message("Error al eliminar el archivo: " + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE)
                );
            }
        }
        //Actualizamos la carpeta en el explorador
        if (parentFolder instanceof FolderNode) {
            ((FolderNode.FolderChildren) parentFolder.getChildren()).refreshKeys();
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

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/ide/arbol/proyectos/FileIcon.png");
    }
}
