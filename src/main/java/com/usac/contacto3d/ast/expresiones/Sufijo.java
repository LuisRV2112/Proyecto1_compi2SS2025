package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Tipo;

/** Un eslabon de una cadena de accesos: .campo, [indice] o .metodo(args). */
public abstract class Sufijo extends Nodo {

    /** Tipo de lo que queda al aplicar este sufijo. Lo asigna el semantico. */
    private Tipo tipo;

    protected Sufijo(int linea, int columna) {
        super(linea, columna);
    }

    public Tipo getTipo()          { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }
}
