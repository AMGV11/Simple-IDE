// PDFViewerTopComponent.java - Visor PDF básico funcional
package org.ide.help;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@ConvertAsProperties(
    dtd = "-//org.ide.help//PDFViewer//EN",
    autostore = false
)
@TopComponent.Description(
    preferredID = "PDFViewerTopComponent",
    persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "org.ide.help.PDFViewerTopComponent")
@ActionReference(path = "Menu/Window")
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_PDFViewerAction",
        preferredID = "PDFViewerActionTopComponent"
)
@Messages({
    "CTL_PDFViewerAction=PDF Viewer",
    "CTL_PDFViewerTopComponent=PDF Viewer",
    "HINT_PDFViewerTopComponent=Visor de archivos PDF"
})
public final class PDFViewerTopComponent extends TopComponent {

    private JScrollPane scrollPane;
    private JPanel pdfPanel;
    private JToolBar toolBar;
    private PDDocument currentDocument;
    private int currentPage = 0;
    private float zoomLevel = 1.0f;
    private JTextField pageField;
    private JLabel totalPagesLabel;
    private PDFRenderer renderer;

    public PDFViewerTopComponent() {
        initComponents();
        setName(Bundle.CTL_PDFViewerTopComponent());
        setToolTipText(Bundle.HINT_PDFViewerTopComponent());
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Crear toolbar con controles básicos
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        JButton prevButton = new JButton("◀");
        prevButton.setToolTipText("Página anterior");
        prevButton.addActionListener(e -> previousPage());
        toolBar.add(prevButton);
        toolBar.add(new JLabel(" Página: "));
        pageField = new JTextField("1", 3);
        pageField.addActionListener(e -> goToPage());
        toolBar.add(pageField);

        totalPagesLabel = new JLabel(" / 0 ");
        toolBar.add(totalPagesLabel);
        JButton nextButton = new JButton("▶");
        nextButton.setToolTipText("Página siguiente");
        nextButton.addActionListener(e -> nextPage());
        toolBar.add(nextButton);
        toolBar.addSeparator();

        JButton zoomInButton = new JButton("🔍+");
        zoomInButton.setToolTipText("Acercar");
        zoomInButton.addActionListener(e -> zoomIn());
        toolBar.add(zoomInButton);
        
        JButton zoomOutButton = new JButton("🔍-");
        zoomOutButton.setToolTipText("Alejar");
        zoomOutButton.addActionListener(e -> zoomOut());
        toolBar.add(zoomOutButton);
        
        JButton fitButton = new JButton("Ajustar");
        fitButton.setToolTipText("Ajustar a ventana");
        fitButton.addActionListener(e -> fitToWindow());
        toolBar.add(fitButton);
        add(toolBar, BorderLayout.NORTH);

        pdfPanel = new JPanel();
        pdfPanel.setLayout(new BorderLayout());
        scrollPane = new JScrollPane(pdfPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        scrollPane.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Forzar el scroll vertical
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getValue() + (e.getWheelRotation() * 20));
            }
        });
    }

    public void loadPDF(File pdfFile) {
        try {
            if (currentDocument != null) {
                currentDocument.close();
            }
            currentDocument = PDDocument.load(pdfFile);
            renderer = new PDFRenderer(currentDocument);
            currentPage = 0;
            zoomLevel = 1.0f;
            setDisplayName(pdfFile.getName());
            totalPagesLabel.setText(" / " + currentDocument.getNumberOfPages());
            pageField.setText("1");
            renderCurrentPage();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar el PDF: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

private void renderCurrentPage() {
    if (currentDocument == null) return;

    try {
        BufferedImage image = renderer.renderImageWithDPI(
            currentPage, 96 * zoomLevel, ImageType.RGB);

        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null) {
                    g.drawImage(image, 0, 0, this);
                }
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(image.getWidth(), image.getHeight());
            }
        };
        pdfPanel.removeAll();
        pdfPanel.add(imagePanel, BorderLayout.CENTER);
        pdfPanel.revalidate();
        pdfPanel.repaint();
        pageField.setText(String.valueOf(currentPage + 1));

    } catch (IOException e) {
    }
}

    private void previousPage() {
        if (currentDocument != null && currentPage > 0) {
            currentPage--;
            renderCurrentPage();
        }
    }

    private void nextPage() {
        if (currentDocument != null && currentPage < currentDocument.getNumberOfPages() - 1) {
            currentPage++;
            renderCurrentPage();
        }
    }

    private void goToPage() {
        if (currentDocument == null) return;
        
        try {
            int page = Integer.parseInt(pageField.getText()) - 1;
            if (page >= 0 && page < currentDocument.getNumberOfPages()) {
                currentPage = page;
                renderCurrentPage();
            } else {
                pageField.setText(String.valueOf(currentPage + 1));
            }
        } catch (NumberFormatException e) {
            pageField.setText(String.valueOf(currentPage + 1));
        }
    }

    private void zoomIn() {
        zoomLevel *= 1.25f;
        renderCurrentPage();
    }

    private void zoomOut() {
        zoomLevel /= 1.25f;
        renderCurrentPage();
    }

    private void fitToWindow() {
        zoomLevel = 1.0f;
        renderCurrentPage();
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        if (currentDocument != null) {
            try {
                currentDocument.close();
            } catch (IOException e) {
            }
        }
    }

    // Método estático para abrir un PDF desde cualquier lugar
    public static void openPDF(File pdfFile) {
        PDFViewerTopComponent viewer = new PDFViewerTopComponent();
        viewer.loadPDF(pdfFile);
        viewer.open();
        viewer.requestActive();
    }
    
    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }
}