// Test.java - Clase para probar el visor
package org.ide.help;

import java.io.File;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.ide.help.PDFViewerTopComponent;

public class Test {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Para probar fuera de NetBeans Platform
            JFrame frame = new JFrame("Test PDF Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            
            PDFViewerTopComponent viewer = new PDFViewerTopComponent();
            frame.add(viewer);
            
            // Cargar un PDF de prueba
            File testPdf = new File("ruta/a/tu/pdf/test.pdf");
            if (testPdf.exists()) {
                //viewer.loadPDF(testPdf);
            }
            
            frame.setVisible(true);
        });
    }
}
