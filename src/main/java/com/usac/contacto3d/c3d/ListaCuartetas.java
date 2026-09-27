package com.usac.contacto3d.c3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListaCuartetas {

    private final List<Cuarteta> cuartetas = new ArrayList<>();
    private int contadorTemporales = 0;
    private int contadorEtiquetas = 0;

    public String nuevoTemporal() { return "t" + (contadorTemporales++); }
    public String nuevaEtiqueta() { return "L" + (contadorEtiquetas++); }

    public void agregar(Operacion operacion, String arg1, String arg2, String resultado) {
        cuartetas.add(new Cuarteta(operacion, arg1, arg2, resultado));
    }

    public void agregarTodas(ListaCuartetas otra) {
        cuartetas.addAll(otra.cuartetas);
    }

    public List<Cuarteta> getCuartetas() {
        return Collections.unmodifiableList(cuartetas);
    }

    public int getCantidadTemporales() {
        return contadorTemporales;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Cuarteta c : cuartetas) {
            boolean margen = c.operacion() == Operacion.ETIQUETA || c.operacion() == Operacion.FUNCION
                    || c.operacion() == Operacion.FIN_FUNCION;
            sb.append(margen ? "" : "    ").append(c).append('\n');
        }
        return sb.toString();
    }
}
