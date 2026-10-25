package com.usac.contacto3d.ast.declaraciones;

import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.instrucciones.Instruccion;

import java.util.List;

public class DeclaracionConstructor extends Funcion {

    private final Visibilidad visibilidad;

    public DeclaracionConstructor(String nombre, List<Parametro> parametros, List<Instruccion> cuerpo,
                                  Visibilidad visibilidad, int linea, int columna) {
        super(nombre, parametros, Tipo.VACIO, cuerpo, linea, columna);
        this.visibilidad = visibilidad;
    }

    public Visibilidad getVisibilidad() { return visibilidad; }

    @Override
    public String getEtiqueta() {
        return "Constructor " + visibilidad.prefijo() + getNombre();
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarDeclaracionConstructor(this);
    }
}
