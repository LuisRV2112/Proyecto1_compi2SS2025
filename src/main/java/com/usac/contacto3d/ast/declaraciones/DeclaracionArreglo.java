package com.usac.contacto3d.ast.declaraciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.expresiones.Expresion;
import com.usac.contacto3d.ast.instrucciones.Instruccion;
import com.usac.contacto3d.simbolos.SimboloVariable;

import java.util.List;

public class DeclaracionArreglo extends Nodo implements Instruccion {

    private final String nombre;
    private Tipo tipoElemento;
    private final List<Expresion> dimensiones;
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
