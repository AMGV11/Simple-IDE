/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.code.editor;

import org.fife.ui.rsyntaxtextarea.parser.*;
import org.fife.ui.rsyntaxtextarea.*;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 *
 * @author anton
 */
public class JavacParser implements Parser {

    @Override
    public ParseResult parse(RSyntaxDocument doc, String style) {
        DefaultParseResult result = new DefaultParseResult(this);
        try {
            String text = doc.getText(0, doc.getLength());

            // Crear archivo temporal con el contenido del editor
            Path tempDir = Files.createTempDirectory("javac-test");
            Path tempFile = tempDir.resolve("TempClass.java");
            Files.writeString(tempFile, text);

            // Ejecutar javac sobre el archivo
            ProcessBuilder pb = new ProcessBuilder("javac", tempFile.toAbsolutePath().toString());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Leer la salida del compilador
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            Pattern errorPattern = Pattern.compile("TempClass\\.java:(\\d+): error: (.+)");
            while ((line = reader.readLine()) != null) {
                Matcher matcher = errorPattern.matcher(line);
                if (matcher.find()) {
                    int lineNumber = Integer.parseInt(matcher.group(1)) - 1; // Línea en 0-index
                    String message = matcher.group(2);
                    ParserNotice notice = new DefaultParserNotice(this, message, lineNumber);
                    result.addNotice(notice);
                }
            }

            process.waitFor();
            // Eliminar archivos temporales
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(tempDir);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public ExtendedHyperlinkListener getHyperlinkListener() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public URL getImageBase() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
