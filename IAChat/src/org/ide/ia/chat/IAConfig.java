package org.ide.ia.chat;

/**
 * Configuración hardcodeada del componente para Chat con Qwenn
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
public class IAConfig {

    // Cambiar entre local y remoto
    private static final boolean USE_REMOTE = true; // Cambiar a true para usar remoto

    // URLs
    private static final String LOCAL_URL = "http://localhost:11434/api/generate";
    private static final String REMOTE_URL = "https://chat.ide-ia-chat.lol/api/generate";

    // Token de seguridad (hardcodeado)
    private static final String SECRET_TOKEN = "MI_TOKEN_SECRETO_123";

    public static String getOllamaUrl() {
        return USE_REMOTE ? REMOTE_URL : LOCAL_URL;
    }

    public static boolean isRemote() {
        return USE_REMOTE;
    }

    public static String getToken() {
        return SECRET_TOKEN;
    }
}
