package com.usac.contacto3d.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base de todos los nodos del AST.
 *
 * DECISION CENTRAL DEL PROYECTO: hay UN SOLO AST para los tres lenguajes.
 * Los tres front-ends (.y, .z, .pig) producen este mismo arbol, y de ahi en
 * adelante hay un solo analizador semantico y un solo generador de cuartetas.
 * Si cada lenguaje tuviera su propio AST habria que escribir tres generadores
 * de C3D y el proyecto se triplicaria.
 *
 * Cada nodo recuerda de que ARCHIVO viene, porque un programa se arma de
 * varios (.pig que importa .y y .z) y los errores deben decir en cual.
 */
public abstract class Nodo {

    protected final int linea;
    protected final int columna;
    protected String archivo = "";

    protected Nodo(int linea, int columna) {
        this.linea = linea;
        this.columna = columna;
    }

    public int getLinea()      { return linea; }
    public int getColumna()    { return columna; }
    public String getArchivo() { return archivo; }

    public void setArchivo(String archivo) {
        this.archivo = archivo == null ? "" : archivo;
    }

    /** Texto para graficar o depurar el arbol. */
    public abstract String getEtiqueta();

    /** Hijos en orden. Nunca null. */
    public abstract List<Nodo> getHijos();

    /** Recorrido con logica (semantico, generador de cuartetas). */
    public abstract <T> T aceptar(Visitante<T> visitante);

    /**
     * Arma la lista de hijos ignorando nulos y aplanando colecciones,
     * para que getHijos() sea casi siempre una sola linea.
     */
    protected static List<Nodo> hijos(Object... elementos) {
        List<Nodo> lista = new ArrayList<>();
        for (Object elemento : elementos) {
            agregar(lista, elemento);
        }
        return lista;
    }

    private static void agregar(List<Nodo> lista, Object elemento) {
        if (elemento == null) {
            return;
        }
        if (elemento instanceof Collection<?> coleccion) {
            for (Object item : coleccion) {
                agregar(lista, item);
            }
        } else if (elemento instanceof Nodo nodo) {
            lista.add(nodo);
        }
    }

    @Override
    public String toString() {
        return getEtiqueta() + " (" + archivo + ":" + linea + ")";
    }
}
