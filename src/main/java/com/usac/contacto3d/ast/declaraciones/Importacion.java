package com.usac.contacto3d.ast.declaraciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;

import java.util.List;

public class Importacion extends Nodo {

    private final List<String> partes;

    public Importacion(List<String> partes, int linea, int columna) {
        super(linea, columna);
        this.partes = List.copyOf(partes);
    }

    public List<String> getPartes() { return partes; }

    public String getExtension() {
        return partes.get(partes.size() - 1);
    }

    public String getNombreArchivo() {
        return partes.get(partes.size() - 2) + "." + getExtension();
    }

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
