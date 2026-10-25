package com.usac.contacto3d.ast.declaraciones;

import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.instrucciones.Instruccion;

import java.util.List;

public class DeclaracionMetodo extends Funcion {

    private final Visibilidad visibilidad;
    private final boolean tieneOverride;

    public DeclaracionMetodo(String nombre, List<Parametro> parametros, Tipo tipoRetorno,
                             List<Instruccion> cuerpo, Visibilidad visibilidad, boolean tieneOverride,
                             int linea, int columna) {
        super(nombre, parametros, tipoRetorno, cuerpo, linea, columna);
        this.visibilidad = visibilidad;
        this.tieneOverride = tieneOverride;
    }

    public Visibilidad getVisibilidad() { return visibilidad; }
    public boolean tieneOverride()      { return tieneOverride; }

    @Override
    public String getEtiqueta() {
        return (tieneOverride ? "@Override " : "") + "Metodo " + visibilidad.prefijo()
                + getNombre() + " -> " + getTipoRetorno();
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarDeclaracionMetodo(this);
    }
}
