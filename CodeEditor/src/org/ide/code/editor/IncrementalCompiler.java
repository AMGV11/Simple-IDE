package org.ide.code.editor;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

 /**
 * Compilador incremental que solo recompila archivos modificados
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class IncrementalCompiler {
    private static IncrementalCompiler instance;
    
    // Cache de metadatos de archivos
    private final Map<String, FileMetadata> fileCache = new ConcurrentHashMap<>();
    
    // Grafo de dependencias entre archivos
    private final Map<String, Set<String>> dependencyGraph = new ConcurrentHashMap<>();
    
    // Archivo donde se persiste el cache
    private static final String CACHE_FILE = ".compile_cache";
    
    public static IncrementalCompiler getInstance() {
        if (instance == null) {
            instance = new IncrementalCompiler();
        }
        return instance;
    }
    
    private IncrementalCompiler() {
    }
    
    public File compile() {
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        if (activeTC instanceof CodeEditorTopComponent editorTC) {
            FileObject currentFile = editorTC.getCurrentFO();
            File projectRoot = findProjectRoot(FileUtil.toFile(currentFile));
            
            if (projectRoot != null) {
                try {
                    if (!compileProjectIncremental(projectRoot)){
                    return null;
                    }
                    System.out.println("[COMPILER] Compilación incremental completada.");
                    return projectRoot;
                } catch (IOException | InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                StatusDisplayer.getDefault().setStatusText("No se encontró el proyecto raíz.");
            }
        }
        return null;
    }
    
    private File findProjectRoot(File file) {
        File current = file;
        while (current != null && !new File(current, "src").exists()) {
            current = current.getParentFile();
        }
        return current;
    }
    
    private boolean compileProjectIncremental(File projectRoot) throws IOException, InterruptedException {
        loadCache(projectRoot);
        
        File srcDir = new File(projectRoot, "src");
        File binDir = new File(projectRoot, "bin");
        
        if (!binDir.exists()) {
            binDir.mkdirs();
        }
        
        List<File> allJavaFiles = new ArrayList<>();
        collectJavaFiles(srcDir, allJavaFiles);
        
        if (allJavaFiles.isEmpty()) {
            StatusDisplayer.getDefault().setStatusText("No hay archivos .java para compilar.");
            return false;
        }
        
        Set<String> modifiedFiles = detectModifiedFiles(allJavaFiles);
        
        if (modifiedFiles.isEmpty()) {
            StatusDisplayer.getDefault().setStatusText("No hay cambios, compilación omitida.");
            return true;
        }
        
        updateDependencyGraph(allJavaFiles);
        Set<String> filesToCompile = getFilesToCompile(modifiedFiles);
        
        System.out.println("[COMPILER] Archivos a recompilar: " + filesToCompile.size() + " de " + allJavaFiles.size());
        
        compileFiles(new ArrayList<>(filesToCompile), binDir);
        
        updateCache(allJavaFiles);
        saveCache(projectRoot);
        return true;
    }
    
    private Set<String> detectModifiedFiles(List<File> javaFiles) throws IOException {
        Set<String> modifiedFiles = new HashSet<>();
        
        for (File file : javaFiles) {
            String filePath = file.getAbsolutePath();
            String currentHash = calculateFileHash(file);
            long lastModified = file.lastModified();
            
            FileMetadata cached = fileCache.get(filePath);
            
            if (cached == null || 
                !cached.hash.equals(currentHash) || 
                cached.lastModified != lastModified) {
                modifiedFiles.add(filePath);
            }
        }
        
        return modifiedFiles;
    }
    
    private void updateDependencyGraph(List<File> javaFiles) throws IOException {
        for (File file : javaFiles) {
            String filePath = file.getAbsolutePath();
            Set<String> imports = extractImports(file);
            dependencyGraph.put(filePath, imports);
        }
    }
    
    private Set<String> extractImports(File file) throws IOException {
        Set<String> imports = new HashSet<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("import ") && !line.contains("java.") && !line.contains("javax.")) {
                    String importClass = line.substring(7, line.indexOf(';')).trim();
                    imports.add(importClass);
                }
                if (line.startsWith("public class") || line.startsWith("class") || 
                    line.startsWith("public interface") || line.startsWith("interface")) {
                    break;
                }
            }
        }
        
        return imports;
    }
    
    private Set<String> getFilesToCompile(Set<String> modifiedFiles) {
        Set<String> filesToCompile = new HashSet<>(modifiedFiles);
        Queue<String> queue = new LinkedList<>(modifiedFiles);
        
        while (!queue.isEmpty()) {
            String modifiedFile = queue.poll();
            
            // Buscar archivos que dependen de este archivo modificado
            for (Map.Entry<String, Set<String>> entry : dependencyGraph.entrySet()) {
                String file = entry.getKey();
                Set<String> dependencies = entry.getValue();
                
                // Si este archivo importa el archivo modificado
                if (dependencies.stream().anyMatch(dep -> isRelatedClass(modifiedFile, dep))) {
                    if (!filesToCompile.contains(file)) {
                        filesToCompile.add(file);
                        queue.offer(file);
                    }
                }
            }
        }
        
        return filesToCompile;
    }
    
    private boolean isRelatedClass(String filePath, String importClass) {
        // Convertir path del archivo a nombre de clase
        String className = filePath.substring(filePath.lastIndexOf("src") + 4)
                                  .replace(File.separator, ".")
                                  .replace(".java", "");
        
        return className.equals(importClass) || className.endsWith("." + importClass);
    }
    
    private void compileFiles(List<String> sourceFiles, File binDir) throws IOException, InterruptedException {
        if (sourceFiles.isEmpty()) return;
        
        List<String> command = new ArrayList<>();
        command.add("javac");
        command.add("-g");
        command.add("-d");
        command.add(binDir.getAbsolutePath());
        command.add("-cp");
        command.add(binDir.getAbsolutePath()); // Incluir clases ya compiladas
        command.addAll(sourceFiles);
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            reader.lines().forEach(System.out::println);
        }
        
        int result = process.waitFor();
        StatusDisplayer.getDefault().setStatusText(
            "Compilación incremental " + (result == 0 ? "exitosa." : "con errores.") +
            " (" + sourceFiles.size() + " archivos)"
        );
    }
    
    private void updateCache(List<File> javaFiles) throws IOException {
        for (File file : javaFiles) {
            String filePath = file.getAbsolutePath();
            String hash = calculateFileHash(file);
            long lastModified = file.lastModified();
            
            fileCache.put(filePath, new FileMetadata(hash, lastModified));
        }
    }
    
    private String calculateFileHash(File file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            
            StringBuilder sb = new StringBuilder();
            for (byte b : md.digest()) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("MD5 no disponible", e);
        }
    }
    
    private void collectJavaFiles(File dir, List<File> collector) {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File f : files) {
            if (f.isDirectory()) {
                collectJavaFiles(f, collector);
            } else if (f.getName().endsWith(".java")) {
                collector.add(f);
            }
        }
    }
       
    private void saveCache(File projectRoot) {
        try {
            File cacheFile = new File(projectRoot, CACHE_FILE);
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheFile))) {
                oos.writeObject(new HashMap<>(fileCache));
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Fallo guardando cache: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadCache(File projectRoot) {
        File cacheFile = new File(projectRoot, CACHE_FILE);
        if (cacheFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheFile))) {
                Map<String, FileMetadata> loaded = (Map<String, FileMetadata>) ois.readObject();
                fileCache.putAll(loaded);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("[ERROR] Fallo cargando cache: " + e.getMessage());
            }
        }
    }
    
    /**
     * Fuerza una recompilación completa limpiando el cache
     */
    public void forceFullCompile() {
        fileCache.clear();
        dependencyGraph.clear();
        compile();
    }
    
    /**
     * Limpia archivos .class órfanos (sin .java correspondiente)
     * @param projectRoot
     */
    public void cleanOrphanedClasses(File projectRoot) {
        File binDir = new File(projectRoot, "bin");
        if (!binDir.exists()) return;
        
        Set<String> javaFiles = new HashSet<>();
        collectJavaFileNames(new File(projectRoot, "src"), javaFiles);
        
        cleanDirectory(binDir, javaFiles);
    }
    
    private void collectJavaFileNames(File dir, Set<String> collector) {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File f : files) {
            if (f.isDirectory()) {
                collectJavaFileNames(f, collector);
            } else if (f.getName().endsWith(".java")) {
                collector.add(f.getName().replace(".java", ".class"));
            }
        }
    }
    
    private void cleanDirectory(File dir, Set<String> validClasses) {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File f : files) {
            if (f.isDirectory()) {
                cleanDirectory(f, validClasses);
            } else if (f.getName().endsWith(".class") && !validClasses.contains(f.getName())) {
                f.delete();
                System.out.println("[COMPILER] Eliminado archivo órfano: " + f.getName());
            }
        }
    }
    
    /**
     * Clase interna para almacenar metadatos de archivos
     */
    private static class FileMetadata implements Serializable {
        private static final long serialVersionUID = 1L;
        
        final String hash;
        final long lastModified;
        
        FileMetadata(String hash, long lastModified) {
            this.hash = hash;
            this.lastModified = lastModified;
        }
    }
}