package org.ide.code.debugger;

import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

/**
 * Muestra y manega la tabla de variables, mostrada al usar la opción debug en
 * el IDE. Muestra los valores en cada momento de las diferentes variables
 * declaradas en el código.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class VariablesDialog extends JDialog {

    private JTable variablesTable;

    public VariablesDialog(Window owner, ThreadReference thread) {
        super(owner, "Variables locales", ModalityType.MODELESS);

        setLayout(new BorderLayout());

        // Crear tabla y modelo
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Variable", "Valor"}, 0);
        variablesTable = new JTable(model);
        variablesTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        variablesTable.setFillsViewportHeight(true);

        JScrollPane scroll = new JScrollPane(variablesTable);
        add(scroll, BorderLayout.CENTER);

        setSize(400, 300);
        setLocationRelativeTo(owner);

        loadVariables(thread);  // Llenar la tabla con los datos actuales
    }

    private void loadVariables(ThreadReference thread) {
        DefaultTableModel model = (DefaultTableModel) variablesTable.getModel();
        model.setRowCount(0); // Limpiar modelo actual

        try {
            if (thread != null && thread.frameCount() > 0) {
                StackFrame frame = thread.frame(0);
                for (LocalVariable variable : frame.visibleVariables()) {
                    Value value = frame.getValue(variable);
                    if (!variable.name().equals("args")) {
                        model.addRow(new Object[]{variable.name(), value});
                    }
                }
            }
        } catch (Exception e) {
            model.addRow(new Object[]{"<Error>", e.getMessage()});
            e.printStackTrace();
        }
    }

    public void setThread(ThreadReference thread) {
        loadVariables(thread);  // Refrescar tabla
    }
}
