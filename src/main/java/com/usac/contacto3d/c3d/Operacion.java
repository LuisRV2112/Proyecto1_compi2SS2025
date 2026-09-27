package com.usac.contacto3d.c3d;

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
