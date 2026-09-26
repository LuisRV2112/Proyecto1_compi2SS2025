package com.usac.contacto3d.errores;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.IntervalSet;

import java.util.Map;

/**
 * Captura los errores sintacticos y los traduce a mensajes en espanol,
 * incluyendo que tokens se esperaban y en que regla ocurrio.
 */
public class ErroresSintacticos extends BaseErrorListener {

    private static final int MAX_ESPERADOS = 6;

    /** Tokens sin texto propio (o con uno invisible) y como se le nombran al usuario. */
    private static final Map<String, String> NOMBRES_LEGIBLES = Map.of(
            "NUEVA_LINEA", "fin de linea",
            "INDENT", "inicio de bloque (mas sangria)",
            "DEDENT", "fin de bloque (menos sangria)");

    private final ListaErrores errores;

    public ErroresSintacticos(ListaErrores errores) {
        this.errores = errores;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int linea, int columna, String mensaje, RecognitionException e) {

        String lexema = "";
        if (offendingSymbol instanceof Token token) {
            lexema = lexemaLegible(token);
        }

        if (e instanceof ErrorConMensaje) {
            errores.agregar(TipoError.SINTACTICO, lexema, mensaje, linea, columna + 1);
            return;
        }

        StringBuilder descripcion = new StringBuilder("No se esperaba '").append(lexema).append("'");

        String esperados = tokensEsperados(recognizer, e);
        if (!esperados.isEmpty()) {
            descripcion.append(". Se esperaba: ").append(esperados);
        }

        String regla = reglaActual(recognizer);
        if (regla != null) {
            descripcion.append(" (dentro de: ").append(regla).append(")");
        }

        errores.agregar(TipoError.SINTACTICO, lexema, descripcion.toString(), linea, columna + 1);
    }

    private static String lexemaLegible(Token token) {
        if (token.getType() == Token.EOF) {
            return "<fin de archivo>";
        }
        String texto = token.getText();
        return texto != null && texto.isBlank() && (texto.contains("\n") || texto.contains("\r"))
                ? "<fin de linea>"
                : texto;
    }

    private String tokensEsperados(Recognizer<?, ?> recognizer, RecognitionException e) {
        if (!(recognizer instanceof Parser parser)) {
            return "";
        }
        IntervalSet esperados = (e != null && e.getExpectedTokens() != null)
                ? e.getExpectedTokens()
                : parser.getExpectedTokens();

        if (esperados == null || esperados.size() == 0) {
            return "";
        }

        Vocabulary vocabulario = parser.getVocabulary();
        StringBuilder sb = new StringBuilder();
        int contador = 0;

        for (int tipo : esperados.toList()) {
            if (contador >= MAX_ESPERADOS) {
                sb.append(", ...");
                break;
            }
            String nombre = tipo == Token.EOF ? "fin de archivo"
                    : NOMBRES_LEGIBLES.getOrDefault(vocabulario.getSymbolicName(tipo),
                                                    vocabulario.getDisplayName(tipo));
            if (nombre == null || nombre.isBlank()) {
                continue;
            }
            if (contador > 0) {
                sb.append(", ");
            }
            sb.append(nombre);
            contador++;
        }
        return sb.toString();
    }

    private String reglaActual(Recognizer<?, ?> recognizer) {
        if (recognizer instanceof Parser parser && parser.getContext() != null) {
            int indice = parser.getContext().getRuleIndex();
            String[] reglas = parser.getRuleNames();
            if (indice >= 0 && indice < reglas.length) {
                return reglas[indice];
            }
        }
        return null;
    }
}
