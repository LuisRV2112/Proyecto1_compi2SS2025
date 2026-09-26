package com.usac.contacto3d.errores;

/** Clasificacion de los errores que puede reportar el compilador. */
public enum TipoError {
    LEXICO("Lexico"),
    SINTACTICO("Sintactico"),
    SEMANTICO("Semantico");

    private final String descripcion;

    TipoError(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
