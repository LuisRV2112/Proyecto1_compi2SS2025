package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.instrucciones.Instruccion;

import java.util.List;

public class Leer extends Expresion implements Instruccion {

    private final Acceso destino;

    public Leer(Acceso destino, int linea, int columna) {
        super(linea, columna);
        this.destino = destino;
    }

    public Acceso getDestino()  { return destino; }
    public boolean tieneDestino() { return destino != null; }

    @Override
    public String getEtiqueta() {
        return "Leer";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(destino);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarLeer(this);
    }
}
