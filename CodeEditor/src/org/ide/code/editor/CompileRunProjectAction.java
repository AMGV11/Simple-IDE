package org.ide.code.editor;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.windows.WindowManager;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import static javax.swing.Action.SMALL_ICON;
import org.ide.output.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;

/**
 * Acción encargaga de la ejecución de los proyectos Java. También manda a
 * compilar los archivos si fuera necesario.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
@ActionID(
        category = "Build",
        id = "org.ide.editor.actions.CompileRunProjectAction"
)
@ActionRegistration(
        displayName = "Compilar y Ejecutar",
        lazy = false
)
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 10),
    @ActionReference(path = "Toolbars/File", position = 500),})
public final class CompileRunProjectAction extends AbstractAction implements PropertyChangeListener {

    private static CompileRunProjectAction instance;
    private Process currentProcess;
    private PrintWriter processInput;

    public CompileRunProjectAction() {
        super("Compilar y Ejecutar");
        instance = this;

        putValue(SMALL_ICON, ImageUtilities.loadImageIcon("org/ide/code/editor/ExecuteIcon.png", false));

        // Inicialmente el botón esta deshabilitado
        setEnabled(false);

        // Escuchar cambios en el registro de TopComponents
        TopComponent.getRegistry().addPropertyChangeListener(this);

        // Verificar estado inicial
        updateEnabledState();
    }

    // Método estático para obtener la instancia actual
    public static CompileRunProjectAction getInstance() {
        return instance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            runProject(IncrementalCompiler.getInstance().compile());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Cuando cambia el TopComponent activamos el botón
        if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            updateEnabledState();
        }
    }

    private void updateEnabledState() {
        TopComponent activeTC = TopComponent.getRegistry().getActivated();

        // Habilitar solo si el componente activo es un CodeEditorTopComponent
        boolean shouldEnable = (activeTC instanceof CodeEditorTopComponent);

        if (shouldEnable && activeTC instanceof CodeEditorTopComponent) {
            CodeEditorTopComponent editor = (CodeEditorTopComponent) activeTC;
            FileObject currentFile = editor.getCurrentFO();
            if (currentFile != null) {
                shouldEnable = "java".equals(currentFile.getExt());
            }
        }

        setEnabled(shouldEnable);
    }

    private void runProject(File projectRoot) throws IOException {
        if (projectRoot == null) {
            return;
        }

        File binDir = new File(projectRoot, "bin");
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

        // Terminar proceso anterior si existe
        stopCurrentProcess();

        currentProcess = pb.start();
        processInput = new PrintWriter(new OutputStreamWriter(currentProcess.getOutputStream()), true);

        // Habilitar el botón Stop
        StopProcessAction.setButtonEnabled(true);

        
        ConsolaTopComponent consola = getConsolaComponent();
        if (consola != null) {
            setupInputRedirection(consola);
        }

        new Thread(() -> {
            try (InputStreamReader reader = new InputStreamReader(currentProcess.getInputStream())) {
                char[] buffer = new char[1024];
                int charsRead;
                StringBuilder lineBuffer = new StringBuilder();
                long lastOutputTime = System.currentTimeMillis();

                while ((charsRead = reader.read(buffer, 0, buffer.length)) != -1) {
                    for (int i = 0; i < charsRead; i++) {
                        char ch = buffer[i];

                        if (ch == '\n') {
                            String line = lineBuffer.toString();
                            if (!line.trim().isEmpty()) {
                                System.out.println(line);
                            }
                            lineBuffer.setLength(0);
                            lastOutputTime = System.currentTimeMillis();
                        } else if (ch == '\r') {
                            continue;
                        } else {
                            lineBuffer.append(ch);
                            lastOutputTime = System.currentTimeMillis();

                            // Detectar prompts comunes (terminan en : o ?)
                            if ((ch == ':' || ch == '?') && lineBuffer.length() < 50) {
                                System.out.print(lineBuffer.toString());
                                System.out.flush();
                                lineBuffer.setLength(0);
                            }
                        }
                    }

                    // Si han pasado 200ms sin más entrada y hay contenido en el buffer,
                    // probablemente sea un prompt que no termina en : o ?
                    if (lineBuffer.length() > 0) {
                        try {
                            Thread.sleep(200);

                            if (reader.ready()) {
                                continue; // Hay más datos llegando, continuar leyendo
                            }

                            // Si no hay más datos y ha pasado tiempo, es probablemente un prompt
                            if (System.currentTimeMillis() - lastOutputTime >= 200) {
                                System.out.print(lineBuffer.toString());
                                System.out.flush();
                                lineBuffer.setLength(0);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }

                // Imprimir cualquier contenido restante
                if (lineBuffer.length() > 0) {
                    System.out.println(lineBuffer.toString());
                }

            } catch (IOException ex) {
            }

            try {
                int exitCode = currentProcess.waitFor();
                System.out.println("[EXEC] Proceso terminado con código: " + exitCode);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                // Deshabilitar el botón Stop cuando el proceso termine
                StopProcessAction.setButtonEnabled(false);
            }
        }).start();
        StatusDisplayer.getDefault().setStatusText("Ejecutando " + mainClass + "...");
    }

    private ConsolaTopComponent getConsolaComponent() {
        for (TopComponent tc : TopComponent.getRegistry().getOpened()) {
            if (tc instanceof ConsolaTopComponent) {
                return (ConsolaTopComponent) tc;
            }
        }

        TopComponent tc = WindowManager.getDefault().findTopComponent("ConsolaTopComponent");
        if (tc instanceof ConsolaTopComponent) {
            if (!tc.isOpened()) {
                tc.open();
            }
            return (ConsolaTopComponent) tc;
        }

        return null;
    }

    private void setupInputRedirection(ConsolaTopComponent consola) {
        TextAreaInputStream inputStream = consola.getInputStream();

        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while (currentProcess != null && currentProcess.isAlive()) {
                    if (inputStream.available() > 0) {
                        bytesRead = inputStream.read(buffer, 0, Math.min(buffer.length, inputStream.available()));
                        if (bytesRead > 0) {
                            String input = new String(buffer, 0, bytesRead);
                            processInput.print(input);
                            processInput.flush();
                        }
                    }

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (IOException ex) {
            }
        }).start();
    }

    private File findMainClass(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }

        for (File f : files) {
            if (f.isDirectory()) {
                File result = findMainClass(f);
                if (result != null) {
                    return result;
                }
            } else if (f.getName().endsWith(".java")) {
                try {
                    String content = Files.readString(f.toPath());
                    String code = removeComments(content);
                    if (code.contains("public static void main")) {
                        return f;
                    }
                } catch (IOException e) {
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

    private String removeComments(String code) {
        code = code.replaceAll("//.*", "");
        Pattern pattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(code);
        return matcher.replaceAll("");
    }

    public void stopCurrentProcess() {
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroyForcibly();
            System.out.println("[EXEC] Proceso detenido por el usuario.");
            StatusDisplayer.getDefault().setStatusText("Proceso detenido.");
        }
    }

    public boolean isProcessRunning() {
        return currentProcess != null && currentProcess.isAlive();
    }
}
