package com.usac.contacto3d.ast.instrucciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.expresiones.Expresion;

import java.util.List;

/**
 * imprimir(...) de Y?, println/print de Zetariano y >> de PigLatin.
 * Los valores se imprimen seguidos; saltoLinea es false solo en print.
 */
public class Imprimir extends Nodo implements Instruccion {

    private final List<Expresion> valores;
    private final boolean saltoLinea;

    public Imprimir(List<Expresion> valores, boolean saltoLinea, int linea, int columna) {
        super(linea, columna);
        this.valores = List.copyOf(valores);
        this.saltoLinea = saltoLinea;
    }

    public List<Expresion> getValores() { return valores; }
    public boolean conSaltoLinea()      { return saltoLinea; }

    @Override
    public String getEtiqueta() {
        return saltoLinea ? "Imprimir" : "Imprimir (sin salto)";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(valores);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarImprimir(this);
    }
}
