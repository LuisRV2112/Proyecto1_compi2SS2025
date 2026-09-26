package com.usac.contacto3d.c3d;

/**
 * Las operaciones de una cuarteta.
 *
 * Ademas de las clasicas del codigo de tres direcciones hay unas pocas
 * "del sistema" (cadenas, conversiones, lectura) que en C son funciones de la
 * plantilla: hacerlas cuarteta a cuarteta inflaria el codigo sin ensenar nada.
 *
 * Convenciones de los campos (arg1, arg2, resultado):
 *   aritmetica/relacional   resultado = arg1 op arg2
 *   LEER_STACK / LEER_HEAP  resultado = zona[arg1]
 *   ESCRIBIR_*              zona[arg1] = arg2
 *   saltos                  el destino va en resultado
 *   IMPRIMIR, A_CADENA,     arg2 es la etiqueta de tipo: entero, flotante,
 *   CONVERTIR_CADENA        caracter, cadena, bool_y, bool_z, bool_pig
 */
public enum Operacion {
    ASIGNAR,
    SUMA, RESTA, MULTIPLICACION, DIVISION, DIVISION_ENTERA, MODULO, NEGATIVO,
    MENOR, MAYOR, MENOR_IGUAL, MAYOR_IGUAL, IGUAL, DIFERENTE, NOT,

    LEER_STACK, ESCRIBIR_STACK, LEER_HEAP, ESCRIBIR_HEAP,

    ETIQUETA, GOTO, IF_TRUE, IF_FALSE,

    FUNCION, FIN_FUNCION, CALL, RETURN, HALT,

    IMPRIMIR, SALTO_LINEA, LEER_CADENA,
    CONCATENAR, A_CADENA, CONVERTIR_CADENA, IGUAL_CADENAS,
    ERROR_EJECUCION;

    /** Simbolo en la forma "t = a op b", o null si no es una operacion binaria o relacional. */
    public String simbolo() {
        return switch (this) {
            case SUMA -> "+";
            case RESTA -> "-";
            case MULTIPLICACION -> "*";
            case DIVISION -> "/";
            case DIVISION_ENTERA -> "div";
            case MODULO -> "%";
            case MENOR -> "<";
            case MAYOR -> ">";
            case MENOR_IGUAL -> "<=";
            case MAYOR_IGUAL -> ">=";
            case IGUAL -> "==";
            case DIFERENTE -> "!=";
            default -> null;
        };
    }
}
