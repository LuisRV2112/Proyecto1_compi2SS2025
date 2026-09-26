package com.usac.contacto3d.ast;

/** De que lenguaje salio un archivo. El semantico lo necesita para reglas propias de cada uno. */
public enum Lenguaje {
    Y("Y?", ".y"),
    ZETARIANO("Zetariano", ".z"),
    PIGLATIN("PigLatin", ".pig");

    private final String nombre;
    private final String extension;

    Lenguaje(String nombre, String extension) {
        this.nombre = nombre;
        this.extension = extension;
    }

    public String getNombre()    { return nombre; }
    public String getExtension() { return extension; }

    /** Null si la extension no es de ninguno de los tres. */
    public static Lenguaje desdeArchivo(String archivo) {
        for (Lenguaje lenguaje : values()) {
            if (archivo.endsWith(lenguaje.extension)) {
                return lenguaje;
            }
        }
        return null;
    }
}
