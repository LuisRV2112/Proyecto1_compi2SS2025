package com.usac.contacto3d.ast.declaraciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.simbolos.SimboloVariable;

import java.util.List;

public class Parametro extends Nodo {

    private final String nombre;
    private Tipo tipo;
    /**
     * Y? lo marca en la sintaxis ([] entero a, {} Persona p); en Zetariano los
     * arreglos y objetos siempre pasan por referencia, como en Java.
     */
    private final boolean porReferencia;

    public Parametro(String nombre, Tipo tipo, boolean porReferencia, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.tipo = tipo;
        this.porReferencia = porReferencia;
    }

    public String getNombre()        { return nombre; }
    public Tipo getTipo()            { return tipo; }
    public void setTipo(Tipo tipo)   { this.tipo = tipo; }
    public boolean esPorReferencia() { return porReferencia; }

    /** Lo resuelve el semantico; el generador lo usa en vez de volver a buscar el nombre. */
    private SimboloVariable simbolo;

    public SimboloVariable getSimbolo() { return simbolo; }
    public void setSimbolo(SimboloVariable simbolo) { this.simbolo = simbolo; }

    @Override
    public String getEtiqueta() {
        return "Parametro " + nombre + " : " + tipo + (porReferencia ? " (referencia)" : "");
    }

    @Override
    public List<Nodo> getHijos() {
        return List.of();
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarParametro(this);
    }
}
