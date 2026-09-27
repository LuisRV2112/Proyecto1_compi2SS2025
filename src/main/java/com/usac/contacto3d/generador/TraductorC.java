package com.usac.contacto3d.generador;

import com.usac.contacto3d.c3d.Cuarteta;
import com.usac.contacto3d.c3d.ListaCuartetas;
import com.usac.contacto3d.c3d.Operacion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class TraductorC {

    private static final String SANGRIA = "    ";

    private static final Set<Operacion> SIN_EFECTOS = Set.of(
            Operacion.ASIGNAR, Operacion.SUMA, Operacion.RESTA, Operacion.MULTIPLICACION,
            Operacion.NEGATIVO, Operacion.MENOR, Operacion.MAYOR, Operacion.MENOR_IGUAL,
            Operacion.MAYOR_IGUAL, Operacion.IGUAL, Operacion.DIFERENTE, Operacion.NOT,
            Operacion.LEER_STACK, Operacion.LEER_HEAP, Operacion.IGUAL_CADENAS);

    public String traducir(ListaCuartetas programa) {
        Map<String, List<Cuarteta>> funciones = separarFunciones(programa.getCuartetas());

        StringBuilder c = new StringBuilder(PLANTILLA);
        c.append("/* ------------------------------ prototipos ------------------------------ */\n\n");
        for (String funcion : funciones.keySet()) {
            if (!funcion.equals("main")) {
                c.append("void ").append(nombreC(funcion)).append("(void);\n");
            }
        }
        c.append("\n/* ------------------------------ programa ------------------------------- */\n");
        for (Map.Entry<String, List<Cuarteta>> funcion : funciones.entrySet()) {
            c.append('\n');
            traducirFuncion(funcion.getKey(), funcion.getValue(), c);
        }
        return c.toString();
    }

    private static Map<String, List<Cuarteta>> separarFunciones(List<Cuarteta> cuartetas) {
        Map<String, List<Cuarteta>> funciones = new LinkedHashMap<>();
        List<Cuarteta> actual = null;
        for (Cuarteta cuarteta : cuartetas) {
            switch (cuarteta.operacion()) {
                case FUNCION -> {
                    actual = new ArrayList<>();
                    funciones.put(cuarteta.arg1(), actual);
                }
                case FIN_FUNCION -> actual = null;
                default -> {
                    if (actual == null) {
                        throw new IllegalStateException("Cuarteta fuera de una funcion: " + cuarteta);
                    }
                    actual.add(cuarteta);
                }
            }
        }
        return funciones;
    }

    private void traducirFuncion(String nombre, List<Cuarteta> cuerpo, StringBuilder c) {
        Set<String> leidos = new HashSet<>();
        Set<String> etiquetasUsadas = new HashSet<>();
        for (Cuarteta cuarteta : cuerpo) {
            marcarLeido(cuarteta.arg1(), leidos);
            marcarLeido(cuarteta.arg2(), leidos);
            if (cuarteta.operacion() == Operacion.GOTO || cuarteta.operacion() == Operacion.IF_TRUE
                    || cuarteta.operacion() == Operacion.IF_FALSE) {
                etiquetasUsadas.add(cuarteta.resultado());
            }
        }

        boolean esMain = nombre.equals("main");
        c.append(esMain ? "int main(void) {\n" : "void " + nombreC(nombre) + "(void) {\n");

        Set<Integer> temporales = new TreeSet<>();
        for (String temporal : leidos) {
            if (esTemporal(temporal)) {
                temporales.add(Integer.parseInt(temporal.substring(1)));
            }
        }
        if (!temporales.isEmpty()) {
            List<String> nombres = temporales.stream().map(t -> "t" + t).toList();
            c.append(SANGRIA).append("double ").append(String.join(", ", nombres)).append(";\n");
        }
        if (!esMain) {
            c.append(SANGRIA).append("verificar_pila();\n");
        }

        for (Cuarteta cuarteta : cuerpo) {
            String linea = traducir(cuarteta, leidos, etiquetasUsadas, esMain);
            if (linea == null) {
                continue;
            }
            boolean esEtiqueta = cuarteta.operacion() == Operacion.ETIQUETA;
            c.append(esEtiqueta ? "" : SANGRIA).append(linea).append('\n');
        }
        boolean terminaEnHalt = !cuerpo.isEmpty() && cuerpo.get(cuerpo.size() - 1).operacion() == Operacion.HALT;
        if (esMain && !terminaEnHalt) {
            c.append(SANGRIA).append("return 0;\n");
        }
        c.append("}\n");
    }

    private String traducir(Cuarteta q, Set<String> leidos, Set<String> etiquetasUsadas, boolean esMain) {
        String r = q.resultado();
        String a = operando(q.arg1());
        String b = operando(q.arg2());

        boolean resultadoMuerto = r != null && esTemporal(r) && !leidos.contains(r);
        if (resultadoMuerto && SIN_EFECTOS.contains(q.operacion())) {
            return null;
        }
        String destino = resultadoMuerto ? "" : operando(r) + " = ";

        String simbolo = q.operacion().simbolo();
        return switch (q.operacion()) {
            case ASIGNAR -> asignacion(r, a);
            case SUMA, RESTA, MULTIPLICACION -> asignacion(r, a + " " + simbolo + " " + b);
            case DIVISION -> destino + "dividir(" + a + ", " + b + ");";
            case DIVISION_ENTERA -> destino + "dividir_entero(" + a + ", " + b + ");";
            case MODULO -> destino + "modulo(" + a + ", " + b + ");";
            case NEGATIVO -> destino + "-(" + a + ");";
            case MENOR, MAYOR, MENOR_IGUAL, MAYOR_IGUAL, IGUAL, DIFERENTE ->
                    destino + "(" + a + " " + simbolo + " " + b + ");";
            case NOT -> destino + "(" + a + " == 0);";
            case LEER_STACK -> destino + "stack[" + indice(q.arg1()) + "];";
            case LEER_HEAP -> destino + "heap[" + indice(q.arg1()) + "];";
            case ESCRIBIR_STACK -> "stack[" + indice(q.arg1()) + "] = " + b + ";";
            case ESCRIBIR_HEAP -> "heap[" + indice(q.arg1()) + "] = " + b + ";";
            case ETIQUETA -> etiquetasUsadas.contains(r) ? r + ": ;" : null;
            case GOTO -> "goto " + r + ";";
            case IF_TRUE -> "if (" + a + " != 0) goto " + r + ";";
            case IF_FALSE -> "if (" + a + " == 0) goto " + r + ";";
            case CALL -> nombreC(q.arg1()) + "();";
            case RETURN -> "return;";
            case HALT -> esMain ? "return 0;" : "exit(0);";
            case IMPRIMIR -> imprimir(a, q.arg2());
            case SALTO_LINEA -> "putchar('\\n');";
            case LEER_CADENA -> destino + "leer_cadena();";
            case CONCATENAR -> destino + "concatenar(" + a + ", " + b + ");";
            case A_CADENA -> destino + aCadena(a, q.arg2()) + ";";
            case CONVERTIR_CADENA -> destino + "convertir_" + q.arg2() + "(" + a + ");";
            case IGUAL_CADENAS -> destino + "cadenas_iguales(" + a + ", " + b + ");";
            case ERROR_EJECUCION -> "error_ejecucion(" + a + ");";
            case FUNCION, FIN_FUNCION -> throw new IllegalStateException("Marcador de funcion suelto: " + q);
        };
    }

    private static String asignacion(String destino, String valor) {
        String linea = operando(destino) + " = " + valor + ";";
        return destino.equals("H") ? linea + " verificar_heap();" : linea;
    }

    private static String imprimir(String valor, String tipo) {
        return switch (tipo) {
            case "cadena" -> "imprimir_cadena(" + valor + ");";
            case "flotante" -> "imprimir_flotante(" + valor + ");";
            case "caracter" -> "imprimir_caracter(" + valor + ");";
            case "bool_y" -> "imprimir_bool(" + valor + ", \"verdadero\", \"falso\");";
            case "bool_z" -> "imprimir_bool(" + valor + ", \"true\", \"false\");";
            case "bool_pig" -> "imprimir_bool(" + valor + ", \"verum\", \"falsus\");";
            default -> "imprimir_entero(" + valor + ");";
        };
    }

    private static String aCadena(String valor, String tipo) {
        return switch (tipo) {
            case "flotante" -> "a_cadena_flotante(" + valor + ")";
            case "caracter" -> "a_cadena_caracter(" + valor + ")";
            case "bool_y" -> "a_cadena_bool(" + valor + ", \"verdadero\", \"falso\")";
            case "bool_z" -> "a_cadena_bool(" + valor + ", \"true\", \"false\")";
            case "bool_pig" -> "a_cadena_bool(" + valor + ", \"verum\", \"falsus\")";
            default -> "a_cadena_entero(" + valor + ")";
        };
    }

    private static String indice(String operando) {
        return operando.matches("\\d+") ? operando : "(int) " + operando(operando);
    }

    private static String operando(String operando) {
        return operando;
    }

    private static String nombreC(String funcion) {
        return "f_" + funcion;
    }

    private static boolean esTemporal(String operando) {
        return operando != null && operando.matches("t\\d+");
    }

    private static void marcarLeido(String operando, Set<String> leidos) {
        if (esTemporal(operando)) {
            leidos.add(operando);
        }
    }

    private static final String PLANTILLA = """
            /*
             * Generado por Contacto 3xtrat3rr3str3D a partir del codigo de tres direcciones.
             * Compilar:  gcc programa.c -o programa
             *
             * Memoria: stack y heap de celdas double. P es la base del marco de la
             * funcion en ejecucion y H la primera celda libre del heap. Una cadena es
             * un caracter por celda terminado en -1; heap[0] es la cadena vacia (null).
             */
            #include <stdio.h>
            #include <stdlib.h>
            #include <string.h>

            #define CELDAS 1000000
            #define LIMITE_PILA (CELDAS - 10000)
            #define FIN_CADENA (-1)
            #define LARGO_TEXTO 4096

            double stack[CELDAS];
            double heap[CELDAS];
            double P = 0;
            double H = 0;

            /* ------------------------- funciones del sistema ------------------------- */

            void error_texto(const char *mensaje) {
                printf("Error de ejecucion: %s\\n", mensaje);
                exit(1);
            }

            void verificar_pila(void) {
                if (P > LIMITE_PILA) error_texto("desbordamiento de pila (recursion demasiado profunda)");
            }

            void verificar_heap(void) {
                if (H >= CELDAS) error_texto("memoria del heap agotada");
            }

            double dividir(double a, double b) {
                if (b == 0) error_texto("division entre cero");
                return a / b;
            }

            double dividir_entero(double a, double b) {
                if (b == 0) error_texto("division entre cero");
                return (double) ((long long) a / (long long) b);
            }

            double modulo(double a, double b) {
                if (b == 0) error_texto("division entre cero");
                return (double) ((long long) a % (long long) b);
            }

            /* Flotante entero con ".0"; si no, hasta 6 decimales sin ceros sobrantes. */
            void formatear_flotante(double v, char *texto) {
                double absoluto = v < 0 ? -v : v;
                if (v == (double) (long long) v && absoluto < 1e15) {
                    sprintf(texto, "%.1f", v);
                    return;
                }
                sprintf(texto, "%.6f", v);
                size_t n = strlen(texto);
                while (n > 0 && texto[n - 1] == '0') texto[--n] = '\\0';
                if (n > 0 && texto[n - 1] == '.') { texto[n] = '0'; texto[n + 1] = '\\0'; }
            }

            /* Copia una cadena del heap a un arreglo de C (para convertirla). */
            void cadena_c(double puntero, char *texto) {
                int i = (int) puntero, n = 0;
                while (heap[i] != FIN_CADENA && n < LARGO_TEXTO - 1) texto[n++] = (char) heap[i++];
                texto[n] = '\\0';
            }

            double nueva_cadena(const char *texto) {
                double puntero = H;
                for (int i = 0; texto[i] != '\\0'; i++) { heap[(int) H] = (unsigned char) texto[i]; H = H + 1; verificar_heap(); }
                heap[(int) H] = FIN_CADENA; H = H + 1; verificar_heap();
                return puntero;
            }

            void imprimir_entero(double v)   { printf("%lld", (long long) v); }
            void imprimir_caracter(double v) { putchar((int) v); }
            void imprimir_bool(double v, const char *si, const char *no) { printf("%s", v != 0 ? si : no); }

            void imprimir_flotante(double v) {
                char texto[64];
                formatear_flotante(v, texto);
                printf("%s", texto);
            }

            void imprimir_cadena(double puntero) {
                for (int i = (int) puntero; heap[i] != FIN_CADENA; i++) putchar((int) heap[i]);
            }

            double a_cadena_entero(double v) {
                char texto[32];
                sprintf(texto, "%lld", (long long) v);
                return nueva_cadena(texto);
            }

            double a_cadena_flotante(double v) {
                char texto[64];
                formatear_flotante(v, texto);
                return nueva_cadena(texto);
            }

            double a_cadena_caracter(double v) {
                char texto[2] = { (char) v, '\\0' };
                return nueva_cadena(texto);
            }

            double a_cadena_bool(double v, const char *si, const char *no) {
                return nueva_cadena(v != 0 ? si : no);
            }

            double concatenar(double a, double b) {
                double puntero = H;
                for (int i = (int) a; heap[i] != FIN_CADENA; i++) { heap[(int) H] = heap[i]; H = H + 1; verificar_heap(); }
                for (int i = (int) b; heap[i] != FIN_CADENA; i++) { heap[(int) H] = heap[i]; H = H + 1; verificar_heap(); }
                heap[(int) H] = FIN_CADENA; H = H + 1; verificar_heap();
                return puntero;
            }

            double cadenas_iguales(double a, double b) {
                int i = (int) a, j = (int) b;
                while (heap[i] != FIN_CADENA && heap[i] == heap[j]) { i++; j++; }
                return heap[i] == heap[j];
            }

            double leer_cadena(void) {
                char texto[LARGO_TEXTO];
                if (fgets(texto, sizeof texto, stdin) == NULL) texto[0] = '\\0';
                texto[strcspn(texto, "\\r\\n")] = '\\0';
                return nueva_cadena(texto);
            }

            /* Quita los espacios de los extremos, como trim() de Java. */
            char *recortar(char *texto) {
                while (*texto == ' ' || *texto == '\\t') texto++;
                size_t n = strlen(texto);
                while (n > 0 && (texto[n - 1] == ' ' || texto[n - 1] == '\\t')) texto[--n] = '\\0';
                return texto;
            }

            /* Lo leido con "x <<" a un tipo no textual; si no se entiende, vale 0. */
            double convertir_flotante(double puntero) {
                char bruto[LARGO_TEXTO], *fin;
                cadena_c(puntero, bruto);
                char *texto = recortar(bruto);
                double valor = strtod(texto, &fin);
                return (fin == texto || *fin != '\\0') ? 0 : valor;
            }

            double convertir_entero(double puntero) {
                return (double) (long long) convertir_flotante(puntero);
            }

            double convertir_caracter(double puntero) {
                char bruto[LARGO_TEXTO];
                cadena_c(puntero, bruto);
                return (unsigned char) recortar(bruto)[0];
            }

            double convertir_bool(double puntero) {
                char bruto[LARGO_TEXTO];
                cadena_c(puntero, bruto);
                char *t = recortar(bruto);
                return strcmp(t, "verdadero") == 0 || strcmp(t, "true") == 0
                        || strcmp(t, "verum") == 0 || strcmp(t, "1") == 0;
            }

            void error_ejecucion(double mensaje) {
                imprimir_cadena(mensaje);
                putchar('\\n');
                exit(1);
            }

            """;
}
