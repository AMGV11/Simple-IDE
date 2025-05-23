/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.code.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

@ActionID(
    category = "Build",
    id = "org.ide.editor.actions.CompileRunProjectAction"
)
@ActionRegistration(
    iconBase = "org/ide/code/editor/CompAndExec.png",
    displayName = "Compilar y Ejecutar"
)
@ActionReference(path = "Toolbars/File", position = 500)
public final class CompileRunProjectAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        TopComponent activeTC = TopComponent.getRegistry().getActivated();

        if (activeTC instanceof CodeEditorTopComponent) {
            CodeEditorTopComponent editorTC = (CodeEditorTopComponent) activeTC;
            FileObject currentFile = editorTC.getCurrentFO();
            File projectRoot = findProjectRoot(FileUtil.toFile(currentFile));

            if (projectRoot != null) {
                try {
                    compileProject(projectRoot);
                    runProject(projectRoot);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                StatusDisplayer.getDefault().setStatusText("No se encontró el proyecto raíz.");
            }
        }
    }

    private File findProjectRoot(File file) {
        File current = file;
        while (current != null && !new File(current, "src").exists()) {
            current = current.getParentFile();
        }
        return current;
    }

    private void compileProject(File projectRoot) throws IOException, InterruptedException {
        File srcDir = new File(projectRoot, "src");
        File binDir = new File(projectRoot, "bin");

        if (!binDir.exists()) {
            binDir.mkdirs();
        }

        List<String> sourceFiles = new ArrayList<>();
        collectJavaFiles(srcDir, sourceFiles);

        if (sourceFiles.isEmpty()) {
            StatusDisplayer.getDefault().setStatusText("No hay archivos .java para compilar.");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("javac");
        command.add("-d");
        command.add(binDir.getAbsolutePath());
        command.addAll(sourceFiles);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            reader.lines().forEach(System.out::println);
        }

        int result = process.waitFor();
        StatusDisplayer.getDefault().setStatusText("Compilación " + (result == 0 ? "exitosa." : "con errores."));
    }

    private void collectJavaFiles(File dir, List<String> collector) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                collectJavaFiles(f, collector);
            } else if (f.getName().endsWith(".java")) {
                collector.add(f.getAbsolutePath());
            }
        }
    }

    private void runProject(File projectRoot) throws IOException {
        File binDir = new File(projectRoot, "bin");

        // Detectar la clase principal automáticamente es complejo, así que por ahora asumiremos un nombre fijo:
        File mainClassFile = findMainClass(new File(projectRoot, "src"));
        if (mainClassFile == null) {
            StatusDisplayer.getDefault().setStatusText("No se encontró clase con método main.");
            return;
        }

        String mainClass = getFullyQualifiedName(mainClassFile, new File(projectRoot, "src"));

        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-cp");
        command.add(binDir.getAbsolutePath());
        command.add(mainClass);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(System.out::println);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }).start();

        StatusDisplayer.getDefault().setStatusText("Ejecutando " + mainClass + "...");
    }

    private File findMainClass(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return null;
        for (File f : files) {
            if (f.isDirectory()) {
                File result = findMainClass(f);
                if (result != null) return result;
            } else if (f.getName().endsWith(".java")) {
                try {
                    String content = Files.readString(f.toPath());
                    if (content.contains("public static void main")) {
                        return f;
                    }
                } catch (IOException e) {
                    // Ignorar
                }
            }
        }
        return null;
    }

    private String getFullyQualifiedName(File javaFile, File srcRoot) {
        Path relative = srcRoot.toPath().relativize(javaFile.toPath());
        String path = relative.toString().replace(File.separatorChar, '.');
        return path.substring(0, path.length() - ".java".length());
    }
}