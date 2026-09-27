package com.usac.contacto3d.ast.instrucciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.expresiones.Acceso;
import com.usac.contacto3d.ast.expresiones.Expresion;
import com.usac.contacto3d.ast.expresiones.Operador;

import java.util.List;

public class Asignacion extends Nodo implements Instruccion {

    private final Acceso destino;
    private final Operador operador;
    private final Expresion valor;

    public Asignacion(Acceso destino, Operador operador, Expresion valor, int linea, int columna) {
        super(linea, columna);
        this.destino = destino;
        this.operador = operador;
        this.valor = valor;
    }

    public Acceso getDestino()    { return destino; }
    public Operador getOperador() { return operador; }
    public Expresion getValor()   { return valor; }
    public boolean esCompuesta()  { return operador != null; }

    @Override
    public String getEtiqueta() {
        return operador == null ? "=" : operador.getSimbolo() + "=";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(destino, valor);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarAsignacion(this);
    }
}
