package com.usac.contacto3d.simbolos;

import com.usac.contacto3d.ast.Tipo;

public abstract class Simbolo {

    public enum Categoria {
        VARIABLE("Variable"),
        PARAMETRO("Parametro"),
        ARREGLO("Arreglo"),
        FUNCION("Funcion"),
        ESTRUCTURA("Estructura"),
        CLASE("Clase"),
        ATRIBUTO("Atributo"),
        METODO("Metodo");

        private final String descripcion;

        Categoria(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    public enum Almacenamiento {
        STACK,
        HEAP,
        GLOBAL,
        NINGUNO
    }

    protected final String nombre;
    protected final Tipo tipo;
    protected final String archivo;
    protected final int linea;
    protected final int columna;

    protected String nombreAmbito = "global";
    protected int nivelAmbito = 0;

    protected int posicion = -1;
    protected Almacenamiento almacenamiento = Almacenamiento.NINGUNO;

    protected Simbolo(String nombre, Tipo tipo, String archivo, int linea, int columna) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.archivo = archivo == null ? "" : archivo;
        this.linea = linea;
        this.columna = columna;
    }

    public String getNombre()         { return nombre; }
    public Tipo getTipo()             { return tipo; }
    public String getArchivo()        { return archivo; }
    public int getLinea()             { return linea; }
    public int getColumna()           { return columna; }
    public String getNombreAmbito()   { return nombreAmbito; }
    public int getNivelAmbito()       { return nivelAmbito; }
    public int getPosicion()          { return posicion; }
    public Almacenamiento getAlmacenamiento() { return almacenamiento; }

    public void ubicarEnMemoria(Almacenamiento donde, int posicion) {
        this.almacenamiento = donde;
        this.posicion = posicion;
    }

    void ubicarEnAmbito(String nombreAmbito, int nivelAmbito) {
        this.nombreAmbito = nombreAmbito;
        this.nivelAmbito = nivelAmbito;
    }

    public abstract Categoria getCategoria();

    public int getTamanio() {
        return 1;
    }

    public abstract String getDetalle();

    @Override
    public String toString() {
        return getCategoria().getDescripcion() + " " + nombre + " : " + tipo;
    }
}
