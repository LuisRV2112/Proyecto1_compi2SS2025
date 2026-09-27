package com.usac.contacto3d.ast;

import com.usac.contacto3d.ast.declaraciones.DeclaracionClase;
import com.usac.contacto3d.ast.declaraciones.DeclaracionEstructura;
import com.usac.contacto3d.ast.declaraciones.Funcion;
import com.usac.contacto3d.ast.declaraciones.Importacion;
import com.usac.contacto3d.ast.instrucciones.Instruccion;

import java.util.List;

public class Programa extends Nodo {

    private final Lenguaje lenguaje;
    private final List<Importacion> importaciones;
    private final List<DeclaracionEstructura> estructuras;
    private final List<DeclaracionClase> clases;
    private final List<Funcion> funciones;
    private final List<Instruccion> globales;
    private final List<Instruccion> principal;

    private Programa(Lenguaje lenguaje, List<Importacion> importaciones,
                     List<DeclaracionEstructura> estructuras, List<DeclaracionClase> clases,
                     List<Funcion> funciones, List<Instruccion> globales, List<Instruccion> principal,
                     int linea, int columna) {
        super(linea, columna);
        this.lenguaje = lenguaje;
        this.importaciones = List.copyOf(importaciones);
        this.estructuras = List.copyOf(estructuras);
        this.clases = List.copyOf(clases);
        this.funciones = List.copyOf(funciones);
        this.globales = List.copyOf(globales);
        this.principal = List.copyOf(principal);
    }

    public static Programa deY(List<DeclaracionEstructura> estructuras, List<Funcion> funciones,
                               int linea, int columna) {
        return new Programa(Lenguaje.Y, List.of(), estructuras, List.of(), funciones,
                List.of(), List.of(), linea, columna);
    }

    public static Programa deZetariano(DeclaracionClase clase, int linea, int columna) {
        return new Programa(Lenguaje.ZETARIANO, List.of(), List.of(), List.of(clase), List.of(),
                List.of(), List.of(), linea, columna);
    }

    public static Programa dePigLatin(List<Importacion> importaciones, List<Instruccion> globales,
                                      List<Funcion> funciones, List<Instruccion> principal,
                                      int linea, int columna) {
        return new Programa(Lenguaje.PIGLATIN, importaciones, List.of(), List.of(), funciones,
                globales, principal, linea, columna);
    }

    public Lenguaje getLenguaje()                     { return lenguaje; }
    public List<Importacion> getImportaciones()       { return importaciones; }
    public List<DeclaracionEstructura> getEstructuras() { return estructuras; }
    public List<DeclaracionClase> getClases()         { return clases; }
    public List<Funcion> getFunciones()               { return funciones; }
    public List<Instruccion> getGlobales()            { return globales; }
    public List<Instruccion> getPrincipal()           { return principal; }

    @Override
    public String getEtiqueta() {
        return "Programa " + lenguaje.getNombre() + " (" + archivo + ")";
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(importaciones, estructuras, clases, globales, funciones, principal);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarPrograma(this);
    }
}
