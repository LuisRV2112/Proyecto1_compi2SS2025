package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;

import java.util.List;

public class LiteralLista extends Expresion {

    private final List<Expresion> elementos;

    public LiteralLista(List<Expresion> elementos, int linea, int columna) {
        super(linea, columna);
        this.elementos = List.copyOf(elementos);
    }

    public List<Expresion> getElementos() { return elementos; }

    @Override
    public String getEtiqueta() {
        return "{ } (" + elementos.size() + ")";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(elementos);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarLiteralLista(this);
    }
}
