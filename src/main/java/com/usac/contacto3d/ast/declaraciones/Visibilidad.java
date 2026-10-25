package com.usac.contacto3d.ast.declaraciones;

/**
 * Visibilidad de un miembro de clase. PAQUETE es la ausencia de modificador: no hay paquetes en el
 * proyecto y se trata como private, pero se distingue para que los mensajes de error lo digan.
 */
public enum Visibilidad {
    PUBLICO("public"), PRIVADO("private"), PROTEGIDO("protected"), PAQUETE("sin modificador");

    private final String palabra;

    Visibilidad(String palabra) {
        this.palabra = palabra;
    }

    public String getPalabra() { return palabra; }

    public static Visibilidad desde(String palabra) {
        if (palabra == null) {
            return PAQUETE;
        }
        return switch (palabra) {
            case "public"    -> PUBLICO;
            case "private"   -> PRIVADO;
            case "protected" -> PROTEGIDO;
            default          -> PAQUETE;
        };
    }

    /** Prefijo para la etiqueta del AST: vacio cuando no hay modificador. */
    String prefijo() {
        return this == PAQUETE ? "" : palabra + " ";
    }
}
