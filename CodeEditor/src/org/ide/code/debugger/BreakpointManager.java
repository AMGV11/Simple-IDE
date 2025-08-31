package org.ide.code.debugger;

import java.util.*;

/**
 * Maneja y guarda los Breakpoints para su correcto uso.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class BreakpointManager {

    private static BreakpointManager instance;

    // Mapa de breakpoints por clase
    private final List<BreakpointInfo> breakpoints = new ArrayList<>();

    private BreakpointManager() {
    }

    public static synchronized BreakpointManager getInstance() {
        if (instance == null) {
            instance = new BreakpointManager();
        }
        return instance;
    }

    public void addBreakpoint(BreakpointInfo bp) {
        if (!breakpoints.contains(bp)) {
            breakpoints.add(bp);
            System.out.println("[DEBUG] Breakpoint registrado: " + bp);
        }
    }

    public boolean contains(BreakpointInfo bp) {
        return breakpoints.contains(bp);
    }

    public List<BreakpointInfo> getBreakpoints() {
        return new ArrayList<>(breakpoints);
    }

    public void removeBreakpoint(BreakpointInfo bp) {
        breakpoints.remove(bp);
        System.out.println("[DEBUG] Breakpoint eliminado: " + bp);
    }

    public void cleanBreakPoints() {
        breakpoints.clear();
    }
}
