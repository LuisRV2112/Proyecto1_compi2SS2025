package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.Visitante;

import java.util.List;

public class NuevoArreglo extends Expresion {

    private Tipo tipoElemento;
    private final List<Expresion> dimensiones;

    public NuevoArreglo(Tipo tipoElemento, List<Expresion> dimensiones, int linea, int columna) {
        super(linea, columna);
        this.tipoElemento = tipoElemento;
        this.dimensiones = List.copyOf(dimensiones);
    }

    public Tipo getTipoElemento()           { return tipoElemento; }
    public void setTipoElemento(Tipo tipo)  { this.tipoElemento = tipo; }
    public List<Expresion> getDimensiones() { return dimensiones; }

    @Override
    public String getEtiqueta() {
        return "Nuevo " + tipoElemento + "[]".repeat(dimensiones.size());
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(dimensiones);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarNuevoArreglo(this);
    }
}
