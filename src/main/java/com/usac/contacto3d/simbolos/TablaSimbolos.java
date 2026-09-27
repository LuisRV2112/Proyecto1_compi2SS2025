package com.usac.contacto3d.simbolos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TablaSimbolos {

    private final Ambito global = new Ambito("global", null);
    private Ambito actual = global;

    private final Map<String, SimboloEstructura> tipos = new LinkedHashMap<>();

    private final Map<String, SimboloFuncion> funciones = new LinkedHashMap<>();

    private final List<Simbolo> historial = new ArrayList<>();

    private int siguienteHeap = 0;

    private int maximoMarco = 0;

    public Ambito getAmbitoGlobal() { return global; }
    public Ambito getAmbitoActual() { return actual; }

    public Ambito abrirAmbito(String nombre) {
        actual = new Ambito(nombre, actual);
        return actual;
    }

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

    public int reservarStack(int celdas) {
        int posicion = actual.reservar(celdas);
        maximoMarco = Math.max(maximoMarco, actual.getSiguientePosicion());
        return posicion;
    }

    public int reservarHeap(int celdas) {
        int posicion = siguienteHeap;
        siguienteHeap += celdas;
        return posicion;
    }

    public int getTamanioHeap() { return siguienteHeap; }

    public boolean declarar(Simbolo simbolo) {
        if (!actual.declarar(simbolo)) {
            return false;
        }
        historial.add(simbolo);
        return true;
    }

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

    public boolean declararFuncion(SimboloFuncion funcion) {
        if (funciones.containsKey(funcion.getFirma())) {
            return false;
        }
        funciones.put(funcion.getFirma(), funcion);
        historial.add(funcion);
        return true;
    }

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

    public List<Simbolo> getHistorial() { return historial; }

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
