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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.ide.code.editor.CodeEditorTopComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * IMPORTANTE: Los valores de las lineas empiezan por 0 no por 1.
 */
public class DebugLauncher{
    
    private static DebugLauncher instance;
    
    final String classPath = "C:/Users/anton/Desktop/ProyectoPrueba/bin";
    private String className;
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

            new Thread (() -> {
        try {
            this.codeEditor = codeEditor;
            launchVM(className); // la lógica actual de tu método aquí
        } catch (Exception ex) {
        }
    }, "Debugger-Thread").start();
    }
    
        private void launchVM(String className) throws Exception{
        
        this.className = className; 

        System.out.println("Classpath usado: " + classPath);
        System.out.println("Clase principal: " + className);

        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        LaunchingConnector connector = vmm.defaultConnector();
        Map<String, Connector.Argument> args = connector.defaultArguments();

        args.get("main").setValue(className);
        args.get("options").setValue("-classpath " + classPath);

        vm = connector.launch(args);
        System.out.println("Debuggee iniciado: " + vm.name());

        EventRequestManager erm = vm.eventRequestManager();

        // Solicita explícitamente que te notifique al cargar esa clase
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
                    System.out.println("[DEBUG] VM iniciada.");
                    //events.resume();
                }

                if (event instanceof ClassPrepareEvent cp) {
                    ReferenceType refType = cp.referenceType();
                    //System.out.println("Clase cargada: " + refType.name());

                    if (refType.name().equals(className)) {
                        List<BreakpointInfo> breakpoints = BreakpointManager.getInstance().getBreakpoints();
                        
                        for(BreakpointInfo bpi : breakpoints){
                            
                            List<Location> locations = refType.locationsOfLine(bpi.getLine());
                            if (!locations.isEmpty()) {
                                BreakpointRequest bp = erm.createBreakpointRequest(locations.get(0));
                                bp.enable();
                                System.out.println("[DEBUG] Breakpoint colocado en linea " + bpi.getLine());
                                breakPointSet = true;
                                
                                if (bpi.getLine() > maxLine) {
                                    maxLine = bpi.getLine();
                                    // System.out.println("MaxLine: " + maxLine);
                                }
                            }
                        }
                    }
                }

                if (event instanceof BreakpointEvent bp) {
                    shouldResume = false;
                    Location loc = bp.location();
                    System.out.println("[DEBUG] Breakpoint alcanzado en: " +
                    loc.sourceName() + " : " + loc.lineNumber());
                    codeEditor.setLineTrackIcon(loc.lineNumber()-1);
                    ThreadReference thread = bp.thread();
                    currentThread = thread;
                    //StackFrame stack = thread.frame(0);
                    /*for (LocalVariable variable : stack.visibleVariables()) {
                    Value value = stack.getValue(variable);
                    System.out.println("-- Variable: " + variable.name() + " con valor: " + value);
                    }*/
                    
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
                    System.out.println(sourceName);
                    currentThread = ((LocatableEvent)stepEvent).thread();
                    
                    if (callStackDialog == null) {
                        setCallStackDialog(currentThread);
                        SwingUtilities.invokeLater(() -> {
                                setVariablesDialog(currentThread, callStackDialog);
                        });
                        
                    } else {
                        callStackDialog.setThread(currentThread);
                        variablesDialog.setThread(currentThread);
                    }
                    
                    // Actualiza RSyntaxTextArea con la nueva línea (opcional)
                    // Refresca las variables locales en el panel (si lo tienes)
                    // Muestra flecha en la línea actual

                    //vm.suspend(); // Volver a pausar la ejecución
                    //events.resume();
                }
                
                if (event instanceof VMDeathEvent) {
                    System.out.println("[DEBUG] VM finalizada.");
                    
                    if(callStackDialog.isShowing()){
                        callStackDialog.dispose();
                    }
                    if(variablesDialog.isShowing()){
                        variablesDialog.dispose();
                    }
                    
                    return;
                }

                if (event instanceof VMDisconnectEvent) {
                    System.out.println("[DEBUG] VM desconectada.");
                    
                    if(callStackDialog.isShowing()){
                        callStackDialog.dispose();
                        callStackDialog = null;
                    }
                    if(variablesDialog.isShowing()){
                        variablesDialog.dispose();
                        variablesDialog = null;
                    }
                    
                    return;
                }
            }
            
            if(shouldResume){
                events.resume();
            }
        }
        
    }
    
    public void stepInto() {
        if(!checkVM()){ 
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
        stepRequest.addCountFilter(1); // Solo un paso
        stepRequest.enable();

        vm.resume(); // Continuar ejecución, se parará en el próximo evento de Step
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stepOver() {
        if(!checkVM()){ 
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
        if(!checkVM()){ 
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
        if(checkVM()){
            vm.resume();
        }
    }
    
    public void stop() {
        if(checkVM()){
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
        if(vm == null){ 
            System.out.println("[DEBUG] No esta inicializado el modo debug.");
            return false;
            
        } else {
           return true; 
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
                System.out.println("[DEBUG] El diálogo de stack no está visible.");
                return;
            }

            Point stackDialogLocation = callStackDialog.getLocationOnScreen();
            int x = stackDialogLocation.x;
            int y = stackDialogLocation.y + callStackDialog.getHeight() + 10; // Justo debajo + 10 píxeles

            variablesDialog = new VariablesDialog(WindowManager.getDefault().getMainWindow(), currentThread);
            variablesDialog.setLocation(x, y);
            variablesDialog.setVisible(true);
        });
    }

    

}
