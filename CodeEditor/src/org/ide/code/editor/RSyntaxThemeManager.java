package org.ide.code.editor;

import java.awt.Color;
import javax.swing.UIManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;

/**
 * Manejador de los temas y encargado de aplicarlos al editor de código del IDE
 * (RSyntaxTextArea). Estas clases son necesarias porque el componente
 * RSyntaxTextArea no cambia de tema automáticamente.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class RSyntaxThemeManager {

    private static RSyntaxThemeManager instance;

    private RSyntaxThemeManager() {
    }

    public static RSyntaxThemeManager getInstance() {
        if (instance == null) {
            instance = new RSyntaxThemeManager();
        }
        return instance;
    }

    public void applyAutoTheme(RSyntaxTextArea textArea) {
        if (isDarkLookAndFeel()) {
            applyDarkTheme(textArea);
        } else {
            applyLightTheme(textArea);
        }
    }

    /**
     * Tema claro: usa colores por defecto del RSyntaxTextArea
     */
    private void applyLightTheme(RSyntaxTextArea textArea) {
        // Solo aplicar colores básicos del sistema, mantener sintaxis por defecto
        Color bgColor = UIManager.getColor("TextArea.background");
        Color fgColor = UIManager.getColor("TextArea.foreground");
        Color caretColor = UIManager.getColor("TextArea.caretForeground");
        Color selectionBg = UIManager.getColor("TextArea.selectionBackground");

        if (bgColor != null) {
            textArea.setBackground(bgColor);
        }
        if (fgColor != null) {
            textArea.setForeground(fgColor);
        }
        if (caretColor != null) {
            textArea.setCaretColor(caretColor);
        }
        if (selectionBg != null) {
            textArea.setSelectionColor(selectionBg);
        }

        Color currentLineBg = bgColor != null ? adjustColor(bgColor, -8) : new Color(245, 245, 245);
        textArea.setCurrentLineHighlightColor(currentLineBg);
        textArea.setHighlightCurrentLine(true);

    }

    /**
     * Tema oscuro: fondo más oscuro + colores vibrantes de sintaxis
     */
    private void applyDarkTheme(RSyntaxTextArea textArea) {
        Color baseBg = UIManager.getColor("TextArea.background");
        Color editorBg = baseBg != null ? adjustColor(baseBg, -25) : new Color(30, 30, 30);
        Color fgColor = UIManager.getColor("TextArea.foreground");

        textArea.setBackground(editorBg);
        if (fgColor != null) {
            textArea.setForeground(fgColor);
        }
        textArea.setCaretColor(new Color(255, 255, 255));

        textArea.setCurrentLineHighlightColor(adjustColor(editorBg, 20));
        textArea.setHighlightCurrentLine(true);
        Color selectionBg = UIManager.getColor("TextArea.selectionBackground");
        if (selectionBg != null) {
            textArea.setSelectionColor(selectionBg);
        }

        // Aplicar colores de sintaxis vibrantes con verificaciones de seguridad
        try {
            SyntaxScheme scheme = textArea.getSyntaxScheme();

            scheme.getStyle(Token.RESERVED_WORD).foreground = new Color(200, 120, 255); // Púrpura brillante
            scheme.getStyle(Token.RESERVED_WORD_2).foreground = new Color(85, 255, 200); // Cian vibrante
            scheme.getStyle(Token.DATA_TYPE).foreground = new Color(78, 201, 176); // Verde intenso
            scheme.getStyle(Token.FUNCTION).foreground = new Color(255, 215, 100); // Amarillo oro (System, Scanner)
            scheme.getStyle(Token.LITERAL_STRING_DOUBLE_QUOTE).foreground = new Color(255, 180, 120); // Naranja cálido
            scheme.getStyle(Token.LITERAL_NUMBER_DECIMAL_INT).foreground = new Color(150, 255, 150); // Verde menta
            scheme.getStyle(Token.LITERAL_NUMBER_FLOAT).foreground = new Color(150, 255, 150);
            scheme.getStyle(Token.COMMENT_EOL).foreground = new Color(128, 190, 100); // Verde olivo
            scheme.getStyle(Token.COMMENT_MULTILINE).foreground = new Color(128, 190, 100);
            scheme.getStyle(Token.COMMENT_MARKUP).foreground = new Color(128, 190, 100);
            scheme.getStyle(Token.COMMENT_KEYWORD).foreground = new Color(128, 190, 100);
            scheme.getStyle(Token.COMMENT_DOCUMENTATION).foreground = new Color(128, 190, 100);
            scheme.getStyle(Token.OPERATOR).foreground = new Color(255, 255, 255); // Blanco puro
            scheme.getStyle(Token.SEPARATOR).foreground = new Color(255, 255, 100); // Amarillo limón (paréntesis)
            scheme.getStyle(Token.IDENTIFIER).foreground = new Color(240, 240, 240); // Blanco suave
            scheme.getStyle(Token.ANNOTATION).foreground = new Color(255, 220, 120); // Amarillo dorado

            textArea.setSyntaxScheme(scheme);

        } catch (Exception e) {
            System.err.println("[ERROR] Fallo aplicando tema oscuro: " + e.getMessage());
        }
    }

    private boolean isDarkLookAndFeel() {
        Color bgColor = UIManager.getColor("Panel.background");
        if (bgColor != null) {
            int brightness = (int) (0.299 * bgColor.getRed() + 0.587 * bgColor.getGreen() + 0.114 * bgColor.getBlue());
            return brightness < 128;
        }
        return false;
    }

    private Color adjustColor(Color color, int adjustment) {
        int r = Math.max(0, Math.min(255, color.getRed() + adjustment));
        int g = Math.max(0, Math.min(255, color.getGreen() + adjustment));
        int b = Math.max(0, Math.min(255, color.getBlue() + adjustment));
        return new Color(r, g, b);
    }
}
