// ManualsExplorerTopComponent.java - Explorador de manuales (ACTUALIZADO)
package org.ide.help;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@ConvertAsProperties(
        dtd = "-//org.ide.help//ManualExplorer//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ManualsExplorerTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "org.ide.help.ManualsExplorerTopComponent")
@ActionReference(path = "Menu/Window")
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ManualsExplorerAction",
        preferredID = "ManualsExplorerActionTopComponent"
)
@Messages({
    "CTL_ManualsExplorerAction=Explorador de Manuales",
    "CTL_ManualsExplorerTopComponent=Manuales",
    "HINT_ManualsExplorerTopComponent=Explorador de manuales PDF"
})
public final class ManualsExplorerTopComponent extends TopComponent {

    private JTree manualsTree;
    private DefaultTreeModel treeModel;

    public ManualsExplorerTopComponent() {
        initComponents();
        setName(Bundle.CTL_ManualsExplorerAction());
        setToolTipText(Bundle.CTL_ManualsExplorerTopComponent());
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Crear árbol de manuales
        initializeTree();

        // Agregar listener para doble click
        manualsTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = manualsTree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        ManualNode node = (ManualNode) path.getLastPathComponent();
                        if (node.getPdfFile() != null) {
                            PDFViewerTopComponent.openPDF(node.getPdfFile());
                        } else if (node.getResourcePath() != null) {
                            // Nuevo: abrir PDF desde resources
                            openPDFFromResources(node.getResourcePath());
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(manualsTree);
        add(scrollPane, BorderLayout.CENTER);

        // Toolbar para pruebas
        /*JToolBar toolBar = new JToolBar();
        JButton refreshButton = new JButton("Actualizar");
        JButton testButton = new JButton("Test PDF");
        refreshButton.addActionListener(e -> refreshManuals());
        toolBar.add(refreshButton);
        toolBar.add(testButton);
        add(toolBar, BorderLayout.NORTH);
        
        testButton.addActionListener(e -> {
            // Test con PDF desde resources
            openPDFFromResources("docs/think-java-2e.pdf");
        });*/
    }

    private void initializeTree() {
        ManualNode root = new ManualNode("Manuales");
        ManualNode userManuals = new ManualNode("Manuales de Usuario");
        ManualNode techManuals = new ManualNode("Manuales Técnicos");
        ManualNode javaManuals = new ManualNode("Manuales para IDE");
        loadManualsFromResources(userManuals, "docs/user");
        loadManualsFromResources(techManuals, "docs/tech");
        loadManualsFromResources(javaManuals, "docs");

        root.add(userManuals);
        root.add(techManuals);
        root.add(javaManuals);

        treeModel = new DefaultTreeModel(root);
        manualsTree = new JTree(treeModel);
        manualsTree.setShowsRootHandles(true);
        manualsTree.expandRow(0); // Expandir root

        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem openInternal = new JMenuItem("Abrir en visor interno");
        openInternal.addActionListener(e -> {
            ManualNode selectedNode = getSelectedNode();
            if (selectedNode != null) {
                if (selectedNode.getPdfFile() != null) {
                    PDFViewerTopComponent.openPDF(selectedNode.getPdfFile());
                } else if (selectedNode.getResourcePath() != null) {
                    openPDFFromResources(selectedNode.getResourcePath());
                }
            }
        });

        JMenuItem openExternal = new JMenuItem("Abrir con aplicación predeterminada");
        openExternal.addActionListener(e -> {
            ManualNode selectedNode = getSelectedNode();
            if (selectedNode != null) {
                if (selectedNode.getPdfFile() != null) {
                    openWithDefaultApp(selectedNode.getPdfFile());
                } else if (selectedNode.getResourcePath() != null) {
                    openResourceWithDefaultApp(selectedNode.getResourcePath());
                }
            }
        });

        popupMenu.add(openInternal);
        popupMenu.add(openExternal);

        // Asignar menú contextual al árbol
        manualsTree.setComponentPopupMenu(popupMenu);
    }

    private void loadManualsFromResources(ManualNode parent, String resourcePath) {
        try {
            // Intentar obtener la URL del directorio de resources
            URL resourceURL = getClass().getResource(resourcePath);
            if (resourceURL == null) {
                //System.out.println("No se encontró el directorio de resources: " + resourcePath);
                return;
            }

            // Manuales que tenemos actualmente
            String[] knownPDFs = {
                "Manual Tecnico Java 24.pdf",
                "Manual Git (Pro GIT).pdf",
                "Manual Java (Think Java 2).pdf",
                "Proximamente....pdf"
            };

            for (String pdfName : knownPDFs) {
                String fullPath = resourcePath + "/" + pdfName;
                URL pdfURL = getClass().getResource(fullPath);

                if (pdfURL != null) {
                    String displayName = pdfName.substring(0, pdfName.lastIndexOf('.'));
                    ManualNode node = new ManualNode(displayName, fullPath); // resourcePath en lugar de File
                    parent.add(node);
                    //System.out.println("PDF agregado: " + fullPath);
                }
            }

        } catch (Exception e) {
            System.err.println("Error cargando manuales desde resources: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openPDFFromResources(String resourcePath) {
        try {
            File tempPDF = createTempFileFromResource(resourcePath);
            if (tempPDF != null) {
                PDFViewerTopComponent.openPDF(tempPDF);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar PDF desde resources: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private File createTempFileFromResource(String resourcePath) throws IOException {
        URL pdfURL = getClass().getResource(resourcePath);
        if (pdfURL == null) {
            throw new FileNotFoundException("PDF no encontrado en resources: " + resourcePath);
        }

        String fileName = Paths.get(resourcePath).getFileName().toString();
        File tempFile = File.createTempFile("manual_", "_" + fileName);
        tempFile.deleteOnExit(); // Se borra automáticamente al cerrar la JVM

        try (InputStream inputStream = pdfURL.openStream()) {
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        //System.out.println("Archivo temporal creado: " + tempFile.getAbsolutePath());
        return tempFile;
    }

    private void openResourceWithDefaultApp(String resourcePath) {
        try {
            File tempFile = createTempFileFromResource(resourcePath);
            if (tempFile != null) {
                openWithDefaultApp(tempFile);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al abrir recurso: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadManualsFromDirectory(ManualNode parent, String directoryPath) {
        File dir = new File(directoryPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((file) -> file.getName().toLowerCase().endsWith(".pdf"));
            if (files != null) {
                for (File file : files) {
                    String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
                    ManualNode node = new ManualNode(name, file);
                    parent.add(node);
                }
            }
        }
    }

    private ManualNode getSelectedNode() {
        TreePath selectedPath = manualsTree.getSelectionPath();
        if (selectedPath != null) {
            return (ManualNode) selectedPath.getLastPathComponent();
        }
        return null;
    }

    private void openWithDefaultApp(File pdfFile) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(pdfFile);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No se puede abrir el archivo con la aplicación predeterminada",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Alternativa para sistemas sin Desktop API
                openWithSystemCommand(pdfFile);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al abrir el archivo: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openWithSystemCommand(File pdfFile) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            if (os.contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", "start", "\"\"", pdfFile.getAbsolutePath());
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("open", pdfFile.getAbsolutePath());
            } else {
                pb = new ProcessBuilder("xdg-open", pdfFile.getAbsolutePath());
            }

            pb.start();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al ejecutar comando del sistema: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshManuals() {
        initializeTree();
        manualsTree.updateUI();
    }

    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }
}
