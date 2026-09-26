package com.usac.contacto3d.constructores;

import com.usac.contacto3d.ast.Nodo;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Lo que comparten los tres constructores y no depende de la gramatica.
 *
 * Las columnas se cuentan desde 1, igual que en los errores lexicos y
 * sintacticos, para que todos los mensajes apunten al mismo lugar.
 */
final class Ayudante {

    private Ayudante() { }

    static int linea(ParserRuleContext ctx)   { return ctx.getStart().getLine(); }
    static int columna(ParserRuleContext ctx) { return ctx.getStart().getCharPositionInLine() + 1; }
    static int linea(Token token)             { return token.getLine(); }
    static int columna(Token token)           { return token.getCharPositionInLine() + 1; }
    static int linea(TerminalNode nodo)       { return linea(nodo.getSymbol()); }
    static int columna(TerminalNode nodo)     { return columna(nodo.getSymbol()); }

    /** El token de un operador binario: en "expr op expr" es el hijo del medio. */
    static TerminalNode operador(ParserRuleContext ctx) {
        ParseTree hijo = ctx.getChild(1);
        return (TerminalNode) hijo;
    }

    /**
     * Se asigna al final recorriendo getHijos() en vez de pasarlo a cada
     * constructor de nodo: asi ningun nodo se queda sin archivo por olvido.
     */
    static void asignarArchivo(Nodo nodo, String archivo) {
        nodo.setArchivo(archivo);
        for (Nodo hijo : nodo.getHijos()) {
            asignarArchivo(hijo, archivo);
        }
    }

    /**
     * Integer si cabe; si no, Long, para que el semantico reporte el
     * desbordamiento con la linea del literal en vez de fallar aqui.
     */
    static Object entero(String lexema) {
        long valor = Long.parseLong(lexema);
        return valor <= Integer.MAX_VALUE ? (Object) (int) valor : (Object) valor;
    }

    /** "texto\n" -> texto con salto real. */
    static String cadena(String lexema) {
        return desescapar(lexema.substring(1, lexema.length() - 1));
    }

    /** 'a' o '\n' */
    static Character caracter(String lexema) {
        return desescapar(lexema.substring(1, lexema.length() - 1)).charAt(0);
    }

    private static String desescapar(String texto) {
        StringBuilder sb = new StringBuilder(texto.length());
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c != '\\' || i + 1 == texto.length()) {
                sb.append(c);
                continue;
            }
            char siguiente = texto.charAt(++i);
            sb.append(switch (siguiente) {
                case 'n' -> '\n';
                case 't' -> '\t';
                case 'r' -> '\r';
                case '0' -> '\0';
                default -> siguiente;   // \" \' \\ y cualquier otro: el caracter tal cual
            });
        }
        return sb.toString();
    }
}
