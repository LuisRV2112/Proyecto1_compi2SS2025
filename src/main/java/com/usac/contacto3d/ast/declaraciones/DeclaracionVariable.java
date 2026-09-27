package com.usac.contacto3d.ast.declaraciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.expresiones.Expresion;
import com.usac.contacto3d.ast.instrucciones.Instruccion;
import com.usac.contacto3d.simbolos.SimboloVariable;

import java.util.List;

public class DeclaracionVariable extends Nodo implements Instruccion {

    private final String nombre;
    private Tipo tipo;
    private final Expresion valor;
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
