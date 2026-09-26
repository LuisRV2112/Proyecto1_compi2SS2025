package com.usac.contacto3d.ast.instrucciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.expresiones.Expresion;

import java.util.List;

/** Ciclo con la condicion al inicio: mientras de Y?, while de Zetariano y dum de PigLatin. */
public class Mientras extends Nodo implements Instruccion {

    private final Expresion condicion;
    private final List<Instruccion> cuerpo;

    public Mientras(Expresion condicion, List<Instruccion> cuerpo, int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.cuerpo = List.copyOf(cuerpo);
    }

    public Expresion getCondicion()      { return condicion; }
    public List<Instruccion> getCuerpo() { return cuerpo; }

    @Override
    public String getEtiqueta() {
        return "Mientras";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(condicion, cuerpo);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarMientras(this);
    }
}
