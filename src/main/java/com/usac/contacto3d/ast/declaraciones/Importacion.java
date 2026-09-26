package com.usac.contacto3d.ast.declaraciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;

import java.util.List;

/**
 * import carpeta.Objeto1.z de PigLatin. Se guardan las partes separadas por
 * punto; la ultima es la extension.
 */
public class Importacion extends Nodo {

    private final List<String> partes;

    public Importacion(List<String> partes, int linea, int columna) {
        super(linea, columna);
        this.partes = List.copyOf(partes);
    }

    public List<String> getPartes() { return partes; }

    /** "z" o "y" (o lo que se haya escrito: validarlo es del semantico). */
    public String getExtension() {
        return partes.get(partes.size() - 1);
    }

    /** Persona.z */
    public String getNombreArchivo() {
        return partes.get(partes.size() - 2) + "." + getExtension();
    }

    /** carpeta/Persona.z */
    public String getRutaRelativa() {
        return String.join("/", partes.subList(0, partes.size() - 2))
                + (partes.size() > 2 ? "/" : "") + getNombreArchivo();
    }

    @Override
    public String getEtiqueta() {
        return "Import " + String.join(".", partes);
    }

    @Override
    public List<Nodo> getHijos() {
        return List.of();
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarImportacion(this);
    }
}
