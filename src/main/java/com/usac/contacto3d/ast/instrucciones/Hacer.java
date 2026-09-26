package com.usac.contacto3d.ast.instrucciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.expresiones.Expresion;

import java.util.List;

/** Ciclo con la condicion al final: hacer de Y?, do-while de Zetariano y facere de PigLatin. */
public class Hacer extends Nodo implements Instruccion {

    private final List<Instruccion> cuerpo;
    private final Expresion condicion;

    public Hacer(List<Instruccion> cuerpo, Expresion condicion, int linea, int columna) {
        super(linea, columna);
        this.cuerpo = List.copyOf(cuerpo);
        this.condicion = condicion;
    }

    public List<Instruccion> getCuerpo() { return cuerpo; }
    public Expresion getCondicion()      { return condicion; }

    @Override
    public String getEtiqueta() {
        return "Hacer";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(cuerpo, condicion);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarHacer(this);
    }
}
