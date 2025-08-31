package org.ide.code.editor;

import org.fife.rsta.ac.java.buildpath.LibraryInfo;
import org.fife.rsta.ac.java.classreader.ClassFile;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import org.fife.rsta.ac.java.PackageMapNode;

/**
 * Clase auxiliar que permite que las herramientas como el autocompletado del
 * IDE puedan trabajar con el sistema de modulos de manera transparente.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class JDK9ClasspathLibraryInfo extends LibraryInfo {

    private Map<String, ClassFile> classNameToClassFile;
    private Map<String, String> classNameToFullyQualified;
    private Map<String, String> classNameToModule;

    private void cacheFiles(List<Path> paths) throws IOException {
        for (Path p : paths) {
            if (Files.isDirectory(p)) {
                cacheFiles(Files.list(p).collect(Collectors.toList()));
            } else {
                if (p.toString().endsWith(".class")) {
                    StringBuilder className = new StringBuilder();
                    StringBuilder fqName = new StringBuilder();
                    for (int i = 2; i < p.getNameCount(); i++) {
                        if (className.length() > 0) {
                            className.append("/");
                            fqName.append(".");
                        }
                        className.append(p.getName(i).toString());
                        if (!p.getName(i).toString().endsWith(".class")) {
                            fqName.append(p.getName(i).toString());
                        } else {
                            fqName.append(p.getName(i).toString(), 0, p.getName(i).toString().lastIndexOf(".class"));
                        }
                    }

                    classNameToModule.put(className.toString(), p.getName(1).toString());
                    String entryName = className.toString();
                    classNameToClassFile.put(entryName, null);
                    classNameToFullyQualified.put(entryName, fqName.toString());
                }

            }
        }
    }

    private Path pathToJRE;
    private Path pathToJrt;

    public JDK9ClasspathLibraryInfo() {
        setSourceLocation(null);
        classNameToClassFile = new HashMap<>();
        classNameToFullyQualified = new HashMap<>();
        classNameToModule = new HashMap<>();

        pathToJRE = Paths.get(System.getProperty("java.home"));
        pathToJrt = pathToJRE.resolve("lib").resolve("jrt-fs.jar");

        if (Files.exists(pathToJrt)) {
            try (URLClassLoader loader = new URLClassLoader(new URL[]{pathToJrt.toUri().toURL()}); FileSystem fs = FileSystems.newFileSystem(URI.create("jrt:/"),
                    Collections.emptyMap(),
                    loader)) {
                List<Path> paths = Files.list(fs.getPath("/modules")).collect(Collectors.toList());
                cacheFiles(paths);
            } catch (Exception ex) {
                System.err.println("Error loading JDK9+ module information: " + ex.getMessage());
            }
        }
    }

    public int compareTo(LibraryInfo o) {
        if (o == this) {

            return 0;
        }
        int res = -1;

        if (o instanceof JDK9ClasspathLibraryInfo) {
            JDK9ClasspathLibraryInfo other = (JDK9ClasspathLibraryInfo) o;
            res = classNameToClassFile.size() - other.classNameToClassFile.size();
            if (res == 0) {
                for (Iterator i = classNameToClassFile.keySet().iterator(); i.hasNext();) {
                    String key = (String) i.next();
                    if (!other.classNameToClassFile.containsKey(key)) {
                        res = -1;
                        break;
                    }
                }
            }
        }

        return res;

    }

    @Override
    public ClassFile createClassFile(String entryName) throws IOException {

        ClassFile cf = null;
        if (classNameToClassFile.containsKey(entryName)) {
            cf = classNameToClassFile.get(entryName);
            if (cf == null) {
                cf = createClassFileImpl(entryName);
                classNameToClassFile.put(entryName, cf);
            }
        }
        return cf;
    }

    private ClassFile createClassFileImpl(String res) throws IOException {

        ClassFile cf = null;
        String module = classNameToModule.get(res);

        if (module != null && !module.isEmpty()) {
            try (URLClassLoader loader = new URLClassLoader(new URL[]{pathToJrt.toUri().toURL()}); FileSystem fs = FileSystems.newFileSystem(URI.create("jrt:/"),
                    Collections.emptyMap(),
                    loader)) {
                byte[] result = Files.readAllBytes(fs.getPath("/modules/" + module + "/" + res));
                if (result.length > 0) {
                    try {
                        DataInputStream din = new DataInputStream(new ByteArrayInputStream(result));
                        cf = new ClassFile(din);
                    } catch (Exception ex) {

                    }
                }
            } catch (Exception ex) {
            }
        }
        return cf;
    }

    @Override
    public PackageMapNode createPackageMap() throws IOException {
        PackageMapNode packageMap = new PackageMapNode();
        for (Iterator i = classNameToClassFile.keySet().iterator(); i.hasNext();) {
            String className = (String) i.next();
            packageMap.add(className);
        }
        return packageMap;
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public String getLocationAsString() {
        return null;
    }

    @Override
    public int hashCode() {
        return classNameToClassFile.hashCode();
    }

    @Override
    public void bulkClassFileCreationEnd() throws IOException {

    }

    @Override
    public void bulkClassFileCreationStart() throws IOException {

    }

    @Override
    public ClassFile createClassFileBulk(String string) throws IOException {
        return createClassFileImpl(string);
    }

    @Override
    public int hashCodeImpl() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
