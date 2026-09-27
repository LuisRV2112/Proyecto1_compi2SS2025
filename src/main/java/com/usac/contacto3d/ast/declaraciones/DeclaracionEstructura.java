package com.usac.contacto3d.ast.declaraciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.instrucciones.Instruccion;
import com.usac.contacto3d.simbolos.SimboloEstructura;

import java.util.List;

public class DeclaracionEstructura extends Nodo implements Instruccion {

    private final String nombre;
    private final List<CampoEstructura> campos;

    public DeclaracionEstructura(String nombre, List<CampoEstructura> campos, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.campos = List.copyOf(campos);
    }

    public String getNombre()               { return nombre; }
    public List<CampoEstructura> getCampos() { return campos; }

    private SimboloEstructura simbolo;

    public SimboloEstructura getSimbolo() { return simbolo; }
    public void setSimbolo(SimboloEstructura simbolo) { this.simbolo = simbolo; }

    @Override
    public String getEtiqueta() {
        return "Estructura " + nombre;
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(campos);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarDeclaracionEstructura(this);
    }
}
