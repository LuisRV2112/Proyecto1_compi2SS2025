package com.usac.contacto3d.errores;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;

public class ErroresLexicos extends BaseErrorListener {

    private final ListaErrores errores;

    public ErroresLexicos(ListaErrores errores) {
        this.errores = errores;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int linea, int columna, String mensaje, RecognitionException e) {

        if (e instanceof ErrorConMensaje && offendingSymbol instanceof Token token) {
            errores.agregar(TipoError.LEXICO, token.getText(), mensaje, linea, columna + 1);
            return;
        }

        String lexema = extraerLexema(recognizer, mensaje);

        errores.agregar(TipoError.LEXICO, lexema,
                "Caracter no reconocido por el lenguaje: '" + lexema + "'",
                linea, columna + 1);
    }

    private String extraerLexema(Recognizer<?, ?> recognizer, String mensaje) {
        if (recognizer instanceof Lexer lexer) {
            String texto = lexer.getErrorDisplay(
                    lexer.getInputStream().getText(
                            Interval.of(lexer._tokenStartCharIndex,
                                        lexer.getInputStream().index())));
            if (texto != null && !texto.isEmpty()) {
                return texto;
            }
        }
        int comilla = mensaje.indexOf('\'');
        if (comilla >= 0 && mensaje.lastIndexOf('\'') > comilla) {
            return mensaje.substring(comilla + 1, mensaje.lastIndexOf('\''));
        }
        return mensaje;
    }
}
