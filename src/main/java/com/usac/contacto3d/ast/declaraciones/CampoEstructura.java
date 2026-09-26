package com.usac.contacto3d.ast.declaraciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.Visitante;

import java.util.List;

/**
 * Campo de una estructura de Y?. Si es arreglo, el tipo ya trae las
 * dimensiones (siempre constantes dentro de una estructura).
 */
public class CampoEstructura extends Nodo {

    private final String nombre;
    private Tipo tipo;

    public CampoEstructura(String nombre, Tipo tipo, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public String getNombre()      { return nombre; }
    public Tipo getTipo()          { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    @Override
    public String getEtiqueta() {
        return "Campo " + nombre + " : " + tipo;
    }

    @Override
    public List<Nodo> getHijos() {
        return List.of();
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarCampoEstructura(this);
    }
}
