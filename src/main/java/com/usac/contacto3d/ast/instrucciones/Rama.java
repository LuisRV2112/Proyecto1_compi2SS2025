package com.usac.contacto3d.ast.instrucciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.expresiones.Expresion;

import java.util.List;

public class Rama extends Nodo {

    private final Expresion condicion;
    private final List<Instruccion> cuerpo;

    public Rama(Expresion condicion, List<Instruccion> cuerpo, int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.cuerpo = List.copyOf(cuerpo);
    }

    public Expresion getCondicion()      { return condicion; }
    public List<Instruccion> getCuerpo() { return cuerpo; }
    public boolean esPorDefecto()        { return condicion == null; }

    @Override
    public String getEtiqueta() {
        return esPorDefecto() ? "Rama por defecto" : "Rama";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(condicion, cuerpo);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarRama(this);
    }
}
