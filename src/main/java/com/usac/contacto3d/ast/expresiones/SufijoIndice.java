package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;

import java.util.List;

/** [indice]. Una matriz m[i][j] son dos sufijos seguidos. */
public class SufijoIndice extends Sufijo {

    private final Expresion indice;

    public SufijoIndice(Expresion indice, int linea, int columna) {
        super(linea, columna);
        this.indice = indice;
    }

    public Expresion getIndice() { return indice; }

    @Override
    public String getEtiqueta() {
        return "[ ]";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(indice);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarSufijoIndice(this);
    }
}
