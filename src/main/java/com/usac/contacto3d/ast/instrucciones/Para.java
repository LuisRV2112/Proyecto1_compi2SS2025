package com.usac.contacto3d.ast.instrucciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.expresiones.Expresion;

import java.util.List;

public class Para extends Nodo implements Instruccion {

    private final List<Instruccion> inicio;
    private final Expresion condicion;
    private final List<Instruccion> actualizacion;
    private final List<Instruccion> cuerpo;

    public Para(List<Instruccion> inicio, Expresion condicion, List<Instruccion> actualizacion,
                List<Instruccion> cuerpo, int linea, int columna) {
        super(linea, columna);
        this.inicio = List.copyOf(inicio);
        this.condicion = condicion;
        this.actualizacion = List.copyOf(actualizacion);
        this.cuerpo = List.copyOf(cuerpo);
    }

    public List<Instruccion> getInicio()        { return inicio; }
    public Expresion getCondicion()             { return condicion; }
    public List<Instruccion> getActualizacion() { return actualizacion; }
    public List<Instruccion> getCuerpo()        { return cuerpo; }

    @Override
    public String getEtiqueta() {
        return "Para";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(inicio, condicion, actualizacion, cuerpo);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarPara(this);
    }
}
