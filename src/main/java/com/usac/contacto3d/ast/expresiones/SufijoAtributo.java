package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.simbolos.SimboloEstructura;

import java.util.List;

/** .nombre — campo de una estructura o atributo de un objeto. */
public class SufijoAtributo extends Sufijo {

    private final String nombre;

    public SufijoAtributo(String nombre, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }

    /** El campo con su offset. Lo resuelve el semantico; el generador lo usa en vez de volver a buscar el nombre. */
    private SimboloEstructura.Campo campo;

    public SimboloEstructura.Campo getCampo() { return campo; }
    public void setCampo(SimboloEstructura.Campo campo) { this.campo = campo; }

    @Override
    public String getEtiqueta() {
        return "." + nombre;
    }

    @Override
    public List<Nodo> getHijos() {
        return List.of();
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarSufijoAtributo(this);
    }
}
