package com.usac.contacto3d.semantico;

import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.expresiones.Operador;

/**
 * Reglas de tipos, las mismas para los tres lenguajes.
 *
 * Jerarquia para la inferencia implicita (la mas alta gana):
 *     cadena 5 > flotante 4 > entero 3 > caracter 2 > bool 1
 *
 *   +               con una cadena concatena (el otro debe ser primitivo);
 *                   si no, gana la jerarquia mas alta
 *   - * /           la cadena no participa; gana la jerarquia mas alta
 *   %               solo enteros (y caracteres); da entero
 *   < > <= >=       numericos; dan bool
 *   == !=           tipos comparables; dan bool
 *   && || !         solo bool
 *
 * Asignar solo puede ampliar dentro de la familia numerica
 * (caracter -> entero -> flotante). bool y cadena exigen el mismo tipo.
 *
 * Los metodos devuelven null cuando la operacion no es valida; quien llama
 * arma el mensaje, porque sabe la posicion y el contexto.
 */
final class Compatibilidad {

    private Compatibilidad() { }

    static Tipo binaria(Operador operador, Tipo a, Tipo b) {
        return switch (operador) {
            case SUMA -> {
                if (a.getBase() == Tipo.Base.CADENA || b.getBase() == Tipo.Base.CADENA) {
                    yield a.esPrimitivo() && b.esPrimitivo() ? Tipo.CADENA : null;
                }
                yield a.esNumerico() && b.esNumerico() ? Tipo.dominante(a, b) : null;
            }
            case RESTA, MULTIPLICACION, DIVISION ->
                    a.esNumerico() && b.esNumerico() ? Tipo.dominante(a, b) : null;
            case MODULO -> esEntero(a) && esEntero(b) ? Tipo.ENTERO : null;
            case MENOR, MAYOR, MENOR_IGUAL, MAYOR_IGUAL ->
                    a.esNumerico() && b.esNumerico() ? Tipo.BOOLEANO : null;
            case IGUAL, DIFERENTE -> comparables(a, b) ? Tipo.BOOLEANO : null;
            case AND, OR -> esBool(a) && esBool(b) ? Tipo.BOOLEANO : null;
            case NOT, NEGATIVO -> throw new IllegalArgumentException("Operador unario: " + operador);
        };
    }

    static Tipo unaria(Operador operador, Tipo a) {
        return switch (operador) {
            // -'a' vale el codigo negado: un caracter negativo no existe, se sube a entero
            case NEGATIVO -> a.esNumerico() && !esBool(a) ? Tipo.dominante(Tipo.ENTERO, a) : null;
            case NOT -> esBool(a) ? Tipo.BOOLEANO : null;
            default -> throw new IllegalArgumentException("Operador binario: " + operador);
        };
    }

    /** Para == y != y para los casos de un elegir/switch. */
    static boolean comparables(Tipo a, Tipo b) {
        if (a.esNumerico() && b.esNumerico()) {
            return true;
        }
        if (a.getBase() == Tipo.Base.NULO || b.getBase() == Tipo.Base.NULO) {
            return aceptaNulo(a) || aceptaNulo(b);
        }
        return a.equals(b);
    }

    static boolean asignable(Tipo destino, Tipo origen) {
        if (destino.esError() || origen.esError()) {
            return true;   // el error ya se reporto donde nacio
        }
        if (origen.getBase() == Tipo.Base.NULO) {
            return aceptaNulo(destino);
        }
        if (destino.esArreglo() && origen.esArreglo()) {
            // Se asigna la referencia: el tamanio no importa, la cantidad de dimensiones si
            return destino.getTipoElemento().equals(origen.getTipoElemento())
                    && destino.getDimensiones().size() == origen.getDimensiones().size();
        }
        if (destino.equals(origen)) {
            return true;
        }
        return switch (destino.getBase()) {
            case FLOTANTE -> origen.getBase() == Tipo.Base.ENTERO || origen.getBase() == Tipo.Base.CARACTER;
            case ENTERO -> origen.getBase() == Tipo.Base.CARACTER;
            default -> false;
        };
    }

    /** true si el origen es numerico y cabe en el destino solo perdiendo informacion. */
    static boolean pierdeInformacion(Tipo destino, Tipo origen) {
        return destino.esNumerico() && origen.esNumerico()
                && destino.getBase() != Tipo.Base.BOOLEANO && origen.getBase() != Tipo.Base.BOOLEANO
                && origen.getJerarquia() > destino.getJerarquia();
    }

    static boolean esEntero(Tipo tipo) {
        return tipo.getBase() == Tipo.Base.ENTERO || tipo.getBase() == Tipo.Base.CARACTER;
    }

    static boolean esBool(Tipo tipo) {
        return tipo.getBase() == Tipo.Base.BOOLEANO;
    }

    private static boolean aceptaNulo(Tipo tipo) {
        return tipo.esObjeto() || tipo.esArreglo() || tipo.getBase() == Tipo.Base.CADENA
                || tipo.getBase() == Tipo.Base.NULO;
    }
}
