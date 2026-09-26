package com.usac.contacto3d.ast.expresiones;

/** Operadores de los tres lenguajes, normalizados. */
public enum Operador {
    SUMA("+"), RESTA("-"), MULTIPLICACION("*"), DIVISION("/"), MODULO("%"),
    MENOR("<"), MAYOR(">"), MENOR_IGUAL("<="), MAYOR_IGUAL(">="),
    IGUAL("=="), DIFERENTE("!="),
    AND("&&"), OR("||"), NOT("!"),
    NEGATIVO("-");

    private final String simbolo;

    Operador(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getSimbolo() {
        return simbolo;
    }

    /** Operador binario a partir de su simbolo en cualquiera de los tres lenguajes. */
    public static Operador binario(String simbolo) {
        return switch (simbolo) {
            case "+"  -> SUMA;
            case "-"  -> RESTA;
            case "*"  -> MULTIPLICACION;
            case "/"  -> DIVISION;
            case "%"  -> MODULO;
            case "<"  -> MENOR;
            case ">"  -> MAYOR;
            case "<=" -> MENOR_IGUAL;
            case ">=" -> MAYOR_IGUAL;
            case "==" -> IGUAL;
            case "!=" -> DIFERENTE;
            case "&&" -> AND;
            case "||" -> OR;
            default -> throw new IllegalArgumentException("Operador binario desconocido: " + simbolo);
        };
    }

    /** El '-' unario es NEGATIVO, no RESTA; la negacion es '!' o 'non' segun el lenguaje. */
    public static Operador unario(String simbolo) {
        return switch (simbolo) {
            case "-"        -> NEGATIVO;
            case "!", "non" -> NOT;
            default -> throw new IllegalArgumentException("Operador unario desconocido: " + simbolo);
        };
    }

    /** Operacion de una asignacion compuesta de Zetariano: += es SUMA. Null para '='. */
    public static Operador deAsignacion(String simbolo) {
        return switch (simbolo) {
            case "="  -> null;
            case "+=" -> SUMA;
            case "-=" -> RESTA;
            case "*=" -> MULTIPLICACION;
            default -> throw new IllegalArgumentException("Asignacion desconocida: " + simbolo);
        };
    }

    public boolean esAritmetico() {
        return this == SUMA || this == RESTA || this == MULTIPLICACION
                || this == DIVISION || this == MODULO || this == NEGATIVO;
    }

    public boolean esRelacional() {
        return this == MENOR || this == MAYOR || this == MENOR_IGUAL
                || this == MAYOR_IGUAL || this == IGUAL || this == DIFERENTE;
    }

    public boolean esLogico() {
        return this == AND || this == OR || this == NOT;
    }
}
