package com.usac.contacto3d.ast.declaraciones;

import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.instrucciones.Instruccion;

import java.util.List;

/** Metodo de una clase de Zetariano. Se puede sobrecargar. */
public class DeclaracionMetodo extends Funcion {

    public DeclaracionMetodo(String nombre, List<Parametro> parametros, Tipo tipoRetorno,
                             List<Instruccion> cuerpo, int linea, int columna) {
        super(nombre, parametros, tipoRetorno, cuerpo, linea, columna);
    }

    @Override
    public String getEtiqueta() {
        return "Metodo " + getNombre() + " -> " + getTipoRetorno();
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarDeclaracionMetodo(this);
    }
}
