package com.usac.contacto3d.ast.declaraciones;

import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.instrucciones.Instruccion;

import java.util.List;

/** Constructor de una clase de Zetariano. Se puede sobrecargar. */
public class DeclaracionConstructor extends Funcion {

    public DeclaracionConstructor(String nombre, List<Parametro> parametros,
                                  List<Instruccion> cuerpo, int linea, int columna) {
        super(nombre, parametros, Tipo.VACIO, cuerpo, linea, columna);
    }

    @Override
    public String getEtiqueta() {
        return "Constructor " + getNombre();
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarDeclaracionConstructor(this);
    }
}
