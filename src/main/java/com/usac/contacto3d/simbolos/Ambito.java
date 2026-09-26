package com.usac.contacto3d.simbolos;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Un ambito con encadenamiento hacia el que lo contiene.
 *
 * La busqueda sube por la cadena: si un nombre no esta aqui, se le pregunta al
 * padre. Asi una funcion ve las globales, pero lo declarado dentro de un ciclo
 * no se filtra hacia afuera.
 */
public class Ambito {

    private final String nombre;
    private final Ambito padre;
    private final int nivel;
    private final Map<String, Simbolo> simbolos = new LinkedHashMap<>();

    /** Siguiente posicion libre del stack dentro de este ambito. */
    private int siguientePosicion;

    public Ambito(String nombre, Ambito padre) {
        this.nombre = nombre;
        this.padre = padre;
        this.nivel = padre == null ? 0 : padre.nivel + 1;
        // Un bloque interno sigue numerando donde quedo el de afuera; una
        // funcion nueva arranca su propio marco desde cero.
        this.siguientePosicion = padre == null ? 0 : padre.siguientePosicion;
    }

    public String getNombre() { return nombre; }
    public Ambito getPadre()  { return padre; }
    public int getNivel()     { return nivel; }
    public Map<String, Simbolo> getSimbolos() { return simbolos; }

    public int getSiguientePosicion() { return siguientePosicion; }

    public void reiniciarPosiciones() { this.siguientePosicion = 0; }

    /** Reserva celdas en el stack y devuelve la posicion inicial. */
    public int reservar(int celdas) {
        int inicio = siguientePosicion;
        siguientePosicion += celdas;
        return inicio;
    }

    /** @return false si el nombre ya estaba ocupado AQUI (redeclaracion) */
    public boolean declarar(Simbolo simbolo) {
        if (simbolos.containsKey(simbolo.getNombre())) {
            return false;
        }
        simbolo.ubicarEnAmbito(nombre, nivel);
        simbolos.put(simbolo.getNombre(), simbolo);
        return true;
    }

    /** Busca solo aqui. Sirve para detectar redeclaraciones. */
    public Simbolo buscarLocal(String nombre) {
        return simbolos.get(nombre);
    }

    /** Busca aqui y sube por la cadena de ambitos. */
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
