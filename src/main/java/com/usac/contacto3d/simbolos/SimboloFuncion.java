package com.usac.contacto3d.simbolos;

import com.usac.contacto3d.ast.Tipo;

import java.util.ArrayList;
import java.util.List;

public class SimboloFuncion extends Simbolo {

    public static final int CELDA_RETORNO = 0;
    public static final int CELDA_THIS = 1;

    private final List<SimboloVariable> parametros;
    private final boolean esMetodo;
    private final String claseDuenia;

    private int tamanioMarco = 0;
    private String etiqueta;

    public SimboloFuncion(String nombre, Tipo tipoRetorno, List<SimboloVariable> parametros,
                          boolean esMetodo, String claseDuenia,
                          String archivo, int linea, int columna) {
        super(nombre, tipoRetorno, archivo, linea, columna);
        this.parametros = parametros == null ? new ArrayList<>() : parametros;
        this.esMetodo = esMetodo;
        this.claseDuenia = claseDuenia;
    }

    public List<SimboloVariable> getParametros() { return parametros; }
    public Tipo getTipoRetorno()                 { return tipo; }
    public int getCantidadParametros()           { return parametros.size(); }
    public boolean esMetodo()                    { return esMetodo; }
    public String getClaseDuenia()               { return claseDuenia; }

    public int getTamanioMarco()                 { return tamanioMarco; }
    public void setTamanioMarco(int celdas)      { this.tamanioMarco = celdas; }
    public String getEtiqueta()                  { return etiqueta; }
    public void setEtiqueta(String etiqueta)     { this.etiqueta = etiqueta; }

    public boolean sinRetorno() {
        return tipo == null || tipo.getBase() == Tipo.Base.VACIO;
    }

    public String getFirma() {
        StringBuilder sb = new StringBuilder(nombre).append('(');
        for (int i = 0; i < parametros.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(parametros.get(i).getTipo());
        }
        return sb.append(')').toString();
    }

    @Override
    public Categoria getCategoria() {
        return esMetodo ? Categoria.METODO : Categoria.FUNCION;
    }

    @Override
    public String getDetalle() {
        if (parametros.isEmpty()) {
            return "sin parametros, marco " + tamanioMarco;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parametros.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parametros.get(i).getNombre()).append(" : ")
              .append(parametros.get(i).getTipo());
        }
        return sb.append("  |  marco ").append(tamanioMarco).toString();
    }
}
