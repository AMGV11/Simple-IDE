package org.ide.code.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.fife.rsta.ac.java.JavaLanguageSupport;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rsyntaxtextarea.templates.CodeTemplate;
import org.fife.ui.rsyntaxtextarea.templates.StaticCodeTemplate;
import org.fife.ui.rtextarea.*;
import org.ide.code.debugger.BreakpointInfo;
import org.ide.code.debugger.BreakpointManager;
import org.openide.awt.ActionID;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Top Component que muestra el editor de código y maneja sus diferentes
 * funcionalidades.
 * El componente es un RSyntaxtTextArea personalizado.
 *
 * @author Antonio Manuel Guisado Valle
 * @version 1.0
 */
@TopComponent.Description(
        preferredID = "EditorTopComponent",
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "org.ide.code.editor.CodeEditorTopComponent")
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_EditorAction",
        preferredID = "EditorTopComponent"
)
@Messages({
    "CTL_EditorAction=Editor",
    "CTL_EditorTopComponent=Editor Window",
    "HINT_EditorTopComponent=This is a Code Editor window"
})
public final class CodeEditorTopComponent extends TopComponent {

    private FileObject currentFO = null;
    private final InstanceContent content = new InstanceContent();
    private final AbstractLookup lookup = new AbstractLookup(content);
    private boolean modifiedState = false;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pendingTask;
    private Gutter gutter;
    private Map<Integer, GutterIconInfo> breakpointIcons = new HashMap<>();
    private GutterIconInfo currentExecutionIcon = null;
    private int currentExecutionLine = -1;

    public CodeEditorTopComponent() throws IOException {
        initComponents();
        setName(Bundle.CTL_EditorTopComponent());
        setToolTipText(Bundle.HINT_EditorTopComponent());

        rSyntaxTextArea1.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        rSyntaxTextArea1.setCodeFoldingEnabled(true);
        rSyntaxTextArea1.setAntiAliasingEnabled(true);

        RSyntaxTextArea.setTemplatesEnabled(true);

        CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();

        CodeTemplate ct = new StaticCodeTemplate("sout", "System.out.println(", null);
        ctm.addTemplate(ct);

        ct = new StaticCodeTemplate("fb", "for (int i=0; i<", "; i++) {\n\t\n}\n");
        ctm.addTemplate(ct);

        JavaLanguageSupport javaLanguageSupport = new JavaLanguageSupport();
        javaLanguageSupport.getJarManager().addClassFileSource(new JDK9ClasspathLibraryInfo());

        javaLanguageSupport.install(rSyntaxTextArea1);

        RTextScrollPane sp = new RTextScrollPane(rSyntaxTextArea1);
        setLayout(new BorderLayout());
        add(sp, BorderLayout.CENTER);

    }

    public CodeEditorTopComponent(FileObject fileObject) throws IOException {
        associateLookup(lookup);
        initComponents();
        setName(Bundle.CTL_EditorTopComponent());
        setToolTipText(Bundle.HINT_EditorTopComponent());

        rSyntaxTextArea1.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        rSyntaxTextArea1.setCodeFoldingEnabled(true);
        rSyntaxTextArea1.setAntiAliasingEnabled(true);

        CodeEditorThemeIntegration.setupTextArea(rSyntaxTextArea1);

        RTextScrollPane sp = new RTextScrollPane(rSyntaxTextArea1, true);
        sp.setLineNumbersEnabled(true);
        sp.setLineNumbersEnabled(true);
        sp.setIconRowHeaderEnabled(false);

        JavaLanguageSupport javaLanguageSupport = null;
        if ("java".equals(fileObject.getExt())) {
            javaLanguageSupport = new JavaLanguageSupport();
            javaLanguageSupport.getJarManager().addClassFileSource(new JDK9ClasspathLibraryInfo());
            javaLanguageSupport.install(rSyntaxTextArea1);
        }

        Image imagen = ImageUtilities.loadImage("org/ide/code/debugger/redButton.png");
        ImageIcon breakpointIcon = new ImageIcon(imagen);

        gutter = sp.getGutter();
        rSyntaxTextArea1.setHighlightCurrentLine(false);

        for (Component comp : gutter.getComponents()) {
            String className = comp.getClass().getName();
            if (className.endsWith("FoldIndicator")) {
                gutter.remove(comp);
                gutter.revalidate();
                gutter.repaint();
                break;
            }
        }

        final JavaLanguageSupport finalJavaSupport = javaLanguageSupport;

        for (Component c : gutter.getComponents()) {
            if (c.getClass().getName().equals("org.fife.ui.rtextarea.LineNumberList")) {
                c.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                            if (finalJavaSupport != null) {
                                finalJavaSupport.uninstall(rSyntaxTextArea1);

                                sp.setIconRowHeaderEnabled(true);
                                int y = e.getY();

                                try {
                                    int pos = rSyntaxTextArea1.viewToModel2D(new Point(0, y));
                                    int line = rSyntaxTextArea1.getLineOfOffset(pos) + 1;
                                    BreakpointInfo bp = new BreakpointInfo(currentFO.getName(), line);
                                    BreakpointManager bpManager = BreakpointManager.getInstance();

                                    if (bpManager.contains(bp)) {
                                        bpManager.removeBreakpoint(bp);
                                        GutterIconInfo icon = breakpointIcons.remove(line - 1);
                                        gutter.removeTrackingIcon(icon);

                                    } else {
                                        GutterIconInfo info = gutter.addLineTrackingIcon(line - 1, breakpointIcon);
                                        breakpointIcons.put(line - 1, info);
                                        bpManager.addBreakpoint(bp);
                                        List<BreakpointInfo> breakpoints = BreakpointManager.getInstance().getBreakpoints();
                                    }

                                } catch (BadLocationException ex) {
                                }
                            }

                        } else if (SwingUtilities.isRightMouseButton(e) && sp.isIconRowHeaderEnabled()) {
                            sp.setIconRowHeaderEnabled(false);
                            if (finalJavaSupport != null) {
                                finalJavaSupport.install(rSyntaxTextArea1);
                            }
                        }
                    }
                });
            }
        }

        setLayout(new BorderLayout());
        add(sp, BorderLayout.CENTER);

        loadFile(fileObject);

        Parser parser = null;
        if ("java".equals(fileObject.getExt())) {
            parser = new JavaLiveCompilerParser(fileObject, getFolderBin(fileObject));
            rSyntaxTextArea1.addParser(parser);
        }

        final Parser finalParser = parser;

        rSyntaxTextArea1.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onTextChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onTextChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onTextChanged();
                if (finalParser != null) {
                    rSyntaxTextArea1.forceReparsing(finalParser);
                }
            }

            private void onTextChanged() {
                setModifiedState();

                if (finalParser != null) {
                    if (pendingTask != null && !pendingTask.isDone()) {
                        pendingTask.cancel(false);
                    }

                    pendingTask = scheduler.schedule(() -> {
                        SwingUtilities.invokeLater(() -> {
                            rSyntaxTextArea1.forceReparsing(finalParser);
                        });
                    }, 2, TimeUnit.SECONDS);
                }
            }
        });

        UIManager.addPropertyChangeListener(evt -> {
            if ("lookAndFeel".equals(evt.getPropertyName())) {
                actualizarTemaRSyntax();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        rSyntaxTextArea1 = new org.fife.ui.rsyntaxtextarea.RSyntaxTextArea();

        rSyntaxTextArea1.setColumns(20);
        rSyntaxTextArea1.setRows(5);
        jScrollPane1.setViewportView(rSyntaxTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private org.fife.ui.rsyntaxtextarea.RSyntaxTextArea rSyntaxTextArea1;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
        // Limpiar cuando se cierre el editor
        CodeEditorThemeIntegration.cleanup(rSyntaxTextArea1);
        super.componentClosed();
    }

    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    private static JMenuBar createMenuBar(RSyntaxTextArea textArea) {
        JMenuBar menuBar = new JMenuBar();
        JMenu editMenu = new JMenu("Edit");

        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.UNDO_ACTION)));
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.REDO_ACTION)));
        editMenu.addSeparator();
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.CUT_ACTION)));
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.COPY_ACTION)));
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.PASTE_ACTION)));
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.DELETE_ACTION)));
        editMenu.addSeparator();
        editMenu.add(createMenuItem(RTextArea.getAction(RTextArea.SELECT_ALL_ACTION)));
        menuBar.add(editMenu);

        return menuBar;
    }

    private static JMenuItem createMenuItem(Action action) {
        JMenuItem item = new JMenuItem(action);
        item.setToolTipText(null);
        return item;
    }

    public void loadFile(FileObject fileObject) throws IOException {
        try (InputStream in = fileObject.getInputStream(); Reader reader = new InputStreamReader(in, "UTF-8")) {

            rSyntaxTextArea1.read(reader, null);
            setDisplayName(fileObject.getNameExt());
            currentFO = fileObject;
            rSyntaxTextArea1.getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void insertUpdate(DocumentEvent e) {
                    setModifiedState();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    setModifiedState();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    setModifiedState();
                }

            });
        }
    }

    public void saveFile() throws IOException {
        try (OutputStream out = currentFO.getOutputStream(); Writer writer = new OutputStreamWriter(out, "UTF-8")) {
            rSyntaxTextArea1.write(writer);
        }
    }

    public FileObject getCurrentFO() {
        return currentFO;
    }

    private void setModifiedState() {
        if (!modifiedState) {
            modifiedState = true;
            updateTitle(true);
            content.add(new SaveCookie() {
                @Override
                public void save() {
                    content.remove(this);
                }
            });
        }
    }

    private void updateTitle(boolean modificado) {
        if (currentFO != null) {
            String name = currentFO.getNameExt();
            if (modificado) {
                setHtmlDisplayName("<html><b>*" + name + "</b></html>");
            } else {
                setHtmlDisplayName("<html>" + name + "</html>");
            }
        }
    }

    public void setState(boolean state) {
        modifiedState = state;
        updateTitle(modifiedState);
    }

    private FileObject getProjectDirectory(FileObject fo) {
        FileObject current = fo;

        while (current != null && !current.getName().equals("src")) {
            current = current.getParent();
        }
        if (current != null) {
            return current.getParent();
        }

        return null;
    }

    public static File getFolderBin(FileObject javaFo) {
        FileObject current = javaFo;
        while (current != null && !current.getName().equals("src")) {
            current = current.getParent();
        }
        if (current == null) {
            return null;
        }
        File projectRoot = new File(current.getParent().getPath());
        File binDir = new File(projectRoot, "bin");
        return binDir.exists() ? binDir : null;
    }

    public void setLineTrackIcon(int newLineNumber) throws BadLocationException {
        Image executionImage = ImageUtilities.loadImage("org/ide/code/debugger/greenArrow.png");
        ImageIcon executionIcon = new ImageIcon(executionImage);

        Image bpImage = ImageUtilities.loadImage("org/ide/code/debugger/redButton.png");
        ImageIcon breakpointIcon = new ImageIcon(bpImage);

        if (currentExecutionIcon != null) {
            gutter.removeTrackingIcon(currentExecutionIcon);
            currentExecutionIcon = null; // Limpiar la referencia
        }

        if (breakpointIcons.containsKey(newLineNumber)) {
            GutterIconInfo removedBreakpoint = breakpointIcons.remove(newLineNumber);
            gutter.removeTrackingIcon(removedBreakpoint);
            breakpointIcons.put(newLineNumber, null);
        }

        try {
            currentExecutionIcon = gutter.addLineTrackingIcon(newLineNumber, executionIcon);
            currentExecutionLine = newLineNumber;
        } catch (BadLocationException e) {
        }

        if (breakpointIcons.containsKey(currentExecutionLine)) {
            GutterIconInfo restoredBreakpoint = gutter.addLineTrackingIcon(currentExecutionLine, breakpointIcon);
            breakpointIcons.put(currentExecutionLine, restoredBreakpoint);
        }
    }

    private void actualizarTemaRSyntax() {
        String lafName = UIManager.getLookAndFeel().getName().toLowerCase();

        String themePath;
        if (lafName.contains("dark") || lafName.contains("darcula") || lafName.contains("flatlaf dark")) {
            themePath = "/org/fife/ui/rsyntaxtextarea/themes/dark.xml";
        } else {
            themePath = "/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml";
        }

        try (InputStream in = getClass().getResourceAsStream(themePath)) {
            Theme theme = Theme.load(in);
            theme.apply(rSyntaxTextArea1);
            rSyntaxTextArea1.repaint();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
