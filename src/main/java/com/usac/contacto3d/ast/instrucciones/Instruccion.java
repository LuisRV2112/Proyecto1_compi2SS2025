package com.usac.contacto3d.ast.instrucciones;

import com.usac.contacto3d.ast.Visitante;

/**
 * Marca los nodos que pueden ir en un cuerpo de instrucciones.
 *
 * Es interfaz y no clase porque hay nodos que son expresion e instruccion a
 * la vez: calcular(10) se usa por su valor en "y = calcular(10)" y por su
 * efecto en "calcular(10);". Java no tiene herencia multiple de clases, asi
 * que esos nodos extienden Expresion e implementan Instruccion.
 *
 * Los metodos ya los implementa Nodo; declararlos aqui permite recorrer una
 * List<Instruccion> sin castear.
 */
public interface Instruccion {

    <T> T aceptar(Visitante<T> visitante);

    int getLinea();

    int getColumna();

    String getArchivo();
}
