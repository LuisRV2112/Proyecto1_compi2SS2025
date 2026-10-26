package com.usac.contacto3d.simbolos;

import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.declaraciones.Visibilidad;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimboloEstructura extends Simbolo {

    public static class Campo {
        private final String nombre;
        private final Tipo tipo;
        private final int offset;
        private final int tamanio;
        private final Visibilidad visibilidad;
        private final SimboloEstructura duenio;

        public Campo(String nombre, Tipo tipo, int offset, int tamanio,
                     Visibilidad visibilidad, SimboloEstructura duenio) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.offset = offset;
            this.tamanio = tamanio;
            this.visibilidad = visibilidad;
            this.duenio = duenio;
        }

        public String getNombre()            { return nombre; }
        public Tipo getTipo()                { return tipo; }
        public int getOffset()               { return offset; }
        public int getTamanio()              { return tamanio; }
        public Visibilidad getVisibilidad()  { return visibilidad; }
        /** La clase o estructura que declaro el campo; en un campo heredado, el padre. */
        public SimboloEstructura getDuenio() { return duenio; }
    }

    /**
     * En un objeto, la celda 0 guarda el puntero a la tabla de metodos virtuales de su clase real,
     * y los campos empiezan en la 1. Las estructuras de Y? no la tienen: sus campos empiezan en 0.
     */
    public static final int CELDA_VTABLE = 0;

    private final Map<String, Campo> campos = new LinkedHashMap<>();
    private final List<SimboloFuncion> metodos = new ArrayList<>();
    private final List<SimboloFuncion> constructores = new ArrayList<>();
    private final boolean esClase;

    private int tamanioTotal = 0;
    private SimboloEstructura padre;          // null si no hereda

    public SimboloEstructura(String nombre, boolean esClase,
                             String archivo, int linea, int columna) {
        super(nombre,
              esClase ? Tipo.objeto(nombre) : Tipo.estructura(nombre),
              archivo, linea, columna);
        this.esClase = esClase;
        this.tamanioTotal = esClase ? CELDA_VTABLE + 1 : 0;
    }

    public boolean esClase() { return esClase; }

    public SimboloEstructura getPadre()           { return padre; }
    public void setPadre(SimboloEstructura padre) { this.padre = padre; }

    /**
     * Copia los campos del padre con sus mismos offsets, para que un objeto de esta clase sea
     * tambien un objeto valido del padre en memoria. Se llama antes de agregar los campos propios.
     */
    public void heredarCampos() {
        if (padre == null) {
            return;
        }
        for (Campo campo : padre.getCampos().values()) {
            campos.put(campo.getNombre(), campo);
        }
        tamanioTotal = padre.getTamanio();
    }

    /** Verdadero si esta clase es {@code otra} o hereda de ella, directa o indirectamente. */
    public boolean esSubclaseDe(SimboloEstructura otra) {
        for (SimboloEstructura actual = this; actual != null; actual = actual.padre) {
            if (actual == otra) {
                return true;
            }
        }
        return false;
    }

    public boolean agregarCampo(String nombre, Tipo tipo, int tamanio) {
        return agregarCampo(nombre, tipo, tamanio, Visibilidad.PUBLICO);
    }

    public boolean agregarCampo(String nombre, Tipo tipo, int tamanio, Visibilidad visibilidad) {
        if (campos.containsKey(nombre)) {
            return false;
        }
        campos.put(nombre, new Campo(nombre, tipo, tamanioTotal, tamanio, visibilidad, this));
        tamanioTotal += tamanio;
        return true;
    }

    public Campo getCampo(String nombre)   { return campos.get(nombre); }
    public boolean tieneCampo(String n)    { return campos.containsKey(n); }
    public Map<String, Campo> getCampos()  { return campos; }

    @Override
    public int getTamanio() { return tamanioTotal; }

    public void agregarMetodo(SimboloFuncion metodo)       { metodos.add(metodo); }
    public void agregarConstructor(SimboloFuncion c)       { constructores.add(c); }
    public List<SimboloFuncion> getMetodos()               { return metodos; }
    public List<SimboloFuncion> getConstructores()         { return constructores; }

    public SimboloFuncion buscarMetodo(String nombre, int cantidadArgumentos) {
        for (SimboloFuncion metodo : metodos) {
            if (metodo.getNombre().equals(nombre)
                    && metodo.getCantidadParametros() == cantidadArgumentos) {
                return metodo;
            }
        }
        return null;
    }

    /**
     * Los metodos con ese nombre visibles en esta clase: los propios y los heredados, del mas
     * cercano al mas lejano. Un metodo sobrescrito oculta al del padre con la misma firma.
     */
    public List<SimboloFuncion> metodosEnCadena(String nombre) {
        List<SimboloFuncion> encontrados = new ArrayList<>();
        java.util.Set<String> firmas = new java.util.HashSet<>();
        for (SimboloEstructura actual = this; actual != null; actual = actual.padre) {
            for (SimboloFuncion metodo : actual.metodos) {
                if (metodo.getNombre().equals(nombre) && firmas.add(metodo.getFirma())) {
                    encontrados.add(metodo);
                }
            }
        }
        return encontrados;
    }

    /** El metodo con esa firma (nombre y tipos de parametros) en esta clase o sus ancestros. */
    public SimboloFuncion buscarPorFirma(String firma) {
        for (SimboloEstructura actual = this; actual != null; actual = actual.padre) {
            for (SimboloFuncion metodo : actual.metodos) {
                if (metodo.getFirma().equals(firma)) {
                    return metodo;
                }
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
            if (padre != null) {
                base += ", hereda de " + padre.getNombre();
            }
        }
        return base + ": " + String.join(", ", campos.keySet());
    }
}
