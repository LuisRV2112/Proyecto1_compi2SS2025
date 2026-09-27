package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;

import java.util.List;

public class OperacionUnaria extends Expresion {

    private final Operador operador;
    private final Expresion operando;

    public OperacionUnaria(Operador operador, Expresion operando, int linea, int columna) {
        super(linea, columna);
        this.operador = operador;
        this.operando = operando;
    }

    public Operador getOperador() { return operador; }
    public Expresion getOperando() { return operando; }

    @Override
    public String getEtiqueta() {
        return operador == Operador.NEGATIVO ? "- (unario)" : operador.getSimbolo();
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(operando);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarOperacionUnaria(this);
    }
}
