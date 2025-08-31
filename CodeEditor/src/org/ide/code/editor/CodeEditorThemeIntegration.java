package org.ide.code.editor;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.openide.modules.ModuleInstall;

/**
 * Clase utilizada para integrar el sistema de temas dinámico en RSyntaxTextArea
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class CodeEditorThemeIntegration {

    private static boolean initialized = false;

    public static void initialize() {
        if (!initialized) {
            RSyntaxThemeManager.getInstance();
            LookAndFeelChangeListener.getInstance();
            initialized = true;
        }
    }

    public static void setupTextArea(RSyntaxTextArea textArea) {
        initialize();
        LookAndFeelChangeListener.getInstance().registerTextArea(textArea);
    }

    public static void applyAutoTheme(RSyntaxTextArea textArea) {
        initialize();
        RSyntaxThemeManager.getInstance().applyAutoTheme(textArea);
    }

    public static void cleanup(RSyntaxTextArea textArea) {
        if (initialized) {
            LookAndFeelChangeListener.getInstance().unregisterTextArea(textArea);
        }
    }

    public static void forceUpdateAll() {
        if (initialized) {
            LookAndFeelChangeListener.getInstance().forceUpdateAll();
        }
    }
}

class EditorThemeModuleInstaller extends ModuleInstall {

    @Override
    public void restored() {
        CodeEditorThemeIntegration.initialize();
    }

    @Override
    public void uninstalled() {
        LookAndFeelChangeListener.getInstance().cleanup();
    }
}
