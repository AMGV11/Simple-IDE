package org.ide.code.editor;

import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.UIManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

/**
 * Listener que escucha los cambios de tema en el IDE para activar el cambio de
 * tema en el editor de código.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class LookAndFeelChangeListener {

    private static LookAndFeelChangeListener instance;
    private List<WeakReference<RSyntaxTextArea>> registeredTextAreas;
    private PropertyChangeListener lafListener;

    private LookAndFeelChangeListener() {
        registeredTextAreas = new ArrayList<>();
        setupLookAndFeelListener();
    }

    public static LookAndFeelChangeListener getInstance() {
        if (instance == null) {
            instance = new LookAndFeelChangeListener();
        }
        return instance;
    }

    private void setupLookAndFeelListener() {
        lafListener = evt -> {
            if ("lookAndFeel".equals(evt.getPropertyName())) {
                updateAllTextAreas();
            }
        };

        UIManager.addPropertyChangeListener(lafListener);
    }

    public void registerTextArea(RSyntaxTextArea textArea) {
        cleanupWeakReferences();
        registeredTextAreas.add(new WeakReference<>(textArea));

        // Aplicar tema inmediatamente
        RSyntaxThemeManager.getInstance().applyAutoTheme(textArea);
    }

    public void unregisterTextArea(RSyntaxTextArea textArea) {
        registeredTextAreas.removeIf(ref -> {
            RSyntaxTextArea area = ref.get();
            return area == null || area == textArea;
        });
    }

    private void updateAllTextAreas() {
        cleanupWeakReferences();

        RSyntaxThemeManager themeManager = RSyntaxThemeManager.getInstance();
        int updatedCount = 0;

        for (WeakReference<RSyntaxTextArea> ref : registeredTextAreas) {
            RSyntaxTextArea textArea = ref.get();
            if (textArea != null) {
                themeManager.applyAutoTheme(textArea);
                updatedCount++;
            }
        }
    }

    private void cleanupWeakReferences() {
        Iterator<WeakReference<RSyntaxTextArea>> iterator = registeredTextAreas.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().get() == null) {
                iterator.remove();
            }
        }
    }

    public void forceUpdateAll() {
        updateAllTextAreas();
    }

    public void cleanup() {
        if (lafListener != null) {
            UIManager.removePropertyChangeListener(lafListener);
        }
        registeredTextAreas.clear();
    }
}
