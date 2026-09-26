package com.usac.contacto3d.ast.declaraciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.simbolos.SimboloEstructura;

import java.util.List;

/** Clase de Zetariano. Sin herencia ni encapsulamiento: no se contemplan en este proyecto. */
public class DeclaracionClase extends Nodo {

    private final String nombre;
    private final List<DeclaracionVariable> atributos;
    private final List<DeclaracionConstructor> constructores;
    private final List<DeclaracionMetodo> metodos;

    public DeclaracionClase(String nombre, List<DeclaracionVariable> atributos,
                            List<DeclaracionConstructor> constructores, List<DeclaracionMetodo> metodos,
                            int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.atributos = List.copyOf(atributos);
        this.constructores = List.copyOf(constructores);
        this.metodos = List.copyOf(metodos);
    }

    public String getNombre()                           { return nombre; }
    public List<DeclaracionVariable> getAtributos()     { return atributos; }
    public List<DeclaracionConstructor> getConstructores() { return constructores; }
    public List<DeclaracionMetodo> getMetodos()         { return metodos; }

    /** Lo resuelve el semantico; el generador lo usa en vez de volver a buscar el nombre. Tiene los offsets. */
    private SimboloEstructura simbolo;

    public SimboloEstructura getSimbolo() { return simbolo; }
    public void setSimbolo(SimboloEstructura simbolo) { this.simbolo = simbolo; }

    @Override
    public String getEtiqueta() {
        return "Clase " + nombre;
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(atributos, constructores, metodos);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarDeclaracionClase(this);
    }
}
