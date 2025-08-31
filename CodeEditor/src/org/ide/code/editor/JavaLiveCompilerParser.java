package org.ide.code.editor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ExtendedHyperlinkListener;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.openide.filesystems.FileObject;

/**
 * Componente encargado de reflejar los posibles errores de compilación en el
 * código con el que se este trabjando. Esto se hace de manera dinámica mediante
 * este compilador.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class JavaLiveCompilerParser implements Parser {

    private final FileObject fileObject;
    private final File projectBinDir;
    private final List<ParserNotice> notices = new ArrayList<>();

    public JavaLiveCompilerParser(FileObject fileObject, File projectBinDir) {
        this.fileObject = fileObject;
        this.projectBinDir = projectBinDir;
    }

    @Override
    public ParseResult parse(RSyntaxDocument doc, String style) {
        notices.clear();
        DefaultParseResult result = new DefaultParseResult(this);

        String code;
        try {
            code = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            return result;
        }
        String fileName = fileObject.getName() + ".java";

        Pattern p = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher m = p.matcher(code);
        if (m.find() && !m.group(1).equals(fileObject.getName())) {
            notices.add(new DefaultParserNotice(
                    this,
                    "La clase pública '" + m.group(1)
                    + "' no coincide con el nombre del archivo '" + fileName + "'",
                    0
            ));
        }

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "liveCompile");
        tempDir.mkdirs();
        File tempFile = new File(tempDir, fileName);
        try (BufferedWriter w = new BufferedWriter(new FileWriter(tempFile))) {
            w.write(code);
        } catch (IOException ex) {
            notices.add(new DefaultParserNotice(this, "Error al escribir temp file: " + ex, 0));
            return finish(result);
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            notices.add(new DefaultParserNotice(this,
                    "No se encontró JavaCompiler. Asegúrate de ejecutar con un JDK, no un JRE.",
                    0));
            return finish(result);
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, null, null);

        List<String> options = new ArrayList<>();
        if (projectBinDir.exists()) {
            options.addAll(Arrays.asList("-classpath", projectBinDir.getAbsolutePath()));
        }

        Iterable<? extends JavaFileObject> units
                = fm.getJavaFileObjectsFromFiles(Collections.singletonList(tempFile));
        JavaCompiler.CompilationTask task
                = compiler.getTask(null, fm, diagnostics, options, null, units);
        task.call();

        for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
            JavaFileObject src = d.getSource();
            if (src != null && src.getName().endsWith(fileName)
                    && d.getKind() == Diagnostic.Kind.ERROR) {
                int line = (int) d.getLineNumber() - 1;
                String msg = d.getMessage(null);
                notices.add(new DefaultParserNotice(this, msg, line));
            }
        }

        try {
            fm.close();
        } catch (IOException ignored) {
        }

        return finish(result);
    }

    private ParseResult finish(DefaultParseResult result) {
        for (ParserNotice pn : notices) {
            result.addNotice(pn);
        }
        return result;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public URL getImageBase() {
        return null;
    }

    @Override
    public ExtendedHyperlinkListener getHyperlinkListener() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
