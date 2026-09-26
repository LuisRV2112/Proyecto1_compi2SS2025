package com.usac.contacto3d.simbolos;

import com.usac.contacto3d.ast.Tipo;

import java.util.ArrayList;
import java.util.List;

/**
 * Funcion global (.y) o metodo de una clase (.z).
 *
 * Guarda el TAMANIO DEL MARCO: cuantas celdas del stack necesita para sus
 * parametros y locales. El generador de C3D lo usa para mover el puntero del
 * stack al entrar y salir de la funcion, que es lo que permite la recursividad.
 */
public class SimboloFuncion extends Simbolo {

    /*
     * Disposicion del marco, la misma para toda funcion:
     *   [0]  valor de retorno (se reserva aunque la funcion no retorne nada,
     *        asi los parametros estan siempre en el mismo lugar)
     *   [1]  el objeto (this), solo en metodos y constructores
     *   ...  parametros en orden, despues las locales
     */
    public static final int CELDA_RETORNO = 0;
    public static final int CELDA_THIS = 1;

    private final List<SimboloVariable> parametros;
    private final boolean esMetodo;
    private final String claseDuenia;   // null si es funcion global

    private int tamanioMarco = 0;
    private String etiqueta;            // etiqueta del C3D, p. ej. "f_calcular"

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

    /**
     * Firma para permitir SOBRECARGA, que Zetariano exige.
     * Dos metodos con el mismo nombre se distinguen por los tipos de sus
     * parametros, asi que la clave de la tabla no puede ser solo el nombre.
     */
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
