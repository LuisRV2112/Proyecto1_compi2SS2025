package com.usac.contacto3d.ast.declaraciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.expresiones.Expresion;
import com.usac.contacto3d.ast.instrucciones.Instruccion;
import com.usac.contacto3d.simbolos.SimboloVariable;

import java.util.List;

/**
 * Variable simple, estructura u objeto, con o sin valor inicial.
 *
 *   entero x = 5                 Y?
 *   Persona p = new Persona();   Zetariano (tambien los atributos de una clase)
 *   esto x : numerus 5;          PigLatin
 *
 * Los arreglos con dimension en la declaracion (entero a[10]) son DeclaracionArreglo.
 */
public class DeclaracionVariable extends Nodo implements Instruccion {

    private final String nombre;
    /** No es final: el semantico cambia ESTRUCTURA por OBJETO cuando el nombre resulta ser una clase. */
    private Tipo tipo;
    private final Expresion valor;
    /**
     * True en la forma rapida de PigLatin "esto x : verum;", donde el tipo sale
     * del literal. Si es valida o no lo decide el semantico.
     */
    private final boolean tipoImplicito;

    public DeclaracionVariable(String nombre, Tipo tipo, Expresion valor, boolean tipoImplicito,
                               int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.tipo = tipo;
        this.valor = valor;
        this.tipoImplicito = tipoImplicito;
    }

    public DeclaracionVariable(String nombre, Tipo tipo, Expresion valor, int linea, int columna) {
        this(nombre, tipo, valor, false, linea, columna);
    }

    public String getNombre()        { return nombre; }
    public Tipo getTipo()            { return tipo; }
    public void setTipo(Tipo tipo)   { this.tipo = tipo; }
    public Expresion getValor()      { return valor; }
    public boolean tieneValor()      { return valor != null; }
    public boolean esTipoImplicito() { return tipoImplicito; }

    /** Lo resuelve el semantico; el generador lo usa en vez de volver a buscar el nombre. */
    private SimboloVariable simbolo;

    public SimboloVariable getSimbolo() { return simbolo; }
    public void setSimbolo(SimboloVariable simbolo) { this.simbolo = simbolo; }

    @Override
    public String getEtiqueta() {
        return "Declaracion " + nombre + " : " + tipo + (tipoImplicito ? " (implicito)" : "");
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(valor);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarDeclaracionVariable(this);
    }
}
