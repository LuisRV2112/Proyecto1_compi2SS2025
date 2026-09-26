package com.usac.contacto3d.ast.instrucciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;

import java.util.List;

/** Bloque suelto entre llaves de Zetariano: { ... }. Abre un ambito propio. */
public class Bloque extends Nodo implements Instruccion {

    private final List<Instruccion> instrucciones;

    public Bloque(List<Instruccion> instrucciones, int linea, int columna) {
        super(linea, columna);
        this.instrucciones = List.copyOf(instrucciones);
    }

    public List<Instruccion> getInstrucciones() { return instrucciones; }

    @Override
    public String getEtiqueta() {
        return "Bloque";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(instrucciones);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarBloque(this);
    }
}
