package com.usac.contacto3d.ast.instrucciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;

import java.util.List;

public class ControlCiclo extends Nodo implements Instruccion {

    public enum Accion { ROMPER, CONTINUAR }

    private final Accion accion;

    public ControlCiclo(Accion accion, int linea, int columna) {
        super(linea, columna);
        this.accion = accion;
    }

    public Accion getAccion()   { return accion; }
    public boolean esRomper()   { return accion == Accion.ROMPER; }

    @Override
    public String getEtiqueta() {
        return accion == Accion.ROMPER ? "Romper" : "Continuar";
    }

    @Override
    public List<Nodo> getHijos() {
        return List.of();
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarControlCiclo(this);
    }
}
