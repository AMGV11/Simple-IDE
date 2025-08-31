package org.ide.help;

import java.awt.Component;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Clase encargada de cargar los iconos y configuraciones en el arbol de
 * manuales.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class ManualTreeCellRenderer extends DefaultTreeCellRenderer {

    private Icon folderIcon;
    private Icon pdfIcon;

    public ManualTreeCellRenderer() {
        super();
        loadCustomIcons();
    }

    private void loadCustomIcons() {
        try {
            URL folderIconUrl = getClass().getResource("PackageIcon.png");
            URL pdfIconUrl = getClass().getResource("PDFIcon.png");

            if (folderIconUrl != null) {
                folderIcon = new ImageIcon(folderIconUrl);
            } else {
                folderIcon = UIManager.getIcon("FileView.directoryIcon");
                if (folderIcon == null) {
                    folderIcon = getDefaultClosedIcon();
                }
            }

            if (pdfIconUrl != null) {
                pdfIcon = new ImageIcon(pdfIconUrl);
            } else {
                pdfIcon = UIManager.getIcon("FileView.fileIcon");
                if (pdfIcon == null) {
                    pdfIcon = getDefaultLeafIcon();
                }
            }

        } catch (Exception e) {
            System.err.println("Error cargando iconos personalizados: " + e.getMessage());
            // Usar iconos por defecto en caso de error
            folderIcon = getDefaultClosedIcon();
            pdfIcon = getDefaultLeafIcon();
        }
    }

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        if (value instanceof ManualNode) {
            ManualNode node = (ManualNode) value;

            if (node.hasPDF()) {
                setIcon(pdfIcon);
            } else {
                if (expanded) {
                    setIcon(folderIcon);
                } else {
                    setIcon(folderIcon);
                }
            }

            setToolTipText(node.getTooltipText());
        } else {
            setIcon(getDefaultLeafIcon());
        }

        return this;
    }

    public void setFolderIcon(Icon icon) {
        this.folderIcon = icon;
    }

    public void setPdfIcon(Icon icon) {
        this.pdfIcon = icon;
    }
}
