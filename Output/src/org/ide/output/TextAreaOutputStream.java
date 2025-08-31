package org.ide.output;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Clase encargada del stream de output que van a consola.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class TextAreaOutputStream extends OutputStream {

    private final JTextPane textPane;
    private final String name;
    private final StringBuilder buffer;

    public TextAreaOutputStream(JTextPane textPane, String name) {
        this.textPane = textPane;
        this.name = name;
        this.buffer = new StringBuilder();
    }

    @Override
    public void write(int b) throws IOException {
        synchronized (buffer) {
            buffer.append((char) b);

            if (b == '\n' || buffer.length() > 1000 || b == '\r'){
                flush();
            }
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        synchronized (buffer) {
            String text = new String(b, off, len, StandardCharsets.UTF_8);
            buffer.append(text);

            if (text.contains("\n") || buffer.length() > 1000) {
                flush();
            }
        }
    }

    @Override
    public void flush() throws IOException {
        synchronized (buffer) {
            if (buffer.length() > 0) {
                String text = buffer.toString();
                buffer.setLength(0);

                // Filtrar mensajes no deseados
                if (!shouldFilterMessage(text)) {
                    final String finalText = text;
                    SwingUtilities.invokeLater(() -> {
                        try {
                            Document doc = textPane.getDocument();
                            doc.insertString(doc.getLength(), finalText, null);

                            // Auto-scroll al final
                            textPane.setCaretPosition(doc.getLength());
                        } catch (BadLocationException ex) {
                            System.err.println("[ERROR] Fallo al mandar datos a consola: " + ex.getMessage());
                        }
                    });
                }
            }
        }
        super.flush();
    }

    private boolean shouldFilterMessage(String text) {
        String trimmedText = text.trim();

        // Filtrar mensajes específicos
        return trimmedText.startsWith("Picked up JAVA_TOOL_OPTIONS:")
                || trimmedText.startsWith("Picked up _JAVA_OPTIONS:")
                || trimmedText.startsWith("OpenJDK 64-Bit Server VM warning:")
                || trimmedText.startsWith("--add-opens=java.base/java.net=ALL-UNNAMED")
                || trimmedText.startsWith("--add-opens=java.desktop/javax.swing=ALL-UNNAMED")
                || trimmedText.startsWith("--add-exports=java.desktop/sun.awt=ALL-UNNAMED")
                || trimmedText.startsWith("-Djava.security.manager=allow")
                || trimmedText.startsWith("-Dnetbeans.security.nosecurity=true")
                || trimmedText.startsWith("-DTopSecurityManager.disable=true");

    }
    
    @Override
    public void close() throws IOException {
        flush();
        super.close();
    }
}
