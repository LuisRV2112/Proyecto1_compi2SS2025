package com.usac.contacto3d.ast.instrucciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.expresiones.Expresion;

import java.util.List;

public class Retorno extends Nodo implements Instruccion {

    private final Expresion valor;

    public Retorno(Expresion valor, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
    }

    public Expresion getValor() { return valor; }

    @Override
    public String getEtiqueta() {
        return "Retorno";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(valor);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarRetorno(this);
    }
}
