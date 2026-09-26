package com.usac.contacto3d.semantico;

import com.usac.contacto3d.ast.expresiones.Expresion;
import com.usac.contacto3d.ast.expresiones.Literal;
import com.usac.contacto3d.ast.expresiones.OperacionBinaria;
import com.usac.contacto3d.ast.expresiones.OperacionUnaria;
import com.usac.contacto3d.ast.expresiones.Operador;

/**
 * Calcula en compilacion el valor entero de una expresion, si se puede.
 *
 * El enunciado pide reportar un indice fuera de rango "si la expresion se
 * puede evaluar": arr[2 + 3] da 5 y se valida; arr[i] da null y no.
 */
final class EvaluadorConstantes {

    private EvaluadorConstantes() { }

    static Integer entero(Expresion expresion) {
        return switch (expresion) {
            case Literal l when l.getValor() instanceof Integer i -> i;
            case Literal l when l.getValor() instanceof Character c -> (int) c;
            case OperacionUnaria u when u.getOperador() == Operador.NEGATIVO -> {
                Integer valor = entero(u.getOperando());
                yield valor == null ? null : -valor;
            }
            case OperacionBinaria b -> binaria(b);
            default -> null;
        };
    }

    private static Integer binaria(OperacionBinaria operacion) {
        Integer a = entero(operacion.getIzquierda());
        Integer b = entero(operacion.getDerecha());
        if (a == null || b == null) {
            return null;
        }
        return switch (operacion.getOperador()) {
            case SUMA -> a + b;
            case RESTA -> a - b;
            case MULTIPLICACION -> a * b;
            case DIVISION -> b == 0 ? null : a / b;   // la division entre cero no es constante, es un error de ejecucion
            case MODULO -> b == 0 ? null : a % b;
            default -> null;
        };
    }
}
