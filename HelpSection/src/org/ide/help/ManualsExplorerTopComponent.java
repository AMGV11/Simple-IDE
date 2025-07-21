// ManualsExplorerTopComponent.java - Explorador de manuales
package org.ide.help;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(manualsTree);
        add(scrollPane, BorderLayout.CENTER);

        // Toolbar simple
        JToolBar toolBar = new JToolBar();
        JButton refreshButton = new JButton("Actualizar");
        JButton testButton = new JButton("Test PDF");
        refreshButton.addActionListener(e -> refreshManuals());
        toolBar.add(refreshButton);
        toolBar.add(testButton);
        add(toolBar, BorderLayout.NORTH);
        

        testButton.addActionListener(e -> {
            File testPdf = new File("C:\\Users\\anton\\Desktop\\Uned\\Plantilla PFG ETSI Informática UNED\\capitulos\\C5-desarrollo.pdf");
            if (testPdf.exists()) {
                PDFViewerTopComponent.openPDF(testPdf);
            } else {
                JOptionPane.showMessageDialog(this, "PDF no encontrado: " + testPdf.getAbsolutePath());
            }
        });
    }

    private void initializeTree() {
        // Crear nodo raíz
        ManualNode root = new ManualNode("Manuales");

        // Crear categorías
        ManualNode userManuals = new ManualNode("Manuales de Usuario");
        ManualNode techManuals = new ManualNode("Manuales Técnicos");

        // Agregar PDFs de ejemplo (cambiar rutas según tu estructura)
        loadManualsFromDirectory(userManuals, "org/ide/help/manuals");
        //loadManualsFromDirectory(techManuals, "manuales/tecnicos");

        root.add(userManuals);
        root.add(techManuals);

        treeModel = new DefaultTreeModel(root);
        manualsTree = new JTree(treeModel);
        manualsTree.setShowsRootHandles(true);
        manualsTree.expandRow(0); // Expandir root
        
            JPopupMenu popupMenu = new JPopupMenu();
    
    JMenuItem openInternal = new JMenuItem("Abrir en visor interno");
    openInternal.addActionListener(e -> {
        ManualNode selectedNode = getSelectedNode();
        if (selectedNode != null && selectedNode.getPdfFile() != null) {
            PDFViewerTopComponent.openPDF(selectedNode.getPdfFile());
        }
    });
    
    JMenuItem openExternal = new JMenuItem("Abrir con aplicación predeterminada");
    openExternal.addActionListener(e -> {
        ManualNode selectedNode = getSelectedNode();
        if (selectedNode != null && selectedNode.getPdfFile() != null) {
            openWithDefaultApp(selectedNode.getPdfFile());
        }
    });
    
    popupMenu.add(openInternal);
    popupMenu.add(openExternal);
    
    // Agregar separador y más opciones si quieres
    popupMenu.addSeparator();
    JMenuItem showInExplorer = new JMenuItem("Mostrar en explorador de archivos");
    showInExplorer.addActionListener(e -> {
        ManualNode selectedNode = getSelectedNode();
        if (selectedNode != null && selectedNode.getPdfFile() != null) {
            showInFileExplorer(selectedNode.getPdfFile());
        }
    });
    popupMenu.add(showInExplorer);
    
    // Asignar menú contextual al árbol
    manualsTree.setComponentPopupMenu(popupMenu);
}
    
    // Método auxiliar para obtener el nodo seleccionado
private ManualNode getSelectedNode() {
    TreePath selectedPath = manualsTree.getSelectionPath();
    if (selectedPath != null) {
        return (ManualNode) selectedPath.getLastPathComponent();
    }
    return null;
}

// Método para abrir con aplicación predeterminada
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

// Método alternativo para sistemas sin Desktop API
private void openWithSystemCommand(File pdfFile) {
    try {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder pb;
        
        if (os.contains("win")) {
            // Windows
            pb = new ProcessBuilder("cmd", "/c", "start", "\"\"", pdfFile.getAbsolutePath());
        } else if (os.contains("mac")) {
            // macOS
            pb = new ProcessBuilder("open", pdfFile.getAbsolutePath());
        } else {
            // Linux/Unix
            pb = new ProcessBuilder("xdg-open", pdfFile.getAbsolutePath());
        }
        
        pb.start();
    } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, 
            "Error al ejecutar comando del sistema: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}

// Bonus: Mostrar en explorador de archivos
private void showInFileExplorer(File pdfFile) {
    try {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder pb;
        
        if (os.contains("win")) {
            // Windows - seleccionar archivo en Explorer
            pb = new ProcessBuilder("explorer", "/select,", pdfFile.getAbsolutePath());
        } else if (os.contains("mac")) {
            // macOS - mostrar en Finder
            pb = new ProcessBuilder("open", "-R", pdfFile.getAbsolutePath());
        } else {
            // Linux - abrir directorio padre
            pb = new ProcessBuilder("xdg-open", pdfFile.getParent());
        }
        
        pb.start();
    } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, 
            "Error al mostrar en explorador: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void loadManualsFromDirectory(ManualNode parent, String resourcePath) {
    try {
        // Obtener la URL del recurso desde el classpath del módulo
        URL resourceUrl = getClass().getResource("/" + resourcePath);
        if (resourceUrl != null) {
            File dir = new File(resourceUrl.toURI());
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles((file) -> 
                    file.getName().toLowerCase().endsWith(".pdf"));
                if (files != null) {
                    for (File file : files) {
                        String name = file.getName().substring(0, 
                            file.getName().lastIndexOf('.'));
                        ManualNode node = new ManualNode(name, file);
                        parent.add(node);
                    }
                }
            }
        }
    } catch (URISyntaxException ex) {
        // Log error - carpeta no encontrada
        System.err.println("No se pudo cargar la carpeta: " + resourcePath);
    }
}

    private void refreshManuals() {
        initializeTree();
        manualsTree.updateUI();
    }
    
    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}

