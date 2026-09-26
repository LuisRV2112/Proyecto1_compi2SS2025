package com.usac.contacto3d.simbolos;

import com.usac.contacto3d.ast.Tipo;

/**
 * Base de todo lo que se registra en la tabla de simbolos.
 *
 * NOVEDAD respecto a la practica: cada simbolo lleva una POSICION DE MEMORIA.
 * El codigo de tres direcciones no trabaja con nombres sino con direcciones
 * (stack[5], heap[12]), asi que el offset hay que asignarlo aqui, durante el
 * analisis semantico, antes de generar cuartetas.
 */
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

    /** Donde vive el valor en tiempo de ejecucion. */
    public enum Almacenamiento {
        STACK,      // variables locales, parametros, estructuras aplanadas
        HEAP,       // arreglos, cadenas y objetos
        GLOBAL,     // variables de la seccion VARIABILES> de PigLatin
        NINGUNO     // funciones, clases y tipos: no ocupan celdas
    }

    protected final String nombre;
    protected final Tipo tipo;
    protected final String archivo;
    protected final int linea;
    protected final int columna;

    protected String nombreAmbito = "global";
    protected int nivelAmbito = 0;

    /** Posicion en el stack o en el heap. -1 mientras no se le asigne. */
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

    /** Cuantas celdas ocupa. Las estructuras aplanadas ocupan mas de una. */
    public int getTamanio() {
        return 1;
    }

    /** Texto extra para la columna de detalle de la tabla. */
    public abstract String getDetalle();

    @Override
    public String toString() {
        return getCategoria().getDescripcion() + " " + nombre + " : " + tipo;
    }
}
