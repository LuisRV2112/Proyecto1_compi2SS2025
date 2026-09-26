package com.usac.contacto3d.simbolos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tabla de simbolos con ambitos anidados y control de memoria.
 *
 * Tres cosas que la diferencian de la practica anterior:
 *
 * 1. HISTORIAL. Al cerrar un ambito los simbolos no se pierden: se acumulan.
 *    Sin esto, al terminar el analisis solo quedarian los globales.
 *
 * 2. REGISTRO DE TIPOS APARTE. Las estructuras y clases no son variables, son
 *    tipos. Resolver a.b[i].c obliga a saltar entre el registro de variables y
 *    el de tipos, y tenerlos separados hace ese salto directo.
 *
 * 3. MEMORIA. Lleva la cuenta del stack (por marco de funcion) y del heap
 *    (global y creciente), porque el C3D trabaja con direcciones, no nombres.
 */
public class TablaSimbolos {

    private final Ambito global = new Ambito("global", null);
    private Ambito actual = global;

    /** Estructuras de Y?/PigLatin y clases de Zetariano, por nombre. */
    private final Map<String, SimboloEstructura> tipos = new LinkedHashMap<>();

    /** Funciones globales, indexadas por firma para permitir sobrecarga. */
    private final Map<String, SimboloFuncion> funciones = new LinkedHashMap<>();

    /** Todo lo declarado, incluso de ambitos ya cerrados. */
    private final List<Simbolo> historial = new ArrayList<>();

    /** Siguiente celda libre del heap. Crece y no se reutiliza. */
    private int siguienteHeap = 0;

    /** Marco de stack de la funcion que se esta analizando. */
    private int maximoMarco = 0;

    public Ambito getAmbitoGlobal() { return global; }
    public Ambito getAmbitoActual() { return actual; }

    /* ------------------------- Ambitos ------------------------------- */

    public Ambito abrirAmbito(String nombre) {
        actual = new Ambito(nombre, actual);
        return actual;
    }

    /** Abre el ambito de una funcion: su marco de stack arranca en cero. */
    public Ambito abrirMarcoFuncion(String nombre) {
        actual = new Ambito(nombre, actual);
        actual.reiniciarPosiciones();
        maximoMarco = 0;
        return actual;
    }

    public void cerrarAmbito() {
        maximoMarco = Math.max(maximoMarco, actual.getSiguientePosicion());
        if (actual.getPadre() != null) {
            actual = actual.getPadre();
        }
    }

    public int getMaximoMarco() { return maximoMarco; }

    /* ------------------------- Memoria ------------------------------- */

    /** Reserva celdas en el stack del ambito actual. */
    public int reservarStack(int celdas) {
        int posicion = actual.reservar(celdas);
        maximoMarco = Math.max(maximoMarco, actual.getSiguientePosicion());
        return posicion;
    }

    /** Reserva celdas en el heap. */
    public int reservarHeap(int celdas) {
        int posicion = siguienteHeap;
        siguienteHeap += celdas;
        return posicion;
    }

    public int getTamanioHeap() { return siguienteHeap; }

    /* ------------------------- Simbolos ------------------------------ */

    /** @return false si ya existia en ESTE ambito */
    public boolean declarar(Simbolo simbolo) {
        if (!actual.declarar(simbolo)) {
            return false;
        }
        historial.add(simbolo);
        return true;
    }

    /**
     * Declara una variable asignandole memoria automaticamente.
     * Los arreglos, objetos y cadenas guardan un puntero en el stack y su
     * contenido en el heap.
     */
    public boolean declararConMemoria(SimboloVariable variable) {
        int celdas = variable.getTamanio();
        int posicion = reservarStack(celdas);
        variable.ubicarEnMemoria(Simbolo.Almacenamiento.STACK, posicion);
        return declarar(variable);
    }

    public Simbolo buscar(String nombre)      { return actual.buscar(nombre); }
    public Simbolo buscarLocal(String nombre) { return actual.buscarLocal(nombre); }

    public SimboloVariable buscarVariable(String nombre) {
        return actual.buscar(nombre) instanceof SimboloVariable v ? v : null;
    }

    /* --------------------- Funciones y tipos -------------------------- */

    /** @return false si ya existia una funcion con la misma firma */
    public boolean declararFuncion(SimboloFuncion funcion) {
        if (funciones.containsKey(funcion.getFirma())) {
            return false;
        }
        funciones.put(funcion.getFirma(), funcion);
        historial.add(funcion);
        return true;
    }

    /** Busca por nombre y cantidad de argumentos (sobrecarga). */
    public SimboloFuncion buscarFuncion(String nombre, int cantidadArgumentos) {
        for (SimboloFuncion funcion : funciones.values()) {
            if (funcion.getNombre().equals(nombre)
                    && funcion.getCantidadParametros() == cantidadArgumentos) {
                return funcion;
            }
        }
        return null;
    }

    public boolean existeFuncion(String nombre) {
        return funciones.values().stream().anyMatch(f -> f.getNombre().equals(nombre));
    }

    public Map<String, SimboloFuncion> getFunciones() { return funciones; }

    /** @return false si ya existia un tipo con ese nombre */
    public boolean declararTipo(SimboloEstructura tipoUsuario) {
        if (tipos.containsKey(tipoUsuario.getNombre())) {
            return false;
        }
        tipoUsuario.ubicarEnAmbito(actual.getNombre(), actual.getNivel());
        tipos.put(tipoUsuario.getNombre(), tipoUsuario);
        historial.add(tipoUsuario);
        return true;
    }

    public SimboloEstructura buscarTipo(String nombre) { return tipos.get(nombre); }
    public boolean existeTipo(String nombre)           { return tipos.containsKey(nombre); }
    public Map<String, SimboloEstructura> getTipos()   { return tipos; }

    /* -------------------------- Reporte ------------------------------ */

    public List<Simbolo> getHistorial() { return historial; }

    /**
     * Para lo que no pasa por declarar(): metodos y constructores viven dentro
     * de su clase, no en un ambito, pero igual deben salir en el reporte.
     */
    public void registrarEnHistorial(Simbolo simbolo) {
        historial.add(simbolo);
    }

    public void limpiar() {
        global.getSimbolos().clear();
        global.reiniciarPosiciones();
        tipos.clear();
        funciones.clear();
        historial.clear();
        siguienteHeap = 0;
        maximoMarco = 0;
        actual = global;
    }
}
