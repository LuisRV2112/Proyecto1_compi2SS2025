package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Tipo;

/** Base de todo lo que produce un valor. */
public abstract class Expresion extends Nodo {

    /** Lo asigna el semantico; el generador de cuartetas lo consulta despues. */
    private Tipo tipo;

    protected Expresion(int linea, int columna) {
        super(linea, columna);
    }

    public Tipo getTipo()          { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
}
