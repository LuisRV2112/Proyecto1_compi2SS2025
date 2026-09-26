package com.usac.contacto3d.ui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.Gutter;

import java.awt.Color;
import java.awt.Font;

/**
 * Paleta del editor. Define un color por cada tipo de token que producen los
 * coloreadores de los tres lenguajes.
 *
 * Los colores son libres segun el enunciado. Lo que NO es libre es de donde
 * salen: el coloreado debe generarse desde los lexers propios, sin librerias.
 */
public final class TemaEditor {

    public static final Color FONDO        = new Color(0x1E1E1E);
    public static final Color FONDO_PANEL  = new Color(0x252526);
    public static final Color TEXTO        = new Color(0xD4D4D4);
    public static final Color LINEA_ACTUAL = new Color(0x2A2D2E);
    public static final Color SELECCION    = new Color(0x264F78);
    public static final Color CURSOR       = new Color(0xAEAFAD);
    public static final Color BORDE        = new Color(0x3E3E42);

    private static final Color SECCION       = new Color(0xC586C0);
    private static final Color RESERVADA     = new Color(0x569CD6);
    private static final Color TIPO_DATO     = new Color(0x4EC9B0);
    private static final Color IDENTIFICADOR = new Color(0x9CDCFE);
    private static final Color FUNCION       = new Color(0xDCDCAA);
    private static final Color NUMERO        = new Color(0xB5CEA8);
    private static final Color CADENA        = new Color(0xCE9178);
    private static final Color COMENTARIO    = new Color(0x6A9955);
    private static final Color OPERADOR      = new Color(0xD4D4D4);
    private static final Color SEPARADOR     = new Color(0xFFD700);
    private static final Color ERROR         = new Color(0xF44747);

    private TemaEditor() {
    }

    public static void aplicar(RSyntaxTextArea editor) {
        editor.setBackground(FONDO);
        editor.setForeground(TEXTO);
        editor.setCaretColor(CURSOR);
        editor.setCurrentLineHighlightColor(LINEA_ACTUAL);
        editor.setSelectionColor(SELECCION);
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        editor.setTabSize(4);
        editor.setMarkOccurrences(true);
        editor.setPaintTabLines(true);

        SyntaxScheme esquema = editor.getSyntaxScheme();

        pintar(esquema, Token.PREPROCESSOR, SECCION, true);
        pintar(esquema, Token.RESERVED_WORD, RESERVADA, true);
        pintar(esquema, Token.RESERVED_WORD_2, RESERVADA, true);
        pintar(esquema, Token.DATA_TYPE, TIPO_DATO, false);
        pintar(esquema, Token.LITERAL_BOOLEAN, RESERVADA, true);
        pintar(esquema, Token.IDENTIFIER, IDENTIFICADOR, false);
        pintar(esquema, Token.FUNCTION, FUNCION, false);
        pintar(esquema, Token.LITERAL_NUMBER_DECIMAL_INT, NUMERO, false);
        pintar(esquema, Token.LITERAL_NUMBER_FLOAT, NUMERO, false);
        pintar(esquema, Token.LITERAL_STRING_DOUBLE_QUOTE, CADENA, false);
        pintar(esquema, Token.LITERAL_CHAR, CADENA, false);
        pintar(esquema, Token.COMMENT_EOL, COMENTARIO, false);
        pintar(esquema, Token.COMMENT_MULTILINE, COMENTARIO, false);
        pintar(esquema, Token.OPERATOR, OPERADOR, false);
        pintar(esquema, Token.SEPARATOR, SEPARADOR, false);
        pintar(esquema, Token.ERROR_IDENTIFIER, ERROR, true);
        pintar(esquema, Token.ERROR_CHAR, ERROR, true);
        pintar(esquema, Token.ERROR_NUMBER_FORMAT, ERROR, true);
        pintar(esquema, Token.ERROR_STRING_DOUBLE, ERROR, true);

        editor.revalidate();
    }

    private static void pintar(SyntaxScheme esquema, int tipoToken, Color color, boolean negrita) {
        Style estilo = esquema.getStyle(tipoToken);
        if (estilo == null) {
            estilo = new Style();
            esquema.setStyle(tipoToken, estilo);
        }
        estilo.foreground = color;
        if (negrita && estilo.font != null) {
            estilo.font = estilo.font.deriveFont(Font.BOLD);
        }
    }

    public static void aplicarAlMargen(Gutter margen) {
        margen.setBackground(FONDO_PANEL);
        margen.setBorderColor(BORDE);
        margen.setLineNumberColor(new Color(0x858585));
        margen.setLineNumberFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }
}
