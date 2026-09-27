package com.usac.contacto3d.ast.declaraciones;

import com.usac.contacto3d.ast.Nodo;
import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.Visitante;
import com.usac.contacto3d.ast.instrucciones.Instruccion;
import com.usac.contacto3d.simbolos.SimboloFuncion;

import java.util.List;

public class Funcion extends Nodo {

    private final String nombre;
    private final List<Parametro> parametros;
    private Tipo tipoRetorno;
    private final List<Instruccion> cuerpo;

    public Funcion(String nombre, List<Parametro> parametros, Tipo tipoRetorno,
                   List<Instruccion> cuerpo, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.parametros = List.copyOf(parametros);
        this.tipoRetorno = tipoRetorno;
        this.cuerpo = List.copyOf(cuerpo);
    }

    public String getNombre()               { return nombre; }
    public List<Parametro> getParametros()  { return parametros; }
    public Tipo getTipoRetorno()            { return tipoRetorno; }
    public void setTipoRetorno(Tipo tipo)   { this.tipoRetorno = tipo; }
    public List<Instruccion> getCuerpo()    { return cuerpo; }

    public boolean sinRetorno() {
        return tipoRetorno == null || tipoRetorno.getBase() == Tipo.Base.VACIO;
    }

    private SimboloFuncion simbolo;

    public SimboloFuncion getSimbolo() { return simbolo; }
    public void setSimbolo(SimboloFuncion simbolo) { this.simbolo = simbolo; }

    @Override
    public String getEtiqueta() {
        return "Funcion " + nombre + " -> " + tipoRetorno;
    }

    @Override
    public List<Nodo> getHijos() {
        return hijos(parametros, cuerpo);
    }

    @Override
    public <T> T aceptar(Visitante<T> visitante) {
        return visitante.visitarFuncion(this);
    }
}
