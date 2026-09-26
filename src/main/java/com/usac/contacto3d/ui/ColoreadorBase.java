package com.usac.contacto3d.ui;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import javax.swing.text.Segment;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Base del coloreado de los tres lenguajes.
 *
 * El enunciado PROHIBE librerias para colorear, asi que esta clase NO usa las
 * palabras predefinidas de RSyntaxTextArea: getWordsToHighlight() devuelve un
 * mapa vacio a proposito. El color lo decide el lexer de ANTLR de cada
 * lenguaje, que es analisis lexico propio.
 *
 * Cada lenguaje hereda de aqui y solo implementa dos cosas:
 *   crearLexer(texto)  -> instancia su lexer generado por ANTLR
 *   colorDe(tipoToken) -> traduce un tipo de token suyo a un color
 *
 * Tres detalles que hubo que resolver y conviene no romper:
 *
 * 1. RSyntaxTextArea exige que los tokens cubran la linea COMPLETA sin huecos.
 *    Por eso los espacios y comentarios deben ir al canal oculto de ANTLR
 *    (-> channel(HIDDEN)) en vez de descartarse con skip: el parser los sigue
 *    ignorando, pero aqui se pueden ver.
 *
 * 2. Los caracteres que el lexer no reconoce dejarian un hueco; se rellenan
 *    como token de error, que ademas es lo que se quiere pintar de rojo.
 *
 * 3. Los comentarios de varias lineas abarcan mas de una, y RSyntaxTextArea
 *    entrega una a la vez, asi que su estado se propaga manualmente.
 */
public abstract class ColoreadorBase extends AbstractTokenMaker {

    /** Marca de "esta linea empezo dentro de un comentario de bloque". */
    protected static final int DENTRO_DE_BLOQUE =
            org.fife.ui.rsyntaxtextarea.Token.COMMENT_MULTILINE;

    /** Crea el lexer del lenguaje concreto. */
    protected abstract Lexer crearLexer(String texto);

    /** Traduce un tipo de token de ANTLR al tipo de color de RSyntaxTextArea. */
    protected abstract int colorDe(int tipoToken);

    /** Delimitadores del comentario de bloque, o null si el lenguaje no tiene. */
    protected abstract String getAperturaBloque();
    protected abstract String getCierreBloque();

    @Override
    public TokenMap getWordsToHighlight() {
        // Vacio a proposito: las palabras reservadas las decide el lexer propio.
        return new TokenMap();
    }

    @Override
    public org.fife.ui.rsyntaxtextarea.Token getTokenList(Segment texto,
                                                          int tipoInicial,
                                                          int desplazamiento) {
        resetTokenList();

        final int inicio = texto.offset;
        final int fin = texto.offset + texto.count;
        int posicion = inicio;

        String apertura = getAperturaBloque();
        String cierre = getCierreBloque();
        boolean hayBloques = apertura != null && cierre != null;

        /* Caso 1: la linea viene dentro de un comentario de bloque. */
        if (hayBloques && tipoInicial == DENTRO_DE_BLOQUE) {
            int posCierre = buscar(texto, inicio, fin, cierre);
            if (posCierre < 0) {
                agregar(texto, inicio, Math.max(inicio, fin - 1), DENTRO_DE_BLOQUE, desplazamiento);
                return firstToken;   // sin addNullToken: el estado se propaga
            }
            int finComentario = posCierre + cierre.length() - 1;
            agregar(texto, inicio, finComentario, DENTRO_DE_BLOQUE, desplazamiento);
            posicion = finComentario + 1;
        }

        /* Caso 2: recorrer el resto de la linea. */
        while (posicion < fin) {
            int posApertura = hayBloques ? buscar(texto, posicion, fin, apertura) : -1;

            if (posApertura < 0) {
                colorearConLexer(texto, posicion, fin, desplazamiento);
                break;
            }

            if (posApertura > posicion) {
                colorearConLexer(texto, posicion, posApertura, desplazamiento);
            }

            int posCierre = buscar(texto, posApertura + apertura.length(), fin, cierre);
            if (posCierre < 0) {
                agregar(texto, posApertura, fin - 1, DENTRO_DE_BLOQUE, desplazamiento);
                return firstToken;   // el bloque sigue en las lineas de abajo
            }
            int finComentario = posCierre + cierre.length() - 1;
            agregar(texto, posApertura, finComentario, DENTRO_DE_BLOQUE, desplazamiento);
            posicion = finComentario + 1;
        }

        addNullToken();
        return firstToken;
    }

    /** Corre el lexer sobre un tramo y rellena los huecos como error. */
    protected void colorearConLexer(Segment texto, int desde, int hasta, int desplazamiento) {
        String fragmento = new String(texto.array, desde, hasta - desde);

        List<Token> tokens = new ArrayList<>();
        try {
            Lexer lexer = crearLexer(fragmento);
            lexer.removeErrorListeners();   // los errores se pintan, no se reportan

            Token token = lexer.nextToken();
            while (token.getType() != Token.EOF) {
                tokens.add(token);
                token = lexer.nextToken();
            }
        } catch (RuntimeException e) {
            agregar(texto, desde, hasta - 1,
                    org.fife.ui.rsyntaxtextarea.Token.ERROR_IDENTIFIER, desplazamiento);
            return;
        }

        int cursor = desde;
        for (Token token : tokens) {
            int inicioToken = desde + token.getStartIndex();
            int finToken = desde + token.getStopIndex();

            if (inicioToken > cursor) {
                agregar(texto, cursor, inicioToken - 1,
                        org.fife.ui.rsyntaxtextarea.Token.ERROR_IDENTIFIER, desplazamiento);
            }
            agregar(texto, inicioToken, finToken, colorDe(token.getType()), desplazamiento);
            cursor = finToken + 1;
        }

        if (cursor < hasta) {
            agregar(texto, cursor, hasta - 1,
                    org.fife.ui.rsyntaxtextarea.Token.ERROR_IDENTIFIER, desplazamiento);
        }
    }

    /** addToken calculando el desplazamiento dentro del documento. */
    protected void agregar(Segment texto, int inicio, int fin, int tipo, int desplazamiento) {
        if (fin < inicio) {
            return;
        }
        addToken(texto, inicio, fin, tipo, desplazamiento + (inicio - texto.offset));
    }

    /** Busca una secuencia en el tramo. -1 si no aparece. */
    protected int buscar(Segment texto, int desde, int hasta, String secuencia) {
        int largo = secuencia.length();
        for (int i = desde; i <= hasta - largo; i++) {
            boolean coincide = true;
            for (int j = 0; j < largo; j++) {
                if (texto.array[i + j] != secuencia.charAt(j)) {
                    coincide = false;
                    break;
                }
            }
            if (coincide) {
                return i;
            }
        }
        return -1;
    }

    /** Atajo para que las subclases creen su lexer en una linea. */
    protected static Lexer construir(String texto, Function<org.antlr.v4.runtime.CharStream, Lexer> fabrica) {
        return fabrica.apply(CharStreams.fromString(texto));
    }
}
