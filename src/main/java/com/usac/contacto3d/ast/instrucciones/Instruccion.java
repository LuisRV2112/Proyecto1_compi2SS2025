package com.usac.contacto3d.ast.instrucciones;

import com.usac.contacto3d.ast.Visitante;

public interface Instruccion {

    <T> T aceptar(Visitante<T> visitante);

    int getLinea();

    int getColumna();

    String getArchivo();
}
