package com.usac.contacto3d.ast.declaraciones;

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

    String prefijo() {
        return this == PAQUETE ? "" : palabra + " ";
    }
}
