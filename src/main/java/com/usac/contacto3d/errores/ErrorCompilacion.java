package com.usac.contacto3d.errores;

/**
 * Un error detectado durante la compilacion.
 *
 * Respecto a la practica anterior se agrega el ARCHIVO: el proyecto maneja
 * varios archivos a la vez (.pig que importa .y y .z), asi que saber la linea
 * ya no alcanza, hay que saber en cual de todos.
 */
public class ErrorCompilacion {

    private final TipoError tipo;
    private final String archivo;
    private final String lexema;
    private final String descripcion;
    private final int linea;
    private final int columna;

    public ErrorCompilacion(TipoError tipo, String archivo, String lexema,
                            String descripcion, int linea, int columna) {
        this.tipo = tipo;
        this.archivo = archivo == null ? "" : archivo;
        this.lexema = lexema == null ? "" : lexema;
        this.descripcion = descripcion;
        this.linea = linea;
        this.columna = columna;
    }

    public TipoError getTipo()     { return tipo; }
    public String getArchivo()     { return archivo; }
    public String getLexema()      { return lexema; }
    public String getDescripcion() { return descripcion; }
    public int getLinea()          { return linea; }
    public int getColumna()        { return columna; }

    @Override
    public String toString() {
        return String.format("[%s] %s linea %d, columna %d -> %s  (lexema: '%s')",
                tipo.getDescripcion(), archivo, linea, columna, descripcion, lexema);
    }
}
