package com.usac.contacto3d.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Sistema de tipos unificado para los tres lenguajes.
 *
 * Los tres usan los mismos tipos con nombres distintos, asi que aqui se
 * normalizan:
 *
 *   Y?          Zetariano    PigLatin      Base
 *   entero      int          numerus       ENTERO
 *   flotante    double       decimalis     FLOTANTE
 *   cadena      String       textum        CADENA
 *   caracter    char         littera       CARACTER
 *   bool        boolean      bool          BOOLEANO
 *   -           void         -             VACIO
 *
 * Novedades respecto a la practica:
 *   - ARREGLO lleva una lista de dimensiones (matrices de N dimensiones)
 *   - OBJETO para las clases de Zetariano, que viven en el heap
 *   - getTamanio() en celdas, que el generador de C3D usa para los offsets
 */
public class Tipo {

    public enum Base {
        ENTERO, FLOTANTE, CADENA, CARACTER, BOOLEANO,
        ESTRUCTURA,  // estructura de Y? / PigLatin: vive en el stack
        OBJETO,      // clase de Zetariano: vive en el heap
        ARREGLO,
        VACIO,       // funciones sin retorno
        NULO,        // literal null de Zetariano
        ERROR        // se propaga tras un error, evita cascadas
    }

    public static final Tipo ENTERO    = new Tipo(Base.ENTERO,    null, null, List.of());
    public static final Tipo FLOTANTE  = new Tipo(Base.FLOTANTE,  null, null, List.of());
    public static final Tipo CADENA    = new Tipo(Base.CADENA,    null, null, List.of());
    public static final Tipo CARACTER  = new Tipo(Base.CARACTER,  null, null, List.of());
    public static final Tipo BOOLEANO  = new Tipo(Base.BOOLEANO,  null, null, List.of());
    public static final Tipo VACIO     = new Tipo(Base.VACIO,     null, null, List.of());
    public static final Tipo NULO      = new Tipo(Base.NULO,      null, null, List.of());
    public static final Tipo ERROR     = new Tipo(Base.ERROR,     null, null, List.of());

    private final Base base;
    private final String nombre;            // ESTRUCTURA u OBJETO
    private final Tipo tipoElemento;        // ARREGLO
    private final List<Integer> dimensiones; // ARREGLO; null = no evaluable

    private Tipo(Base base, String nombre, Tipo tipoElemento, List<Integer> dimensiones) {
        this.base = base;
        this.nombre = nombre;
        this.tipoElemento = tipoElemento;
        // No List.copyOf: rechaza null, y una dimension null es valida ("no evaluable",
        // como en el parametro [] entero a o en int[][] antes del new)
        this.dimensiones = dimensiones == null ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(dimensiones));
    }

    public static Tipo estructura(String nombre) {
        return new Tipo(Base.ESTRUCTURA, nombre, null, List.of());
    }

    public static Tipo objeto(String nombre) {
        return new Tipo(Base.OBJETO, nombre, null, List.of());
    }

    /** Arreglo de N dimensiones. Una dimension null significa "no evaluable". */
    public static Tipo arreglo(Tipo tipoElemento, List<Integer> dimensiones) {
        return new Tipo(Base.ARREGLO, null, tipoElemento, dimensiones);
    }

    public static Tipo arreglo(Tipo tipoElemento) {
        return arreglo(tipoElemento, List.of());
    }

    /**
     * Normaliza la palabra de tipo de CUALQUIERA de los tres lenguajes.
     * Lo que no reconoce se asume tipo definido por el usuario.
     */
    public static Tipo desdePalabra(String palabra) {
        return switch (palabra) {
            case "entero", "int", "numerus"        -> ENTERO;
            case "flotante", "double", "decimalis" -> FLOTANTE;
            case "cadena", "String", "textum"      -> CADENA;
            case "caracter", "char", "littera"     -> CARACTER;
            case "bool", "boolean"                 -> BOOLEANO;
            case "void"                            -> VACIO;
            default                                -> estructura(palabra);
        };
    }

    public Base getBase()                  { return base; }
    public String getNombre()              { return nombre; }
    public Tipo getTipoElemento()          { return tipoElemento; }
    public List<Integer> getDimensiones()  { return dimensiones; }

    public boolean esArreglo()    { return base == Base.ARREGLO; }
    public boolean esEstructura() { return base == Base.ESTRUCTURA; }
    public boolean esObjeto()     { return base == Base.OBJETO; }
    public boolean esError()      { return base == Base.ERROR; }

    /** Los objetos y arreglos viven en el heap; el resto en el stack. */
    public boolean viveEnHeap() {
        return base == Base.ARREGLO || base == Base.OBJETO || base == Base.CADENA;
    }

    public boolean esPrimitivo() {
        return switch (base) {
            case ENTERO, FLOTANTE, CADENA, CARACTER, BOOLEANO -> true;
            default -> false;
        };
    }

    /** Participa en aritmetica y comparaciones (todo menos cadena). */
    public boolean esNumerico() {
        return switch (base) {
            case ENTERO, FLOTANTE, CARACTER, BOOLEANO -> true;
            default -> false;
        };
    }

    /**
     * Jerarquia para la inferencia implicita: al operar dos tipos, el
     * resultado toma el de jerarquia mas alta.
     */
    public int getJerarquia() {
        return switch (base) {
            case CADENA   -> 5;
            case FLOTANTE -> 4;
            case ENTERO   -> 3;
            case CARACTER -> 2;
            case BOOLEANO -> 1;
            default       -> 0;
        };
    }

    public static Tipo dominante(Tipo a, Tipo b) {
        return a.getJerarquia() >= b.getJerarquia() ? a : b;
    }

    /**
     * Tamanio en celdas de memoria. Lo usa el generador de C3D para calcular
     * offsets dentro del stack.
     *
     * Los primitivos ocupan una celda. Los arreglos y objetos tambien, porque
     * lo que se guarda en el stack es el PUNTERO al heap, no el contenido.
     * Las estructuras se aplanan: ocupan la suma de sus campos, y por eso el
     * tamanio real lo calcula la tabla de simbolos, no esta clase.
     */
    public int getTamanio() {
        return 1;
    }

    /** Cantidad total de celdas de un arreglo, si todas sus dimensiones se conocen. */
    public Integer getTotalElementos() {
        if (!esArreglo() || dimensiones.isEmpty()) {
            return null;
        }
        int total = 1;
        for (Integer dimension : dimensiones) {
            if (dimension == null) {
                return null;
            }
            total *= dimension;
        }
        return total;
    }

    @Override
    public String toString() {
        return switch (base) {
            case ESTRUCTURA, OBJETO -> nombre;
            case ARREGLO -> {
                StringBuilder sb = new StringBuilder(String.valueOf(tipoElemento));
                if (dimensiones.isEmpty()) {
                    sb.append("[]");
                } else {
                    for (Integer d : dimensiones) {
                        sb.append('[').append(d == null ? "" : d).append(']');
                    }
                }
                yield sb.toString();
            }
            case VACIO -> "void";
            case NULO  -> "null";
            case ERROR -> "error";
            default    -> base.name().toLowerCase();
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tipo otro)) return false;
        return base == otro.base
                && Objects.equals(nombre, otro.nombre)
                && Objects.equals(tipoElemento, otro.tipoElemento);
        // Las dimensiones NO entran: un arreglo de 3 y uno de 5 son asignables.
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, nombre, tipoElemento);
    }
}
