/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.code.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ide.code.debugger.DebugLauncher;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

/**
 *
 * @author anton
 */
public class Compile {
    private static Compile instance;
    
    public static Compile getInstance() {
        instance = new Compile();

        return instance;
    }
    
    public void compile(){
        TopComponent activeTC = TopComponent.getRegistry().getActivated();

        if (activeTC instanceof CodeEditorTopComponent) {
            CodeEditorTopComponent editorTC = (CodeEditorTopComponent) activeTC;
            FileObject currentFile = editorTC.getCurrentFO();
            File projectRoot = findProjectRoot(FileUtil.toFile(currentFile));

            if (projectRoot != null) {
                try {
                    compileProject(projectRoot);
                    System.out.println("Se ha compilado " + currentFile.getName() + ".");
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
        command.add("-g");
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
    
    private String removeComments(String code) {
        // Elimina comentarios de línea
        code = code.replaceAll("//.*", "");
        
        // Elimina comentarios multilínea
        Pattern pattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(code);
        code = matcher.replaceAll("");
        
        return code;
}
}

