package com.usac.contacto3d.c3d;

public record Cuarteta(Operacion operacion, String arg1, String arg2, String resultado) {

    @Override
    public String toString() {
        String simbolo = operacion.simbolo();
        if (simbolo != null) {
            return resultado + " = " + arg1 + " " + simbolo + " " + arg2;
        }
        return switch (operacion) {
            case ASIGNAR -> resultado + " = " + arg1;
            case NEGATIVO -> resultado + " = -" + arg1;
            case NOT -> resultado + " = !" + arg1;
            case LEER_STACK -> resultado + " = stack[" + arg1 + "]";
            case LEER_HEAP -> resultado + " = heap[" + arg1 + "]";
            case ESCRIBIR_STACK -> "stack[" + arg1 + "] = " + arg2;
            case ESCRIBIR_HEAP -> "heap[" + arg1 + "] = " + arg2;
            case ETIQUETA -> resultado + ":";
            case GOTO -> "goto " + resultado;
            case IF_TRUE -> "if " + arg1 + " goto " + resultado;
            case IF_FALSE -> "if_false " + arg1 + " goto " + resultado;
            case FUNCION -> "funcion " + arg1 + ":";
            case FIN_FUNCION -> "fin " + arg1;
            case CALL -> "call " + arg1;
            case RETURN -> "return";
            case HALT -> "halt";
            case IMPRIMIR -> "print " + arg1 + "  (" + arg2 + ")";
            case SALTO_LINEA -> "print_salto";
            case LEER_CADENA -> resultado + " = leer";
            case CONCATENAR -> resultado + " = concatenar " + arg1 + ", " + arg2;
            case A_CADENA -> resultado + " = a_cadena " + arg1 + "  (" + arg2 + ")";
            case CONVERTIR_CADENA -> resultado + " = convertir " + arg1 + "  (" + arg2 + ")";
            case IGUAL_CADENAS -> resultado + " = cadenas_iguales " + arg1 + ", " + arg2;
            case ERROR_EJECUCION -> "error " + arg1;
            default -> operacion + " " + arg1 + ", " + arg2 + ", " + resultado;
        };
    }
}
