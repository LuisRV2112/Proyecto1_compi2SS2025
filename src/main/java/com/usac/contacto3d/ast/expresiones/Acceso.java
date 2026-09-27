package com.usac.contacto3d.ast.expresiones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.instrucciones.Instruccion;
import com.usac.contacto3d.simbolos.Simbolo;
import com.usac.contacto3d.simbolos.SimboloEstructura;

import java.util.List;

public class Acceso extends Expresion implements Instruccion {

    private final String nombre;
    private final boolean esThis;
    private final LlamadaFuncion llamada;
    private final List<Sufijo> sufijos;

    private Acceso(String nombre, boolean esThis, LlamadaFuncion llamada, List<Sufijo> sufijos,
                   int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.esThis = esThis;
        this.llamada = llamada;
        this.sufijos = List.copyOf(sufijos);
    }

    public static Acceso deVariable(String nombre, List<Sufijo> sufijos, int linea, int columna) {
        return new Acceso(nombre, false, null, sufijos, linea, columna);
    }

    public static Acceso deThis(List<Sufijo> sufijos, int linea, int columna) {
        return new Acceso(null, true, null, sufijos, linea, columna);
    }

    public static Acceso deLlamada(LlamadaFuncion llamada, List<Sufijo> sufijos, int linea, int columna) {
        return new Acceso(null, false, llamada, sufijos, linea, columna);
    }

    public String getNombre()          { return nombre; }
    public boolean esThis()            { return esThis; }
    public LlamadaFuncion getLlamada() { return llamada; }
    public List<Sufijo> getSufijos()   { return sufijos; }

    public boolean esSimple() {
        return nombre != null && sufijos.isEmpty();
    }

    private Simbolo simbolo;

    public Simbolo getSimbolo() { return simbolo; }
    public void setSimbolo(Simbolo simbolo) { this.simbolo = simbolo; }

    private SimboloEstructura.Campo campoImplicito;

    public SimboloEstructura.Campo getCampoImplicito() { return campoImplicito; }
    public void setCampoImplicito(SimboloEstructura.Campo campoImplicito) { this.campoImplicito = campoImplicito; }

    @Override
    public String getEtiqueta() {
        return "Acceso " + (esThis ? "this" : nombre != null ? nombre : "(llamada)");
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(llamada, sufijos);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarAcceso(this);
    }
}
