package com.usac.contacto3d.simbolos;

import com.usac.contacto3d.ast.Tipo;

public class SimboloVariable extends Simbolo {

    private final Categoria categoria;
    private boolean inicializada;
    private int tamanio = 1;

    private boolean porReferencia;

    public SimboloVariable(String nombre, Tipo tipo, Categoria categoria,
                           boolean inicializada, String archivo, int linea, int columna) {
        super(nombre, tipo, archivo, linea, columna);
        this.categoria = categoria;
        this.inicializada = inicializada;
        this.porReferencia = tipo != null && tipo.viveEnHeap();
    }

    public static SimboloVariable variable(String nombre, Tipo tipo, boolean inicializada,
                                           String archivo, int linea, int columna) {
        return new SimboloVariable(nombre, tipo, Categoria.VARIABLE, inicializada,
                archivo, linea, columna);
    }

    public static SimboloVariable parametro(String nombre, Tipo tipo,
                                            String archivo, int linea, int columna) {
        return new SimboloVariable(nombre, tipo, Categoria.PARAMETRO, true,
                archivo, linea, columna);
    }

    public static SimboloVariable arreglo(String nombre, Tipo tipo, boolean inicializado,
                                          String archivo, int linea, int columna) {
        return new SimboloVariable(nombre, tipo, Categoria.ARREGLO, inicializado,
                archivo, linea, columna);
    }

    public boolean estaInicializada()  { return inicializada; }
    public void marcarInicializada()   { this.inicializada = true; }
    public boolean esPorReferencia()   { return porReferencia; }
    public void setPorReferencia(boolean valor) { this.porReferencia = valor; }
    public void setTamanio(int tamanio) { this.tamanio = tamanio; }

    @Override
    public int getTamanio() { return tamanio; }

    @Override
    public Categoria getCategoria() { return categoria; }

    @Override
    public String getDetalle() {
        StringBuilder sb = new StringBuilder();
        sb.append(almacenamiento).append('[').append(posicion).append(']');
        if (tamanio > 1) {
            sb.append(", ").append(tamanio).append(" celdas");
        }
        if (porReferencia) {
            sb.append(", por referencia");
        }
        if (!inicializada) {
            sb.append(", sin inicializar");
        }
        return sb.toString();
    }
}
