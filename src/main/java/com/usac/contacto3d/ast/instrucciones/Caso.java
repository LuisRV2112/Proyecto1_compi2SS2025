package com.usac.contacto3d.ast.instrucciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.expresiones.Expresion;

import java.util.List;

public class Caso extends Nodo {

    private final Expresion valor;
    private final List<Instruccion> cuerpo;

    public Caso(Expresion valor, List<Instruccion> cuerpo, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
        this.cuerpo = List.copyOf(cuerpo);
    }

    public Expresion getValor()          { return valor; }
    public List<Instruccion> getCuerpo() { return cuerpo; }
    public boolean esPorDefecto()        { return valor == null; }

    @Override
    public String getEtiqueta() {
        return esPorDefecto() ? "Caso por defecto" : "Caso";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(valor, cuerpo);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarCaso(this);
    }
}
