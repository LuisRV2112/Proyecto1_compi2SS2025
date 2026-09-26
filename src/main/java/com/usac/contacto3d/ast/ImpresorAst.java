package com.usac.contacto3d.ast;

import java.util.List;

/** Dibuja el AST con lineas, para depurar desde consola. */
public final class ImpresorAst {

    private ImpresorAst() { }

    public static String dibujar(Nodo raiz) {
        StringBuilder salida = new StringBuilder(raiz.getEtiqueta()).append('\n');
        List<Nodo> hijos = raiz.getHijos();
        for (int i = 0; i < hijos.size(); i++) {
            dibujar(hijos.get(i), "", i == hijos.size() - 1, salida);
        }
        return salida.toString();
    }

    private static void dibujar(Nodo nodo, String prefijo, boolean esUltimo, StringBuilder salida) {
        salida.append(prefijo).append(esUltimo ? "'-- " : "|-- ")
              .append(nodo.getEtiqueta()).append('\n');
        List<Nodo> hijos = nodo.getHijos();
        String siguiente = prefijo + (esUltimo ? "    " : "|   ");
        for (int i = 0; i < hijos.size(); i++) {
            dibujar(hijos.get(i), siguiente, i == hijos.size() - 1, salida);
        }
    }
}
