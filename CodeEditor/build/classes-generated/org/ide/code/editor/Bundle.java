package org.ide.code.editor;
/** Localizable strings for {@link org.ide.code.editor}. */
class Bundle {
    /**
     * @return <i>Editor</i>
     * @see EditorTopComponent
     */
    static String CTL_EditorAction() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CTL_EditorAction");
    }
    /**
     * @return <i>Editor de Codigo</i>
     * @see EditorTopComponent
     */
    static String CTL_EditorTopComponent() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CTL_EditorTopComponent");
    }
    /**
     * @return <i>This is a Editor window</i>
     * @see EditorTopComponent
     */
    static String HINT_EditorTopComponent() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "HINT_EditorTopComponent");
    }
    private Bundle() {}
}
