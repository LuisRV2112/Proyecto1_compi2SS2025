package com.usac.contacto3d.ui;

import com.usac.contacto3d.errores.ListaErrores;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PestanaEditor extends JPanel {

    public static final String ESTILO_Y = "text/lenguaje-y";
    public static final String ESTILO_ZETARIANO = "text/zetariano";
    public static final String ESTILO_PIGLATIN = "text/piglatin";

    static {
        AbstractTokenMakerFactory fabrica = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        fabrica.putMapping(ESTILO_Y, ColoreadorY.class.getName());
        fabrica.putMapping(ESTILO_ZETARIANO, ColoreadorZ.class.getName());
        fabrica.putMapping(ESTILO_PIGLATIN, ColoreadorPig.class.getName());
    }

    private final RSyntaxTextArea editor = new RSyntaxTextArea();
    private final ParserErrores marcas = new ParserErrores();
    private Path archivo;
    private boolean modificado;
    private Runnable alCambiarEstado = () -> { };

    public PestanaEditor(Path archivo) throws IOException {
        super(new BorderLayout());
        this.archivo = archivo;

        TemaEditor.aplicar(editor);
        editor.setSyntaxEditingStyle(estiloPara(archivo));
        editor.setText(Files.readString(archivo, StandardCharsets.UTF_8));
        editor.setCaretPosition(0);
        editor.discardAllEdits();
        editor.addParser(marcas);

        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { marcarModificado(); }
            @Override public void removeUpdate(DocumentEvent e)  { marcarModificado(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        });

        RTextScrollPane scroll = new RTextScrollPane(editor);
        scroll.setBorder(null);
        TemaEditor.aplicarAlMargen(scroll.getGutter());
        add(scroll, BorderLayout.CENTER);
    }

    public static String estiloPara(Path archivo) {
        String nombre = archivo.getFileName().toString();
        if (nombre.endsWith(".y")) return ESTILO_Y;
        if (nombre.endsWith(".z")) return ESTILO_ZETARIANO;
        if (nombre.endsWith(".pig")) return ESTILO_PIGLATIN;
        return SyntaxConstants.SYNTAX_STYLE_NONE;
    }

    public void guardar() throws IOException {
        Files.writeString(archivo, editor.getText(), StandardCharsets.UTF_8);
        modificado = false;
        alCambiarEstado.run();
    }

    public void guardarComo(Path destino) throws IOException {
        archivo = destino;
        editor.setSyntaxEditingStyle(estiloPara(destino));
        guardar();
    }

    public void setArchivo(Path archivo) {
        this.archivo = archivo;
        editor.setSyntaxEditingStyle(estiloPara(archivo));
        alCambiarEstado.run();
    }

    public void marcarErrores(ListaErrores errores) {
        marcas.setErrores(errores, archivo.getFileName().toString());
        editor.forceReparsing(marcas);
    }

    public void irALinea(int linea) {
        try {
            int destino = Math.max(0, Math.min(linea - 1, editor.getLineCount() - 1));
            editor.setCaretPosition(editor.getLineStartOffset(destino));
            editor.requestFocusInWindow();
        } catch (BadLocationException e) {
        }
    }

    private void marcarModificado() {
        if (!modificado) {
            modificado = true;
            alCambiarEstado.run();
        }
    }

    public void setAlCambiarEstado(Runnable accion) { this.alCambiarEstado = accion; }
    public Path getArchivo()                        { return archivo; }
    public boolean isModificado()                   { return modificado; }
    public RSyntaxTextArea getEditor()              { return editor; }

    public String getTitulo() {
        return (modificado ? "* " : "") + archivo.getFileName();
    }
}
