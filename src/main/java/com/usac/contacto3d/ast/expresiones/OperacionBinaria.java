package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;

import java.util.List;

public class OperacionBinaria extends Expresion {

    private final Operador operador;
    private final Expresion izquierda;
    private final Expresion derecha;

    public OperacionBinaria(Operador operador, Expresion izquierda, Expresion derecha,
                            int linea, int columna) {
        super(linea, columna);
        this.operador = operador;
        this.izquierda = izquierda;
        this.derecha = derecha;
    }

    public Operador getOperador()  { return operador; }
    public Expresion getIzquierda() { return izquierda; }
    public Expresion getDerecha()   { return derecha; }

    @Override
    public String getEtiqueta() {
        return operador.getSimbolo();
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(izquierda, derecha);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarOperacionBinaria(this);
    }
}
