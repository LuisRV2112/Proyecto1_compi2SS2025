package com.usac.contacto3d.c3d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ejecuta las cuartetas directamente, sin pasar por C.
 *
 * Sirve para comprobar que el generador es correcto antes de tener el
 * traductor (y despues, para comparar: el .c compilado debe imprimir lo mismo).
 * Por eso reproduce el modelo que va a tener el C: stack y heap de celdas
 * double, P y H globales, y temporales LOCALES a cada funcion.
 *
 * Las operaciones del sistema (cadenas, conversiones, lectura) se definen
 * aqui; la plantilla de C de la fase 5 tiene que hacer exactamente lo mismo.
 */
public class InterpreteCuartetas {

    private static final int CELDAS = 1_000_000;
    private static final long MAXIMO_PASOS = 100_000_000L;
    private static final int MAXIMA_PROFUNDIDAD = 3_000;
    private static final double FIN_CADENA = -1;

    /** Termina la ejecucion: halt, un error de ejecucion o un limite superado. */
    private static class Fin extends RuntimeException {
        Fin() {
            super(null, null, false, false);
        }
    }

    private final List<Cuarteta> codigo;
    private final Map<String, Integer> funciones = new HashMap<>();
    private final Map<String, Integer> etiquetas = new HashMap<>();
    private final double[] stack = new double[CELDAS];
    private final double[] heap = new double[CELDAS];
    private double p;
    private double h;

    private final StringBuilder salida = new StringBuilder();
    private final BufferedReader entrada;
    private long pasos;
    private int profundidad;

    private InterpreteCuartetas(ListaCuartetas lista, String entrada) {
        this.codigo = lista.getCuartetas();
        this.entrada = new BufferedReader(new StringReader(entrada == null ? "" : entrada));
        for (int i = 0; i < codigo.size(); i++) {
            Cuarteta c = codigo.get(i);
            if (c.operacion() == Operacion.FUNCION) {
                funciones.put(c.arg1(), i);
            } else if (c.operacion() == Operacion.ETIQUETA) {
                etiquetas.put(c.resultado(), i);
            }
        }
    }

    /** Ejecuta el programa desde main y devuelve todo lo que imprimio. */
    public static String ejecutar(ListaCuartetas programa, String entrada) {
        InterpreteCuartetas interprete = new InterpreteCuartetas(programa, entrada);
        try {
            interprete.llamar("main");
        } catch (Fin fin) {
            // halt o error: la salida ya tiene lo que corresponde
        }
        return interprete.salida.toString();
    }

    private void llamar(String funcion) {
        Integer inicio = funciones.get(funcion);
        if (inicio == null) {
            abortar("no existe la funcion " + funcion);
        }
        if (++profundidad > MAXIMA_PROFUNDIDAD) {
            abortar("desbordamiento de pila (recursion demasiado profunda)");
        }
        Map<String, Double> temporales = new HashMap<>();
        int pc = inicio + 1;
        while (true) {
            if (++pasos > MAXIMO_PASOS) {
                abortar("se supero el limite de instrucciones (ciclo infinito?)");
            }
            Cuarteta c = codigo.get(pc++);
            switch (c.operacion()) {
                case FIN_FUNCION, RETURN -> {
                    profundidad--;
                    return;
                }
                case HALT -> throw new Fin();
                case FUNCION, ETIQUETA -> { }
                case GOTO -> pc = etiquetas.get(c.resultado());
                case IF_TRUE -> {
                    if (valor(c.arg1(), temporales) != 0) {
                        pc = etiquetas.get(c.resultado());
                    }
                }
                case IF_FALSE -> {
                    if (valor(c.arg1(), temporales) == 0) {
                        pc = etiquetas.get(c.resultado());
                    }
                }
                case CALL -> llamar(c.arg1());
                default -> ejecutar(c, temporales);
            }
        }
    }

    private void ejecutar(Cuarteta c, Map<String, Double> t) {
        double a = c.arg1() == null ? 0 : valor(c.arg1(), t);
        switch (c.operacion()) {
            case ASIGNAR -> asignar(c.resultado(), a, t);
            case SUMA -> asignar(c.resultado(), a + valor(c.arg2(), t), t);
            case RESTA -> asignar(c.resultado(), a - valor(c.arg2(), t), t);
            case MULTIPLICACION -> asignar(c.resultado(), a * valor(c.arg2(), t), t);
            case DIVISION -> asignar(c.resultado(), a / divisor(valor(c.arg2(), t)), t);
            case DIVISION_ENTERA -> asignar(c.resultado(), (long) a / (long) divisor(valor(c.arg2(), t)), t);
            case MODULO -> asignar(c.resultado(), (long) a % (long) divisor(valor(c.arg2(), t)), t);
            case NEGATIVO -> asignar(c.resultado(), -a, t);
            case MENOR -> asignar(c.resultado(), a < valor(c.arg2(), t) ? 1 : 0, t);
            case MAYOR -> asignar(c.resultado(), a > valor(c.arg2(), t) ? 1 : 0, t);
            case MENOR_IGUAL -> asignar(c.resultado(), a <= valor(c.arg2(), t) ? 1 : 0, t);
            case MAYOR_IGUAL -> asignar(c.resultado(), a >= valor(c.arg2(), t) ? 1 : 0, t);
            case IGUAL -> asignar(c.resultado(), a == valor(c.arg2(), t) ? 1 : 0, t);
            case DIFERENTE -> asignar(c.resultado(), a != valor(c.arg2(), t) ? 1 : 0, t);
            case NOT -> asignar(c.resultado(), a == 0 ? 1 : 0, t);
            case LEER_STACK -> asignar(c.resultado(), stack[celda(a, stack)], t);
            case LEER_HEAP -> asignar(c.resultado(), heap[celda(a, heap)], t);
            case ESCRIBIR_STACK -> stack[celda(a, stack)] = valor(c.arg2(), t);
            case ESCRIBIR_HEAP -> heap[celda(a, heap)] = valor(c.arg2(), t);
            case IMPRIMIR -> salida.append(texto(a, c.arg2()));
            case SALTO_LINEA -> salida.append('\n');
            case LEER_CADENA -> asignar(c.resultado(), nuevaCadena(leerLinea()), t);
            case CONCATENAR -> asignar(c.resultado(), nuevaCadena(cadena(a) + cadena(valor(c.arg2(), t))), t);
            case A_CADENA -> asignar(c.resultado(), nuevaCadena(texto(a, c.arg2())), t);
            case CONVERTIR_CADENA -> asignar(c.resultado(), convertir(cadena(a), c.arg2()), t);
            case IGUAL_CADENAS -> asignar(c.resultado(), cadena(a).equals(cadena(valor(c.arg2(), t))) ? 1 : 0, t);
            case ERROR_EJECUCION -> {
                salida.append(cadena(a)).append('\n');
                throw new Fin();
            }
            default -> throw new IllegalStateException("Operacion sin ejecutar: " + c.operacion());
        }
    }

    /* ---------------- operandos ---------------- */

    private double valor(String operando, Map<String, Double> temporales) {
        return switch (operando) {
            case "P" -> p;
            case "H" -> h;
            default -> operando.charAt(0) == 't' ? temporales.getOrDefault(operando, 0.0) : Double.parseDouble(operando);
        };
    }

    private void asignar(String destino, double valor, Map<String, Double> temporales) {
        switch (destino) {
            case "P" -> p = valor;
            case "H" -> h = valor;
            default -> temporales.put(destino, valor);
        }
    }

    private int celda(double direccion, double[] zona) {
        if (direccion < 0 || direccion >= zona.length) {
            abortar("acceso fuera de la memoria (" + (long) direccion + ")");
        }
        return (int) direccion;
    }

    private double divisor(double valor) {
        if (valor == 0) {
            abortar("division entre cero");
        }
        return valor;
    }

    private void abortar(String mensaje) {
        salida.append("Error de ejecucion: ").append(mensaje).append('\n');
        throw new Fin();
    }

    /* ---------------- sistema: cadenas y conversiones ---------------- */

    private String cadena(double puntero) {
        StringBuilder sb = new StringBuilder();
        int i = celda(puntero, heap);
        while (heap[i] != FIN_CADENA) {
            sb.append((char) heap[i]);
            i = celda(i + 1, heap);
        }
        return sb.toString();
    }

    private double nuevaCadena(String texto) {
        double puntero = h;
        for (int i = 0; i < texto.length(); i++) {
            heap[celda(h++, heap)] = texto.charAt(i);
        }
        heap[celda(h++, heap)] = FIN_CADENA;
        return puntero;
    }

    /** El texto de un valor segun su tipo; lo usan IMPRIMIR y A_CADENA. */
    private String texto(double valor, String tipo) {
        return switch (tipo) {
            case "cadena" -> cadena(valor);
            case "flotante" -> formatearFlotante(valor);
            case "caracter" -> String.valueOf((char) valor);
            case "bool_y" -> valor != 0 ? "verdadero" : "falso";
            case "bool_z" -> valor != 0 ? "true" : "false";
            case "bool_pig" -> valor != 0 ? "verum" : "falsus";
            default -> String.valueOf((long) valor);
        };
    }

    /**
     * Un flotante entero se escribe con ".0" (como Java); si no, con hasta 6
     * decimales sin ceros sobrantes. La plantilla de C hace lo mismo con printf.
     *
     * Se redondea el valor binario EXACTO al par mas cercano, que es lo que hace
     * printf de glibc; String.format redondea los empates hacia arriba y en
     * casos como 0.0078125 daria un digito distinto al del programa en C.
     */
    public static String formatearFlotante(double valor) {
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            return Double.isNaN(valor) ? "nan" : valor > 0 ? "inf" : "-inf";
        }
        // printf conserva el signo aunque el resultado redondee a cero (-0.0); BigDecimal no
        String signo = valor < 0 || (valor == 0 && 1 / valor < 0) ? "-" : "";
        BigDecimal absoluto = new BigDecimal(Math.abs(valor));
        if (valor == Math.rint(valor) && Math.abs(valor) < 1e15) {
            return signo + absoluto.setScale(1, RoundingMode.HALF_EVEN).toPlainString();
        }
        String texto = absoluto.setScale(6, RoundingMode.HALF_EVEN).toPlainString().replaceAll("0+$", "");
        return signo + (texto.endsWith(".") ? texto + "0" : texto);
    }

    /** Lo que se lee con "x <<" a un tipo no textual. Si no se entiende, vale 0. */
    private static double convertir(String texto, String tipo) {
        String limpio = texto.trim();
        try {
            return switch (tipo) {
                case "entero" -> (long) Double.parseDouble(limpio);
                case "flotante" -> Double.parseDouble(limpio);
                case "caracter" -> limpio.isEmpty() ? 0 : limpio.charAt(0);
                case "bool" -> switch (limpio) {
                    case "verdadero", "true", "verum", "1" -> 1;
                    default -> 0;
                };
                default -> 0;
            };
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String leerLinea() {
        try {
            String linea = entrada.readLine();
            return linea == null ? "" : linea;
        } catch (IOException e) {
            return "";
        }
    }
}
