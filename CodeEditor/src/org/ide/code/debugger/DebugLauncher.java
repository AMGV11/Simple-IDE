package org.ide.code.debugger;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.ide.code.editor.CodeEditorTopComponent;
import org.ide.output.ConsolaTopComponent;
import org.ide.output.TextAreaInputStream;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Clase encargada de lanzar y manejar el modo debug y sus diferentes opciones.
 * IMPORTANTE: Los valores de las lineas empiezan por 0 no por 1.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.2
 */
public class DebugLauncher {

    private static DebugLauncher instance;

    String classPath = "C:/Users/anton/Desktop/ProyectoPrueba/bin";
    private String className;
    private static InputStream originalSystemIn;
    private static PrintStream originalSystemOut;
    private static PrintStream originalSystemErr;
    private ConsolaTopComponent consola;
    private TextAreaInputStream debugInputStream;
    private static ThreadReference currentThread;
    private static VirtualMachine vm;
    private volatile CallStackDialog callStackDialog;
    private volatile VariablesDialog variablesDialog;
    private volatile CodeEditorTopComponent codeEditor;

    public static DebugLauncher getInstance() {
        if (instance == null) {
            instance = new DebugLauncher();
        }
        return instance;
    }

    public void launchAndDebug(String className, CodeEditorTopComponent codeEditor) throws IOException, IllegalConnectorArgumentsException, VMStartException, InterruptedException, AbsentInformationException, IncompatibleThreadStateException {

        new Thread(() -> {
            try {
                this.codeEditor = codeEditor;
                launchVM(className);
            } catch (Exception ex) {
            }
        }, "Debugger-Thread").start();
    }

    private static String convertRouteVM(String classPath) {
        String newRoute = classPath.replace("\\", "/");

        if (newRoute.contains(" ")) {
            newRoute = "\"" + newRoute + "\"";
        }

        return newRoute;
    }

    private void launchVM(String className) throws Exception {
        if (originalSystemIn == null) {
            originalSystemIn = System.in;
            originalSystemOut = System.out;
            originalSystemErr = System.err;
        }
        this.className = className;
        System.out.println("[DEBUG] Classpath usado: " + classPath);

        classPath = convertRouteVM(CodeEditorTopComponent.getFolderBin(codeEditor.getCurrentFO()).getPath());

        // Obtener la instancia de la consola existente
        SwingUtilities.invokeAndWait(() -> {
            consola = ConsolaTopComponent.getInstance();
            if (consola != null) {
                debugInputStream = consola.getInputStream();
                System.out.println("[DEBUG] === MODO DEBUG INICIADO ===");
            } else {
                System.err.println("[ERROR] No se pudo obtener la instancia de la consola");
            }
        });

        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        LaunchingConnector connector = vmm.defaultConnector();
        Map<String, Connector.Argument> args = connector.defaultArguments();

        args.get("main").setValue(className);
        args.get("options").setValue("-classpath " + classPath);

        vm = connector.launch(args);

        setupIORedirection();

        EventRequestManager erm = vm.eventRequestManager();

        ClassPrepareRequest prepReq = erm.createClassPrepareRequest();
        prepReq.addClassFilter(className);
        prepReq.enable();

        EventQueue queue = vm.eventQueue();
        boolean breakPointSet = false;
        int maxLine = 0;

        while (true) {
            EventSet events = queue.remove();
            boolean shouldResume = true;

            for (Event event : events) {
                if (event instanceof VMStartEvent) {
                }

                if (event instanceof ClassPrepareEvent cp) {
                    ReferenceType refType = cp.referenceType();

                    if (refType.name().equals(className)) {
                        List<BreakpointInfo> breakpoints = BreakpointManager.getInstance().getBreakpoints();

                        for (BreakpointInfo bpi : breakpoints) {

                            List<Location> locations = refType.locationsOfLine(bpi.getLine());
                            if (!locations.isEmpty()) {
                                BreakpointRequest bp = erm.createBreakpointRequest(locations.get(0));
                                bp.enable();
                                System.out.println("[DEBUG] Breakpoint colocado en linea " + bpi.getLine());
                                breakPointSet = true;

                                if (bpi.getLine() > maxLine) {
                                    maxLine = bpi.getLine();
                                }
                            }
                        }
                    }
                }

                if (event instanceof BreakpointEvent bp) {
                    shouldResume = false;
                    Location loc = bp.location();
                    System.out.println("[DEBUG] 🔴 Breakpoint alcanzado en: "
                            + loc.sourceName() + " : " + loc.lineNumber());
                    codeEditor.setLineTrackIcon(loc.lineNumber() - 1);
                    ThreadReference thread = bp.thread();
                    currentThread = thread;

                    SwingUtilities.invokeLater(() -> {
                        if (callStackDialog == null) {
                            setCallStackDialog(currentThread);
                            SwingUtilities.invokeLater(() -> {
                                setVariablesDialog(currentThread, callStackDialog);
                            });
                        } else {
                            callStackDialog.setThread(currentThread);
                            variablesDialog.setThread(currentThread);
                        }
                    });
                }

                if (event instanceof StepEvent) {
                    shouldResume = false;
                    StepEvent stepEvent = (StepEvent) event;
                    int line = stepEvent.location().lineNumber();
                    System.out.println("[DEBUG] Step ejecutado en línea: " + stepEvent.location().lineNumber());
                    codeEditor.setLineTrackIcon(line - 1);

                    String sourceName = stepEvent.location().sourceName();
                    currentThread = ((LocatableEvent) stepEvent).thread();

                    if (callStackDialog == null) {
                        setCallStackDialog(currentThread);
                        SwingUtilities.invokeLater(() -> {
                            setVariablesDialog(currentThread, callStackDialog);
                        });

                    } else {
                        callStackDialog.setThread(currentThread);
                        variablesDialog.setThread(currentThread);
                    }

                }

                if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
                    System.out.println("\n[DEBUG] === MODO DEBUG FINALIZADO ===");

                    restoreOriginalStreams();
                    cleanup();

                    SwingUtilities.invokeLater(() -> {
                        // Limpiar referencias
                        consola = null;
                        debugInputStream = null;

                        if (callStackDialog != null && callStackDialog.isShowing()) {
                            callStackDialog.dispose();
                            callStackDialog = null;
                        }
                        if (variablesDialog != null && variablesDialog.isShowing()) {
                            variablesDialog.dispose();
                            variablesDialog = null;
                        }
                    });

                    return;
                }
            }

            if (shouldResume) {
                events.resume();
            }
        }

    }

    private static void restoreOriginalStreams() {
        try {
            if (originalSystemIn != null) {
                System.setIn(originalSystemIn);
            }

            if (originalSystemOut != null) {
                System.setOut(originalSystemOut);
            }

            if (originalSystemErr != null) {
                System.setErr(originalSystemErr);
            }

            SwingUtilities.invokeLater(() -> {
                ConsolaTopComponent consolaInstance = ConsolaTopComponent.getInstance();
                if (consolaInstance != null) {
                    consolaInstance.reconfigureForNormalMode();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stepInto() {
        if (!checkVM()) {
            return;
        }

        if (currentThread == null || !currentThread.isSuspended()) {
            System.out.println("[DEBUG] No hay hilo válido para Step Into.");
            return;
        }

        try {
            clearPreviousSteps();

            StepRequest stepRequest = vm.eventRequestManager().createStepRequest(
                    currentThread,
                    StepRequest.STEP_LINE,
                    StepRequest.STEP_INTO
            );

            stepRequest.addClassFilter(className);
            stepRequest.addCountFilter(1);
            stepRequest.enable();

            vm.resume();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stepOver() {
        if (!checkVM()) {
            return;
        }

        if (currentThread == null || !currentThread.isSuspended()) {
            System.out.println("[DEBUG] No hay hilo válido para Step Over.");
            return;
        }

        try {
            clearPreviousSteps();

            StepRequest stepRequest = vm.eventRequestManager().createStepRequest(
                    currentThread,
                    StepRequest.STEP_LINE,
                    StepRequest.STEP_OVER
            );

            stepRequest.addClassFilter(className);
            stepRequest.addCountFilter(1);
            stepRequest.enable();

            vm.resume();
        } catch (Exception e) {
        }
    }

    public void stepOut() {
        if (!checkVM()) {
            return;
        }

        if (currentThread == null || !currentThread.isSuspended()) {
            System.out.println("[DEBUG] No hay hilo válido para Step Out.");
            return;
        }

        try {
            clearPreviousSteps();

            StepRequest stepRequest = vm.eventRequestManager().createStepRequest(
                    currentThread,
                    StepRequest.STEP_LINE,
                    StepRequest.STEP_OUT
            );

            stepRequest.addClassFilter(className);
            stepRequest.addCountFilter(1);
            stepRequest.enable();

            vm.resume();
        } catch (Exception e) {
        }
    }

    public void resume() {
        if (checkVM()) {
            vm.resume();
        }
    }

    public void stop() {
        if (checkVM()) {
            vm.exit(0);
        }
    }

    private void clearPreviousSteps() {
        List<StepRequest> toRemove = new ArrayList<>();
        for (StepRequest sr : vm.eventRequestManager().stepRequests()) {
            toRemove.add(sr);
        }
        for (StepRequest sr : toRemove) {
            vm.eventRequestManager().deleteEventRequest(sr);
        }
    }

    //Para comprobar si la VM esta activa
    private boolean checkVM() {
        if (vm == null) {
            System.out.println("[DEBUG] No esta inicializado el modo debug.");
            return false;
        }

        try {
            vm.allThreads(); // Esto lanza excepción si la VM está desconectada
            return true;

        } catch (com.sun.jdi.VMDisconnectedException e) {
            cleanup();
            return false;

        } catch (Exception e) {
            cleanup();
            return false;
        }
    }

    public void setCallStackDialog(ThreadReference currentThread) {
        SwingUtilities.invokeLater(() -> {
            TopComponent codeEditorInstance = TopComponent.getRegistry().getActivated();

            if (codeEditorInstance instanceof CodeEditorTopComponent) {

                Point editorLocation = codeEditorInstance.getLocationOnScreen();
                int x = editorLocation.x + codeEditorInstance.getWidth() + 10; // 10 píxeles a la derecha
                int y = editorLocation.y;

                callStackDialog = new CallStackDialog(WindowManager.getDefault().getMainWindow(), currentThread);
                callStackDialog.setLocation(x, y);
                callStackDialog.setVisible(true);
            }
        });
    }

    public void setVariablesDialog(ThreadReference currentThread, CallStackDialog callStackDialog) {
        SwingUtilities.invokeLater(() -> {
            if (callStackDialog == null || !callStackDialog.isShowing()) {
                System.out.println("[ERROR] El diálogo de stack no está visible.");
                return;
            }

            Point stackDialogLocation = callStackDialog.getLocationOnScreen();
            int x = stackDialogLocation.x;
            int y = stackDialogLocation.y + callStackDialog.getHeight() + 10;

            variablesDialog = new VariablesDialog(WindowManager.getDefault().getMainWindow(), currentThread);
            variablesDialog.setLocation(x, y);
            variablesDialog.setVisible(true);
        });
    }

    private void setupIORedirection() {
        if (vm.process() != null && consola != null) {
            Process process = vm.process();

            Thread outputThread = new Thread(() -> {
                try {
                    char[] buffer = new char[1024];
                    StringBuilder lineBuffer = new StringBuilder();
                    long lastOutputTime = System.currentTimeMillis();

                    try (InputStreamReader reader = new InputStreamReader(process.getInputStream())) {
                        int charsRead;
                        while ((charsRead = reader.read(buffer, 0, buffer.length)) != -1) {
                            for (int i = 0; i < charsRead; i++) {
                                char ch = buffer[i];

                                if (ch == '\n') {
                                    String line = lineBuffer.toString();
                                    if (!line.trim().isEmpty()) {
                                        final String finalLine = line;
                                        SwingUtilities.invokeLater(() -> {
                                            System.out.println(finalLine);
                                            System.out.flush();
                                        });
                                    }
                                    lineBuffer.setLength(0);
                                    lastOutputTime = System.currentTimeMillis();

                                } else if (ch == '\r') {
                                    continue;

                                } else {
                                    lineBuffer.append(ch);
                                    lastOutputTime = System.currentTimeMillis();

                                    if ((ch == ':' || ch == '?' || ch == '>' || ch == '$')
                                            && lineBuffer.length() < 80) {

                                        final String prompt = lineBuffer.toString();
                                        SwingUtilities.invokeLater(() -> {
                                            System.out.print(prompt);
                                            System.out.flush();
                                        });
                                        lineBuffer.setLength(0);
                                    }
                                }
                            }

                            if (lineBuffer.length() > 0) {
                                try {
                                    Thread.sleep(100);
                                    if (!reader.ready()) { 
                                        if (System.currentTimeMillis() - lastOutputTime >= 100) {
                                            final String prompt = lineBuffer.toString();
                                            SwingUtilities.invokeLater(() -> {
                                                System.out.print(prompt);
                                                System.out.flush();
                                            });
                                            lineBuffer.setLength(0);
                                        }
                                    }
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                            }
                        }

                        // Flush final de cualquier contenido restante
                        if (lineBuffer.length() > 0) {
                            final String remaining = lineBuffer.toString();
                            SwingUtilities.invokeLater(() -> {
                                System.out.print(remaining);
                                System.out.flush();
                            });
                        }
                    }

                } catch (IOException e) {
                    System.err.println("[ERROR] Fallo leyendo output del proceso: " + e.getMessage());
                }
            }, "Debug-Output-Reader");

            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String error = line;
                        SwingUtilities.invokeLater(() -> {
                            System.err.println("[ERROR] " + error);
                            System.err.flush();
                        });
                    }
                } catch (IOException e) {
                }
            }, "Debug-Error-Reader");

            Thread inputThread = new Thread(() -> {
                try {
                    PrintWriter processWriter = new PrintWriter(
                            new OutputStreamWriter(process.getOutputStream()), true);

                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = debugInputStream.read(buffer)) != -1) {
                        String input = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);

                        processWriter.print(input);
                        processWriter.flush();

                    }
                } catch (IOException e) {
                    System.err.println("[ERROR] Fallo en input: " + e.getMessage());
                }
            }, "Debug-Input-Redirector");

            outputThread.setDaemon(true);
            errorThread.setDaemon(true);
            inputThread.setDaemon(true);

            outputThread.start();
            errorThread.start();
            inputThread.start();

        } else {
            System.err.println("[DEBUG] Proceso o consola no disponibles");
        }
    }

    private void redirectInputFromConsole(PrintWriter processWriter) {
        if (debugInputStream == null) {
            return;
        }

        try {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = debugInputStream.read(buffer)) != -1) {
                String input = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                processWriter.print(input);
                processWriter.flush();

                SwingUtilities.invokeLater(() -> {
                    System.out.println("[INPUT] " + input.trim());
                });
            }
        } catch (IOException e) {
        }
    }

    private void cleanup() {
        vm = null;
        currentThread = null;
        consola = null;
        debugInputStream = null;

        SwingUtilities.invokeLater(() -> {
            if (callStackDialog != null && callStackDialog.isShowing()) {
                callStackDialog.dispose();
                callStackDialog = null;
            }
            if (variablesDialog != null && variablesDialog.isShowing()) {
                variablesDialog.dispose();
                variablesDialog = null;
            }
        });
    }
}
