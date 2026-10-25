package com.usac.contacto3d.errores;

public enum TipoError {
    LEXICO("Lexico"),
    SINTACTICO("Sintactico"),
    SEMANTICO("Semantico"),
    EJECUCION("Ejecucion");

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
