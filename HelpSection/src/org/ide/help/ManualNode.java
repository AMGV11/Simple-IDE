// ManualNode.java - Nodo del árbol que soporta archivos y resources
package org.ide.help;

import java.io.File;
import javax.swing.tree.DefaultMutableTreeNode;

public class ManualNode extends DefaultMutableTreeNode {

    private File pdfFile;           // Para PDFs del sistema de archivos
    private String resourcePath;    // Para PDFs desde resources
    private String displayName;

    public ManualNode(String displayName) {
        super(displayName);
        this.displayName = displayName;
        this.pdfFile = null;
        this.resourcePath = null;
    }

    public ManualNode(String displayName, File pdfFile) {
        super(displayName);
        this.displayName = displayName;
        this.pdfFile = pdfFile;
        this.resourcePath = null;
    }

    public ManualNode(String displayName, String resourcePath) {
        super(displayName);
        this.displayName = displayName;
        this.pdfFile = null;
        this.resourcePath = resourcePath;
    }

    public File getPdfFile() {
        return pdfFile;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean hasPDF() {
        return pdfFile != null || resourcePath != null;
    }

    public boolean isFileSystemPDF() {
        return pdfFile != null;
    }

    public boolean isResourcePDF() {
        return resourcePath != null;
    }

    public String getTooltipText() {
        if (pdfFile != null) {
            return "Archivo: " + pdfFile.getAbsolutePath();
        } else if (resourcePath != null) {
            return "Recurso: " + resourcePath;
        } else {
            return "Categoría: " + displayName;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}
