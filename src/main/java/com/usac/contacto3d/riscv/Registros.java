package com.usac.contacto3d.riscv;

import java.util.HashMap;
import java.util.Map;

final class Registros {

    private static final Map<String, Integer> ENTEROS = new HashMap<>();
    private static final Map<String, Integer> FLOTANTES = new HashMap<>();

    static {
        String[] abi = {"zero", "ra", "sp", "gp", "tp", "t0", "t1", "t2", "s0", "s1",
                "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7",
                "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11", "t3", "t4", "t5", "t6"};
        for (int i = 0; i < 32; i++) {
            ENTEROS.put("x" + i, i);
            ENTEROS.put(abi[i], i);
        }
        ENTEROS.put("fp", 8);

        String[] abiF = {"ft0", "ft1", "ft2", "ft3", "ft4", "ft5", "ft6", "ft7", "fs0", "fs1",
                "fa0", "fa1", "fa2", "fa3", "fa4", "fa5", "fa6", "fa7",
                "fs2", "fs3", "fs4", "fs5", "fs6", "fs7", "fs8", "fs9", "fs10", "fs11",
                "ft8", "ft9", "ft10", "ft11"};
        for (int i = 0; i < 32; i++) {
            FLOTANTES.put("f" + i, i);
            FLOTANTES.put(abiF[i], i);
        }
    }

    private Registros() { }

    static int entero(String nombre, int linea) {
        Integer r = ENTEROS.get(nombre.trim().toLowerCase());
        if (r == null) {
            throw new Ensamblador.ErrorEnsamblado(linea, "registro entero desconocido '" + nombre + "'");
        }
        return r;
    }

    static int flotante(String nombre, int linea) {
        Integer r = FLOTANTES.get(nombre.trim().toLowerCase());
        if (r == null) {
            throw new Ensamblador.ErrorEnsamblado(linea, "registro flotante desconocido '" + nombre + "'");
        }
        return r;
    }
}
