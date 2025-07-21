/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/NetBeansModuleDevelopment-files/templateTopComponent637.java to edit this template
 */
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
import org.openide.awt.ActionReference;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Top component which displays something.
 */

@TopComponent.Description(
        preferredID = "EditorTopComponent",
       // iconBase="OpenFileIcon",
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "org.ide.code.editor.CodeEditorTopComponent") 
@ActionReference(path = "Menu/Window" /*, position = 333 */)
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
        
        rSyntaxTextArea1.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA); // Puedes cambiarlo según el tipo de archivo
        rSyntaxTextArea1.setCodeFoldingEnabled(true);
        rSyntaxTextArea1.setAntiAliasingEnabled(true);
        
        // Whether templates are enabled is a global property affecting all
        // RSyntaxTextAreas, so this method is static.
        RSyntaxTextArea.setTemplatesEnabled(true);

        // Code templates are shared among all RSyntaxTextAreas. You add and
        // remove templates through the shared CodeTemplateManager instance.
        CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();

        // StaticCodeTemplates are templates that insert static text before and
        // after the current caret position. This template is basically shorthand
        // for "System.out.println(".
        CodeTemplate ct = new StaticCodeTemplate("sout", "System.out.println(", null);
        ctm.addTemplate(ct);

        // This template is for a for-loop. The caret is placed at the upper
        // bound of the loop.
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
        
        rSyntaxTextArea1.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA); // Puedes cambiarlo según el tipo de archivo
        rSyntaxTextArea1.setCodeFoldingEnabled(true);
        rSyntaxTextArea1.setAntiAliasingEnabled(true);
        
        JavaLanguageSupport javaLanguageSupport = new JavaLanguageSupport();
        javaLanguageSupport.getJarManager().addClassFileSource(new JDK9ClasspathLibraryInfo());
        javaLanguageSupport.install(rSyntaxTextArea1);
        
        RTextScrollPane sp = new RTextScrollPane(rSyntaxTextArea1, true);
        sp.setLineNumbersEnabled(true);
        sp.setLineNumbersEnabled(true);
        sp.setIconRowHeaderEnabled(false);
        
        Image imagen =  ImageUtilities.loadImage("org/ide/code/debugger/redButton.png");
        ImageIcon breakpointIcon = new ImageIcon(imagen);
        
        //Ayuda al debug
        gutter = sp.getGutter();
        rSyntaxTextArea1.setHighlightCurrentLine(false);
        
        // Eliminar FoldIndicator si está presente
         for (Component comp : gutter.getComponents()) {
             String className = comp.getClass().getName();
             if (className.endsWith("FoldIndicator")) {
                 gutter.remove(comp); // ✅ lo quitamos
                 gutter.revalidate();
                 gutter.repaint();
                 break;
             }
         }
        
        // Inspecciona los hijos del Gutter directamente
        for (Component c : gutter.getComponents()) {
            if (c.getClass().getName().equals("org.fife.ui.rtextarea.LineNumberList")){
            c.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                        javaLanguageSupport.uninstall(rSyntaxTextArea1);
                        sp.setIconRowHeaderEnabled(true);
                        int y = e.getY();
                        
                        try {
                            // Convertimos coordenadas Y a número de línea
                            int pos = rSyntaxTextArea1.viewToModel2D(new Point(0, y));
                            int line = rSyntaxTextArea1.getLineOfOffset(pos) + 1;
                            BreakpointInfo bp = new BreakpointInfo(currentFO.getName(), line);
                            BreakpointManager bpManager = BreakpointManager.getInstance();
                            
                            if (bpManager.contains(bp)){
                                bpManager.removeBreakpoint(bp);
                                GutterIconInfo icon = breakpointIcons.remove(line-1);
                                gutter.removeTrackingIcon(icon);
                                
                            } else {
                                GutterIconInfo info = gutter.addLineTrackingIcon(line - 1, breakpointIcon);
                                breakpointIcons.put(line - 1, info);
                                bpManager.addBreakpoint(bp);
                                List<BreakpointInfo> breakpoints = BreakpointManager.getInstance().getBreakpoints();
                                System.out.println(breakpoints.get(0).getClassName());

                            }
                            
                        } catch (BadLocationException ex) {
                        }
                        
                    } else if (SwingUtilities.isRightMouseButton(e) && sp.isIconRowHeaderEnabled()){
                        sp.setIconRowHeaderEnabled(false);
                        javaLanguageSupport.install(rSyntaxTextArea1);
                    }
                }       
            });   
            }
        }
        
        setLayout(new BorderLayout());
        add(sp, BorderLayout.CENTER);
        
        loadFile(fileObject);
        
        Parser parser = new JavaLiveCompilerParser(fileObject, getFolderBin(fileObject));
        rSyntaxTextArea1.addParser(parser);
        
        rSyntaxTextArea1.getDocument().addDocumentListener(new DocumentListener(){
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
                rSyntaxTextArea1.forceReparsing(parser);
            }
            
                private void onTextChanged() {
                setModifiedState();

                // Cancela compilación anterior si todavía no ocurrió
                if (pendingTask != null && !pendingTask.isDone()) {
                    pendingTask.cancel(false);
                }

                // Programa una nueva compilación con 2 segundos de retraso
                pendingTask = scheduler.schedule(() -> {
                    // fuerza el análisis sintáctico en el hilo de Swing
                    SwingUtilities.invokeLater(() -> {
                        rSyntaxTextArea1.forceReparsing(parser);
                    });
                }, 2, TimeUnit.SECONDS);
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
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
    
    //Esto es el menu base, lo podemos editar e implementar
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
        item.setToolTipText(null); // Swing annoyingly adds tool tip text to the menu item
        return item;
    }
    
    public void loadFile(FileObject fileObject) throws IOException {
        try (InputStream in = fileObject.getInputStream();
                
            Reader reader = new InputStreamReader(in, "UTF-8")) {
            
                rSyntaxTextArea1.read(reader, null);
                setDisplayName(fileObject.getNameExt()); // Cambiar el título del editor
                currentFO = fileObject;
                rSyntaxTextArea1.getDocument().addDocumentListener(new DocumentListener(){
                    
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
        try (OutputStream out = currentFO.getOutputStream();
            Writer writer = new OutputStreamWriter(out, "UTF-8")) {
                rSyntaxTextArea1.write(writer);
            }
    }
    
    public FileObject getCurrentFO (){
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
        if (currentFO!=null){
            String name = currentFO.getNameExt();
            if (modificado) {
                setHtmlDisplayName("<html><b>*" + name + "</b></html>");
            } else {
                setHtmlDisplayName("<html>" + name + "</html>");
            }
        }
    }
    
    public void setState(boolean state){
        modifiedState = state;
        updateTitle (modifiedState);
    }
    
    private FileObject getProjectDirectory (FileObject fo){
        FileObject current = fo;
        
        while ( current!=null && !current.getName().equals("src") ){
            current = current.getParent();
        }
        if (current!=null){
            return current.getParent();
        }
        
        return null;
    }
    
    
    public static File getFolderBin(FileObject javaFo) {
        FileObject current = javaFo;
        // Sube hasta encontrar "src"
        while (current != null && !current.getName().equals("src")) {
            current = current.getParent();
        }
        if (current == null) {
            // No encontramos src; devolvemos null o un default si quieres
            return null;
        }
        // current es la carpeta src; su padre es la raíz del proyecto
        File projectRoot = new File(current.getParent().getPath());
        // La carpeta bin junto al proyecto
        File binDir = new File(projectRoot, "bin");
        return binDir.exists() ? binDir : null;
    }
    
public void setLineTrackIcon(int newLineNumber) throws BadLocationException {
    Image executionImage = ImageUtilities.loadImage("org/ide/code/debugger/greenArrow.png");
    ImageIcon executionIcon = new ImageIcon(executionImage);
    
    Image bpImage = ImageUtilities.loadImage("org/ide/code/debugger/redButton.png");
    ImageIcon breakpointIcon = new ImageIcon(bpImage);

    // Paso 1: Limpiar el icono de ejecución anterior
    if (currentExecutionIcon != null) {
        gutter.removeTrackingIcon(currentExecutionIcon);
        currentExecutionIcon = null; // Limpiar la referencia
    }

    // Paso 2: Manejar el breakpoint en la nueva línea
    if (breakpointIcons.containsKey(newLineNumber)) {
        // Si hay un breakpoint en la nueva línea, lo eliminamos
        GutterIconInfo removedBreakpoint = breakpointIcons.remove(newLineNumber);
        gutter.removeTrackingIcon(removedBreakpoint);
        breakpointIcons.put(newLineNumber, null);
    }

    // Paso 3: Añadir el icono de ejecución
    try {
        currentExecutionIcon = gutter.addLineTrackingIcon(newLineNumber, executionIcon);
        currentExecutionLine = newLineNumber;
    } catch (BadLocationException e) {
    }

    // Paso 4: Restaurar el breakpoint anterior si es necesario
    if (breakpointIcons.containsKey(currentExecutionLine)) {
        GutterIconInfo restoredBreakpoint = gutter.addLineTrackingIcon(currentExecutionLine, breakpointIcon);
        breakpointIcons.put(currentExecutionLine, restoredBreakpoint);
    }
}



    
    private void print(String text){
        System.out.println(text);
    }
    
}
