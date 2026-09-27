package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Tipo;

public abstract class Expresion extends Nodo {

    private Tipo tipo;

    protected Expresion(int linea, int columna) {
        super(linea, columna);
    }

    public Tipo getTipo()          { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
}
