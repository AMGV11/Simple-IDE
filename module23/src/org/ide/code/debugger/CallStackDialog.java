/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.ide.code.debugger;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CallStackDialog extends JDialog {

    private JList<String> stackList;
    private ThreadReference thread;
    private DefaultListModel<String> listModel;

    public CallStackDialog(Window owner, ThreadReference thread) {
        super(owner, "Pila de llamadas", ModalityType.MODELESS);
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        stackList = new JList<>(listModel);
        stackList.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scroll = new JScrollPane(stackList);
        add(scroll, BorderLayout.CENTER);

        setSize(400, 300);
        setLocationRelativeTo(owner);

        loadStackFrames(thread);
    }

    private void loadStackFrames(ThreadReference thread) {
        try {
            List<StackFrame> frames = thread.frames();
            for (int i = 0; i < frames.size(); i++) {
                StackFrame frame = frames.get(i);
                String methodName = frame.location().method().name();
                String className = frame.location().declaringType().name();
                int line = frame.location().lineNumber();
                listModel.addElement("#" + i + " " + className + "." + methodName + " (línea " + line + ")");
            }
        } catch (Exception e) {
            listModel.addElement("Error al obtener pila: " + e.getMessage());
        }
    }
    
        public void setThread(ThreadReference thread) {
        this.thread = thread;
        updateCallStack();
    }

    private void updateCallStack() {
        DefaultListModel<String> model = new DefaultListModel<>();
        try {
            List<StackFrame> frames = thread.frames();
            for (int i = 0; i < frames.size(); i++) {
                StackFrame frame = frames.get(i);
                Location loc = frame.location();
                String className = loc.declaringType().name();
                String methodName = loc.method().name();
                int line = loc.lineNumber();

                model.addElement("#" + i + " " + className + "." + methodName + " (línea " + line + ")");
            }
        } catch (IncompatibleThreadStateException e) {
            model.addElement("Error al obtener la pila: " + e.getMessage());
        }

        stackList.setModel(model);
    }
    
}


