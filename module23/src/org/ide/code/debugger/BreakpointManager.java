/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.code.debugger;

import java.util.*;

public class BreakpointManager {

    private static BreakpointManager instance;

    // Mapa de breakpoints por clase
    private final List<BreakpointInfo> breakpoints = new ArrayList<>();

    private BreakpointManager() {}

    public static synchronized BreakpointManager getInstance() {
        if (instance == null) {
            instance = new BreakpointManager();
        }
        return instance;
    }

    public void addBreakpoint(BreakpointInfo bp) {
        if (!breakpoints.contains(bp)) {
            breakpoints.add(bp);
            System.out.println("Breakpoint registrado: " + bp);
        }
    }
    
    public boolean contains (BreakpointInfo bp){
        return breakpoints.contains(bp);
    }
    
    public List<BreakpointInfo> getBreakpoints() {
        return new ArrayList<>(breakpoints);
    }

    public void removeBreakpoint(BreakpointInfo bp) {
        breakpoints.remove(bp);
        System.out.println("Breakpoint eliminado: " + bp);
    }

    public void cleanBreakPoints() {
        breakpoints.clear();
    }
}

//Ejemplo de uso: BreakpointManager.getInstance().agregarBreakpoint("com.ejemplo.Main", 12);
//Set<Integer> bps = BreakpointManager.getInstance().obtenerBreakpoints("com.ejemplo.Main");
