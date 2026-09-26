package com.usac.contacto3d.ast.declaraciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.expresiones.Expresion;
import com.usac.contacto3d.ast.instrucciones.Instruccion;
import com.usac.contacto3d.simbolos.SimboloVariable;

import java.util.List;

/**
 * Arreglo que recibe sus dimensiones al declararse, de una o mas dimensiones:
 *
 *   entero matriz[3][2] = { {1, 2}, {3, 4}, {5, 6} }     Y?
 *   series matriz[2][3] : numerus { {3, 2, 1}, {4, 5, 6} } PigLatin
 *
 * En Zetariano las dimensiones van en el new (int[][] m = new int[3][3]), asi
 * que ahi es una DeclaracionVariable de tipo arreglo con un NuevoArreglo.
 */
public class DeclaracionArreglo extends Nodo implements Instruccion {

    private final String nombre;
    private Tipo tipoElemento;
    private final List<Expresion> dimensiones;
    /** LiteralLista con los valores iniciales, o null. */
    private final Expresion valor;

    public DeclaracionArreglo(String nombre, Tipo tipoElemento, List<Expresion> dimensiones,
                              Expresion valor, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.tipoElemento = tipoElemento;
        this.dimensiones = List.copyOf(dimensiones);
        this.valor = valor;
    }

    public String getNombre()                   { return nombre; }
    public Tipo getTipoElemento()               { return tipoElemento; }
    public void setTipoElemento(Tipo tipo)      { this.tipoElemento = tipo; }
    public List<Expresion> getDimensiones()     { return dimensiones; }
    public Expresion getValor()                 { return valor; }
    public boolean tieneValor()                 { return valor != null; }

    /** Lo resuelve el semantico; el generador lo usa en vez de volver a buscar el nombre. */
    private SimboloVariable simbolo;

    public SimboloVariable getSimbolo() { return simbolo; }
    public void setSimbolo(SimboloVariable simbolo) { this.simbolo = simbolo; }

    @Override
    public String getEtiqueta() {
        return "Arreglo " + nombre + " : " + tipoElemento + "[]".repeat(dimensiones.size());
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(dimensiones, valor);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarDeclaracionArreglo(this);
    }
}
