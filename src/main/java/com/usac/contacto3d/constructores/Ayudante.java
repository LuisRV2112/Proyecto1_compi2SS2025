package com.usac.contacto3d.constructores;

import com.usac.contacto3d.ast.Nodo;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

final class Ayudante {

    private Ayudante() { }

    static int linea(ParserRuleContext ctx)   { return ctx.getStart().getLine(); }
    static int columna(ParserRuleContext ctx) { return ctx.getStart().getCharPositionInLine() + 1; }
    static int linea(Token token)             { return token.getLine(); }
    static int columna(Token token)           { return token.getCharPositionInLine() + 1; }
    static int linea(TerminalNode nodo)       { return linea(nodo.getSymbol()); }
    static int columna(TerminalNode nodo)     { return columna(nodo.getSymbol()); }

    static TerminalNode operador(ParserRuleContext ctx) {
        ParseTree hijo = ctx.getChild(1);
        return (TerminalNode) hijo;
    }

    static void asignarArchivo(Nodo nodo, String archivo) {
        nodo.setArchivo(archivo);
        for (Nodo hijo : nodo.getHijos()) {
            asignarArchivo(hijo, archivo);
        }
    }

    static Object entero(String lexema) {
        long valor = Long.parseLong(lexema);
        return valor <= Integer.MAX_VALUE ? (Object) (int) valor : (Object) valor;
    }

    static String cadena(String lexema) {
        return desescapar(lexema.substring(1, lexema.length() - 1));
    }

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
                default -> siguiente;
            });
        }
        return sb.toString();
    }
}
