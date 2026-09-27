package com.usac.contacto3d.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    public abstract String getEtiqueta();

    public abstract List<Nodo> getHijos();

    public abstract <T> T aceptar(Visitante<T> visitante);

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
