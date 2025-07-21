// ManualNode.java - Nodo para el árbol de manuales
package org.ide.help;

import java.io.File;
import javax.swing.tree.DefaultMutableTreeNode;

public class ManualNode extends DefaultMutableTreeNode {
    private String title;
    private File pdfFile;
    private String category;
    private String description;

    public ManualNode(String title) {
        super(title);
        this.title = title;
    }

    public ManualNode(String title, File pdfFile) {
        this(title);
        this.pdfFile = pdfFile;
    }

    public String getTitle() {
        return title;
    }

    public File getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(File pdfFile) {
        this.pdfFile = pdfFile;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean isLeaf() {
        return pdfFile != null;
    }

    @Override
    public String toString() {
        return title;
    }
}