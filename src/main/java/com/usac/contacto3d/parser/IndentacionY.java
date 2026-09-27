package com.usac.contacto3d.parser;

import com.usac.contacto3d.errores.ErrorConMensaje;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.Supplier;

public class IndentacionY {

    private static final int ANCHO_TAB = 4;

    private final Lexer lexer;
    private final Deque<Token> pendientes = new ArrayDeque<>();
    private final Deque<Integer> niveles = new ArrayDeque<>();
    private final Deque<Boolean> llaves = new ArrayDeque<>();

    private boolean inicioLinea;
    private int anchoIndentacion;
    private boolean sangriaMedida;
    private boolean lineaConContenido;
    private int literalesAbiertos;
    private boolean llaveDeElegirPendiente;

    public IndentacionY(Lexer lexer) {
        this.lexer = lexer;
        reiniciar();
    }

    public final void reiniciar() {
        pendientes.clear();
        niveles.clear();
        niveles.push(0);
        llaves.clear();
        literalesAbiertos = 0;
        nuevaLinea();
    }

    public Token siguiente(Supplier<Token> lexerCrudo) {
        while (pendientes.isEmpty()) {
            procesar(lexerCrudo.get());
        }
        return pendientes.poll();
    }

    private void procesar(Token token) {
        int tipo = token.getType();

        if (tipo == Token.EOF) {
            cerrarTodo(token);
            pendientes.add(token);
            return;
        }

        if (token.getChannel() != Token.DEFAULT_CHANNEL) {
            if (inicioLinea && !sangriaMedida) {
                if (tipo == LenguajeYLexer.WS) {
                    anchoIndentacion = medir(token.getText(), anchoIndentacion);
                } else {
                    sangriaMedida = true;
                }
            }
            pendientes.add(token);
            return;
        }

        if (tipo == LenguajeYLexer.NUEVA_LINEA) {
            if (literalesAbiertos > 0) {
                ocultar(token);
            } else {
                if (!lineaConContenido) {
                    ocultar(token);
                }
                nuevaLinea();
            }
            pendientes.add(token);
            return;
        }

        if (inicioLinea) {
            ajustarNivel(token);
            inicioLinea = false;
            lineaConContenido = true;
        }
        seguirLlaves(token);
        pendientes.add(token);
    }

    private void ajustarNivel(Token token) {
        int actual = niveles.peek();

        if (anchoIndentacion > actual) {
            niveles.push(anchoIndentacion);
            pendientes.add(artificial(LenguajeYParser.INDENT, "<inicio de bloque>", token));
            return;
        }

        while (anchoIndentacion < niveles.peek() && anchoIndentacion <= nivelDebajoDelTope()) {
            niveles.pop();
            pendientes.add(artificial(LenguajeYParser.DEDENT, "<fin de bloque>", token));
        }

        if (anchoIndentacion != niveles.peek()) {
            lexer.getErrorListenerDispatch().syntaxError(lexer, token,
                    token.getLine(), token.getCharPositionInLine(),
                    "Indentacion inconsistente: la linea tiene sangria de " + anchoIndentacion
                            + " columnas y no coincide con ningun bloque abierto. Se esperaba "
                            + nivelesAbiertos(),
                    new ErrorConMensaje(lexer));
        }
    }

    private int nivelDebajoDelTope() {
        Iterator<Integer> it = niveles.iterator();
        it.next();
        return it.hasNext() ? it.next() : -1;
    }

    private String nivelesAbiertos() {
        StringBuilder sb = new StringBuilder();
        Iterator<Integer> it = niveles.descendingIterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.append(" columnas").toString();
    }

    private void seguirLlaves(Token token) {
        switch (token.getType()) {
            case LenguajeYLexer.ELEGIR -> llaveDeElegirPendiente = true;
            case LenguajeYLexer.LLAVE_A -> {
                boolean literal = !llaveDeElegirPendiente;
                llaveDeElegirPendiente = false;
                llaves.push(literal);
                if (literal) {
                    literalesAbiertos++;
                }
            }
            case LenguajeYLexer.LLAVE_C -> {
                if (!llaves.isEmpty() && llaves.pop()) {
                    literalesAbiertos--;
                }
            }
            default -> { }
        }
    }

    private void cerrarTodo(Token eof) {
        if (lineaConContenido) {
            pendientes.add(artificial(LenguajeYLexer.NUEVA_LINEA, "<fin de linea>", eof));
            nuevaLinea();
        }
        while (niveles.peek() > 0) {
            niveles.pop();
            pendientes.add(artificial(LenguajeYParser.DEDENT, "<fin de bloque>", eof));
        }
    }

    private void nuevaLinea() {
        inicioLinea = true;
        anchoIndentacion = 0;
        sangriaMedida = false;
        lineaConContenido = false;
        llaveDeElegirPendiente = false;
    }

    private static int medir(String espacios, int desde) {
        int ancho = desde;
        for (char c : espacios.toCharArray()) {
            ancho = c == '\t' ? ancho + ANCHO_TAB - ancho % ANCHO_TAB : ancho + 1;
        }
        return ancho;
    }

    private static void ocultar(Token token) {
        ((CommonToken) token).setChannel(Token.HIDDEN_CHANNEL);
    }

    private Token artificial(int tipo, String texto, Token referencia) {
        int inicio = referencia.getStartIndex();
        CommonToken token = new CommonToken(
                new org.antlr.v4.runtime.misc.Pair<>(lexer, lexer.getInputStream()),
                tipo, Token.DEFAULT_CHANNEL, inicio, inicio - 1);
        token.setText(texto);
        token.setLine(referencia.getLine());
        token.setCharPositionInLine(referencia.getCharPositionInLine());
        return token;
    }
}
