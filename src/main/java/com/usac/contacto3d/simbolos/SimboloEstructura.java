package com.usac.contacto3d.simbolos;

import com.usac.contacto3d.ast.Tipo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Definicion de un tipo del usuario: 'estructura' de Y? o 'class' de Zetariano.
 *
 * Los campos se APLANAN: cada uno recibe un offset relativo al inicio de la
 * estructura, y el tamanio total es la suma. Asi el generador de C3D accede a
 * un atributo con una sola suma de direcciones (base + offset) en vez de
 * navegar punteros.
 *
 * Las estructuras de Y? viven en el stack; los objetos de Zetariano en el heap,
 * pero el calculo de offsets es el mismo.
 */
public class SimboloEstructura extends Simbolo {

    /** Un campo con su posicion relativa dentro de la estructura. */
    public static class Campo {
        private final String nombre;
        private final Tipo tipo;
        private final int offset;
        private final int tamanio;

        public Campo(String nombre, Tipo tipo, int offset, int tamanio) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.offset = offset;
            this.tamanio = tamanio;
        }

        public String getNombre() { return nombre; }
        public Tipo getTipo()     { return tipo; }
        public int getOffset()    { return offset; }
        public int getTamanio()   { return tamanio; }
    }

    private final Map<String, Campo> campos = new LinkedHashMap<>();
    private final List<SimboloFuncion> metodos = new ArrayList<>();
    private final List<SimboloFuncion> constructores = new ArrayList<>();
    private final boolean esClase;

    private int tamanioTotal = 0;

    public SimboloEstructura(String nombre, boolean esClase,
                             String archivo, int linea, int columna) {
        super(nombre,
              esClase ? Tipo.objeto(nombre) : Tipo.estructura(nombre),
              archivo, linea, columna);
        this.esClase = esClase;
    }

    public boolean esClase() { return esClase; }

    /**
     * Agrega un campo calculando su offset.
     * @return false si el nombre ya existia (hay que reportarlo como error)
     */
    public boolean agregarCampo(String nombre, Tipo tipo, int tamanio) {
        if (campos.containsKey(nombre)) {
            return false;
        }
        campos.put(nombre, new Campo(nombre, tipo, tamanioTotal, tamanio));
        tamanioTotal += tamanio;
        return true;
    }

    public Campo getCampo(String nombre)   { return campos.get(nombre); }
    public boolean tieneCampo(String n)    { return campos.containsKey(n); }
    public Map<String, Campo> getCampos()  { return campos; }

    @Override
    public int getTamanio() { return tamanioTotal; }

    /* --------- Metodos y constructores (solo para clases de Zetariano) --------- */

    public void agregarMetodo(SimboloFuncion metodo)       { metodos.add(metodo); }
    public void agregarConstructor(SimboloFuncion c)       { constructores.add(c); }
    public List<SimboloFuncion> getMetodos()               { return metodos; }
    public List<SimboloFuncion> getConstructores()         { return constructores; }

    /** Busca un metodo por nombre y cantidad de argumentos (soporta sobrecarga). */
    public SimboloFuncion buscarMetodo(String nombre, int cantidadArgumentos) {
        for (SimboloFuncion metodo : metodos) {
            if (metodo.getNombre().equals(nombre)
                    && metodo.getCantidadParametros() == cantidadArgumentos) {
                return metodo;
            }
        }
        return null;
    }

    public SimboloFuncion buscarConstructor(int cantidadArgumentos) {
        for (SimboloFuncion constructor : constructores) {
            if (constructor.getCantidadParametros() == cantidadArgumentos) {
                return constructor;
            }
        }
        return null;
    }

    @Override
    public Categoria getCategoria() {
        return esClase ? Categoria.CLASE : Categoria.ESTRUCTURA;
    }

    @Override
    public String getDetalle() {
        String base = campos.size() + " campo(s), " + tamanioTotal + " celdas";
        if (esClase) {
            base += ", " + metodos.size() + " metodo(s)";
        }
        return base + ": " + String.join(", ", campos.keySet());
    }
}
