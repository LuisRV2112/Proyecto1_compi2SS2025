package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.instrucciones.Instruccion;

import java.util.List;

/**
 * x++, x--, y en Zetariano tambien ++x y --x. Como expresion, el postfijo vale
 * lo de antes de sumar y el prefijo lo de despues.
 */
public class IncrementoDecremento extends Expresion implements Instruccion {

    private final Acceso destino;
    private final boolean incremento;
    private final boolean prefijo;

    public IncrementoDecremento(Acceso destino, boolean incremento, boolean prefijo,
                                int linea, int columna) {
        super(linea, columna);
        this.destino = destino;
        this.incremento = incremento;
        this.prefijo = prefijo;
    }

    public Acceso getDestino()   { return destino; }
    public boolean esIncremento() { return incremento; }
    public boolean esPrefijo()    { return prefijo; }

    @Override
    public String getEtiqueta() {
        String simbolo = incremento ? "++" : "--";
        return prefijo ? simbolo + " (prefijo)" : simbolo + " (postfijo)";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(destino);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarIncrementoDecremento(this);
    }
}
