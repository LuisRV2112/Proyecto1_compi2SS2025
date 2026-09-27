package com.usac.contacto3d.errores;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ListaErrores {

    private final List<ErrorCompilacion> errores = new ArrayList<>();
    private String archivoActual = "";

    public void setArchivoActual(String archivo) {
        this.archivoActual = archivo == null ? "" : archivo;
    }

    public String getArchivoActual() {
        return archivoActual;
    }

    public void agregar(ErrorCompilacion error) {
        errores.add(error);
    }

    public void agregar(TipoError tipo, String lexema, String descripcion, int linea, int columna) {
        errores.add(new ErrorCompilacion(tipo, archivoActual, lexema, descripcion, linea, columna));
    }

    public void semantico(String lexema, String descripcion, int linea, int columna) {
        agregar(TipoError.SEMANTICO, lexema, descripcion, linea, columna);
    }

    public List<ErrorCompilacion> getErrores() {
        return errores;
    }

    public List<ErrorCompilacion> getPorTipo(TipoError tipo) {
        return errores.stream().filter(e -> e.getTipo() == tipo).toList();
    }

    public boolean hayErrores() {
        return !errores.isEmpty();
    }

    public boolean parseoExitoso() {
        return errores.stream()
                .noneMatch(e -> e.getTipo() == TipoError.LEXICO || e.getTipo() == TipoError.SINTACTICO);
    }

    public int cantidad() {
        return errores.size();
    }

    public void limpiar() {
        errores.clear();
    }

    public void ordenar() {
        errores.sort(Comparator
                .comparing(ErrorCompilacion::getArchivo)
                .thenComparingInt(ErrorCompilacion::getLinea)
                .thenComparingInt(ErrorCompilacion::getColumna));
    }
}
