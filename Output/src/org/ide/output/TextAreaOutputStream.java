package org.ide.output;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

public class TextAreaOutputStream extends OutputStream {

    private final JTextPane textPane;
    private final StyledDocument doc;
    private final StringBuilder sb = new StringBuilder();
    private final String prefix;

    public TextAreaOutputStream(JTextPane textPane, String prefix) {
        this.textPane = textPane;
        this.doc = textPane.getStyledDocument();
        this.prefix = "";//prefix + "> ";
    }

    @Override
    public void write(int b) throws IOException {
        if (b == '\r') {
            // Ignorar retorno de carro
            return;
        }

        if (b == '\n') {
            final String text = sb.toString() + "\n";
            SwingUtilities.invokeLater(() -> {
                try {
                    doc.insertString(doc.getLength(), prefix + text, null);
                    textPane.setCaretPosition(doc.getLength());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            });
            sb.setLength(0);
            return;
        }

        sb.append((char) b);
    }
}
