package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.simbolos.SimboloFuncion;

import java.util.List;

public class SufijoMetodo extends Sufijo {

    private final String nombre;
    private final List<Expresion> argumentos;

    public SufijoMetodo(String nombre, List<Expresion> argumentos, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.argumentos = List.copyOf(argumentos);
    }

    public String getNombre()              { return nombre; }
    public List<Expresion> getArgumentos() { return argumentos; }

    private SimboloFuncion metodo;

    public SimboloFuncion getMetodo() { return metodo; }
    public void setMetodo(SimboloFuncion metodo) { this.metodo = metodo; }

    @Override
    public String getEtiqueta() {
        return "." + nombre + "()";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(argumentos);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarSufijoMetodo(this);
    }
}
