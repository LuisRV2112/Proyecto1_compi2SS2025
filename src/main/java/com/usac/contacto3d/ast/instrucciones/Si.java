package com.usac.contacto3d.ast.instrucciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;

import java.util.List;

public class Si extends Nodo implements Instruccion {

    private final List<Rama> ramas;

    public Si(List<Rama> ramas, int linea, int columna) {
        super(linea, columna);
        this.ramas = List.copyOf(ramas);
    }

    public List<Rama> getRamas() { return ramas; }

    public boolean tieneRamaPorDefecto() {
        return !ramas.isEmpty() && ramas.get(ramas.size() - 1).esPorDefecto();
    }

    @Override
    public String getEtiqueta() {
        return "Si";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(ramas);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarSi(this);
    }
}
