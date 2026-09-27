package com.usac.contacto3d.simbolos;

import java.util.LinkedHashMap;
import java.util.Map;

public class Ambito {

    private final String nombre;
    private final Ambito padre;
    private final int nivel;
    private final Map<String, Simbolo> simbolos = new LinkedHashMap<>();

    private int siguientePosicion;

    public Ambito(String nombre, Ambito padre) {
        this.nombre = nombre;
        this.padre = padre;
        this.nivel = padre == null ? 0 : padre.nivel + 1;
        this.siguientePosicion = padre == null ? 0 : padre.siguientePosicion;
    }

    public String getNombre() { return nombre; }
    public Ambito getPadre()  { return padre; }
    public int getNivel()     { return nivel; }
    public Map<String, Simbolo> getSimbolos() { return simbolos; }

    public int getSiguientePosicion() { return siguientePosicion; }

    public void reiniciarPosiciones() { this.siguientePosicion = 0; }

    public int reservar(int celdas) {
        int inicio = siguientePosicion;
        siguientePosicion += celdas;
        return inicio;
    }

    public boolean declarar(Simbolo simbolo) {
        if (simbolos.containsKey(simbolo.getNombre())) {
            return false;
        }
        simbolo.ubicarEnAmbito(nombre, nivel);
        simbolos.put(simbolo.getNombre(), simbolo);
        return true;
    }

    public Simbolo buscarLocal(String nombre) {
        return simbolos.get(nombre);
    }

    public Simbolo buscar(String nombre) {
        Ambito actual = this;
        while (actual != null) {
            Simbolo encontrado = actual.simbolos.get(nombre);
            if (encontrado != null) {
                return encontrado;
            }
            actual = actual.padre;
        }
        return null;
    }

    @Override
    public String toString() {
        return nombre + " (nivel " + nivel + ", " + simbolos.size() + " simbolos)";
    }
}
