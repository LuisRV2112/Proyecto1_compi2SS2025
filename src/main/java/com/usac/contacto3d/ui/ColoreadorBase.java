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

public abstract class ColoreadorBase extends AbstractTokenMaker {

    protected static final int DENTRO_DE_BLOQUE =
            org.fife.ui.rsyntaxtextarea.Token.COMMENT_MULTILINE;

    protected abstract Lexer crearLexer(String texto);

    protected abstract int colorDe(int tipoToken);

    protected abstract String getAperturaBloque();
    protected abstract String getCierreBloque();

    @Override
    public TokenMap getWordsToHighlight() {
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

        if (hayBloques && tipoInicial == DENTRO_DE_BLOQUE) {
            int posCierre = buscar(texto, inicio, fin, cierre);
            if (posCierre < 0) {
                agregar(texto, inicio, Math.max(inicio, fin - 1), DENTRO_DE_BLOQUE, desplazamiento);
                return firstToken;
            }
            int finComentario = posCierre + cierre.length() - 1;
            agregar(texto, inicio, finComentario, DENTRO_DE_BLOQUE, desplazamiento);
            posicion = finComentario + 1;
        }

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
                return firstToken;
            }
            int finComentario = posCierre + cierre.length() - 1;
            agregar(texto, posApertura, finComentario, DENTRO_DE_BLOQUE, desplazamiento);
            posicion = finComentario + 1;
        }

        addNullToken();
        return firstToken;
    }

    protected void colorearConLexer(Segment texto, int desde, int hasta, int desplazamiento) {
        String fragmento = new String(texto.array, desde, hasta - desde);

        List<Token> tokens = new ArrayList<>();
        try {
            Lexer lexer = crearLexer(fragmento);
            lexer.removeErrorListeners();

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

    protected void agregar(Segment texto, int inicio, int fin, int tipo, int desplazamiento) {
        if (fin < inicio) {
            return;
        }
        addToken(texto, inicio, fin, tipo, desplazamiento + (inicio - texto.offset));
    }

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

    protected static Lexer construir(String texto, Function<org.antlr.v4.runtime.CharStream, Lexer> fabrica) {
        return fabrica.apply(CharStreams.fromString(texto));
    }
}
