package org.ide.ia.chat;
/** Localizable strings for {@link org.ide.ia.chat}. */
class Bundle {
    /**
     * @return <i>ChatWindow</i>
     * @see ChatWindowTopComponent
     */
    static String CTL_ChatWindowAction() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CTL_ChatWindowAction");
    }
    /**
     * @return <i>Chat IA</i>
     * @see ChatWindowTopComponent
     */
    static String CTL_ChatWindowTopComponent() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CTL_ChatWindowTopComponent");
    }
    /**
     * @return <i>This is a ChatWindow window</i>
     * @see ChatWindowTopComponent
     */
    static String HINT_ChatWindowTopComponent() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "HINT_ChatWindowTopComponent");
    }
    private Bundle() {}
}
