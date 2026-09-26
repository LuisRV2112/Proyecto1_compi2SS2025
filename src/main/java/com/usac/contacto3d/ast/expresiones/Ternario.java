package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;

import java.util.List;

/** condicion ? siVerdadero : siFalso — solo Zetariano. */
public class Ternario extends Expresion {

    private final Expresion condicion;
    private final Expresion siVerdadero;
    private final Expresion siFalso;

    public Ternario(Expresion condicion, Expresion siVerdadero, Expresion siFalso,
                    int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.siVerdadero = siVerdadero;
        this.siFalso = siFalso;
    }

    public Expresion getCondicion()   { return condicion; }
    public Expresion getSiVerdadero() { return siVerdadero; }
    public Expresion getSiFalso()     { return siFalso; }

    @Override
    public String getEtiqueta() {
        return "? :";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(condicion, siVerdadero, siFalso);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarTernario(this);
    }
}
