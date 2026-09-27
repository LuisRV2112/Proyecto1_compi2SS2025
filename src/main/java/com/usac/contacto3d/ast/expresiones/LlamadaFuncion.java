package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.instrucciones.Instruccion;
import com.usac.contacto3d.simbolos.SimboloFuncion;

import java.util.List;

public class LlamadaFuncion extends Expresion implements Instruccion {

    private final String nombre;
    private final List<Expresion> argumentos;

    public LlamadaFuncion(String nombre, List<Expresion> argumentos, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.argumentos = List.copyOf(argumentos);
    }

    public String getNombre()              { return nombre; }
    public List<Expresion> getArgumentos() { return argumentos; }

    private SimboloFuncion funcion;

    public SimboloFuncion getFuncion() { return funcion; }
    public void setFuncion(SimboloFuncion funcion) { this.funcion = funcion; }

    @Override
    public String getEtiqueta() {
        return "Llamada " + nombre + "()";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(argumentos);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarLlamadaFuncion(this);
    }
}
