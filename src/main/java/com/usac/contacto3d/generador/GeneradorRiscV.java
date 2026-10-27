package com.usac.contacto3d.generador;

import com.usac.contacto3d.c3d.Cuarteta;
import com.usac.contacto3d.c3d.ListaCuartetas;
import com.usac.contacto3d.c3d.Operacion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeneradorRiscV {

    public static final int CELDAS_HEAP = 1_000_000;
    public static final int CELDAS_PILA = 250_000;

    private static final String SANGRIA = "    ";

    private final StringBuilder texto = new StringBuilder();
    private final Map<String, String> constantes = new LinkedHashMap<>();
    private final Map<String, String> imagenes = new LinkedHashMap<>();
    private Map<String, Integer> temporales = Map.of();
    private String funcionActual;
    private List<String> numeradas = List.of();
    private boolean hayIndirectas;
    private int contadorEtiquetas;

    public String generar(ListaCuartetas programa) {
        numeradas = programa.getFunciones();
        arranque();
        for (Map.Entry<String, List<Cuarteta>> funcion : separarFunciones(programa.getCuartetas()).entrySet()) {
            traducirFuncion(funcion.getKey(), funcion.getValue());
        }
        texto.append(RUTINAS);

        StringBuilder s = new StringBuilder();
        s.append("# Generado por Contacto3D: RV32IMD, ecall con el servicio en a7 (RARS)\n");
        s.append(".data\n");
        s.append(".align 3\n");
        constantes.forEach((valor, etiqueta) -> s.append(etiqueta).append(": .double ").append(valor).append('\n'));
        s.append(DATOS);
        s.append(".align 2\n");
        imagenes.forEach((etiqueta, valores) -> s.append(etiqueta).append(": .word ").append(valores).append('\n'));
        if (hayIndirectas) {
            s.append("_funciones: .word ")
             .append(String.join(", ", numeradas.stream().map(GeneradorRiscV::etiquetaFuncion).toList()))
             .append('\n');
        }
        s.append("\n.text\n.globl _inicio\n");
        s.append(texto);
        return s.toString();
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

    private void arranque() {
        texto.append("_inicio:\n");
        emitir("li a0, " + CELDAS_HEAP * 8L);
        emitir("li a7, 9");
        emitir("ecall");
        emitir("mv s1, a0");
        emitir("li a0, " + CELDAS_PILA * 8L);
        emitir("li a7, 9");
        emitir("ecall");
        emitir("mv s2, a0");
        emitir("li s10, 0");
        emitir("li s11, 0");
        emitir("li s3, 0");
        emitir("fcvt.d.w fs0, zero");
        emitir("li t0, -1");
        emitir("fcvt.d.w fs1, t0");
        emitir("li t0, 1");
        emitir("fcvt.d.w fs2, t0");
        emitir("jal ra, " + etiquetaFuncion("main"));
        emitir("li a7, 10");
        emitir("ecall");
    }

    private void traducirFuncion(String nombre, List<Cuarteta> cuerpo) {
        funcionActual = nombre;
        temporales = new LinkedHashMap<>();
        for (Cuarteta c : cuerpo) {
            for (String operando : new String[] {c.arg1(), c.arg2(), c.resultado()}) {
                if (esTemporal(operando)) {
                    temporales.putIfAbsent(operando, 8 + 8 * temporales.size());
                }
            }
        }
        int marco = (8 + 8 * temporales.size() + 15) / 16 * 16;

        texto.append('\n').append(etiquetaFuncion(nombre)).append(":\n");
        ajustarSp(-marco);
        emitir("sw ra, 0(sp)");
        emitir("sw s0, 4(sp)");
        emitir("mv s0, sp");
        if (!temporales.isEmpty()) {
            String ciclo = nuevaEtiqueta();
            emitir("addi t0, s0, 8");
            emitir("li t1, " + temporales.size());
            texto.append(ciclo).append(":\n");
            emitir("fsd fs0, 0(t0)");
            emitir("addi t0, t0, 8");
            emitir("addi t1, t1, -1");
            emitir("bnez t1, " + ciclo);
        }

        for (int k = 0; k < cuerpo.size(); k++) {
            int fin = finDeBloqueConstante(cuerpo, k);
            if (fin - k >= 4) {
                copiarBloqueConstante(cuerpo.subList(k, fin));
                k = fin - 1;
                continue;
            }
            Cuarteta c = cuerpo.get(k);
            texto.append(SANGRIA).append("# ").append(c).append('\n');
            traducir(c);
        }

        texto.append(etiquetaFuncion(nombre)).append("_fin:\n");
        emitir("mv sp, s0");
        emitir("lw ra, 0(sp)");
        emitir("lw s0, 4(sp)");
        ajustarSp(marco);
        emitir("ret");
    }

    private void traducir(Cuarteta c) {
        String a = c.arg1();
        String b = c.arg2();
        String r = c.resultado();
        switch (c.operacion()) {
            case ASIGNAR -> {
                if (esRegistroEntero(r) && esEnteroPequenio(a, Integer.MIN_VALUE, Integer.MAX_VALUE)) {
                    emitir("li " + registroDe(r) + ", " + (long) Double.parseDouble(a));
                } else {
                    cargar(a, "ft0");
                    guardar(r, "ft0");
                }
            }
            case SUMA, RESTA -> {
                if (esRegistroEntero(r) && r.equals(a) && esEnteroPequenio(b, -2047, 2047)) {
                    long valor = (long) Double.parseDouble(b);
                    emitir("addi " + registroDe(r) + ", " + registroDe(r) + ", "
                            + (c.operacion() == Operacion.SUMA ? valor : -valor));
                } else {
                    binaria(a, b, r, c.operacion() == Operacion.SUMA ? "fadd.d ft2, ft0, ft1" : "fsub.d ft2, ft0, ft1");
                }
            }
            case MULTIPLICACION -> binaria(a, b, r, "fmul.d ft2, ft0, ft1");
            case DIVISION -> {
                cargar(a, "ft0");
                cargar(b, "ft1");
                verificarDivisor();
                emitir("fdiv.d ft2, ft0, ft1");
                guardar(r, "ft2");
            }
            case DIVISION_ENTERA, MODULO -> {
                cargar(a, "ft0");
                cargar(b, "ft1");
                verificarDivisor();
                emitir("fcvt.w.d t1, ft0, rtz");
                emitir("fcvt.w.d t2, ft1, rtz");
                emitir((c.operacion() == Operacion.MODULO ? "rem" : "div") + " t1, t1, t2");
                emitir("fcvt.d.w ft2, t1");
                guardar(r, "ft2");
            }
            case NEGATIVO -> {
                cargar(a, "ft0");
                emitir("fneg.d ft2, ft0");
                guardar(r, "ft2");
            }
            case MENOR -> comparacion(a, b, r, "flt.d t0, ft0, ft1", false);
            case MAYOR -> comparacion(a, b, r, "flt.d t0, ft1, ft0", false);
            case MENOR_IGUAL -> comparacion(a, b, r, "fle.d t0, ft0, ft1", false);
            case MAYOR_IGUAL -> comparacion(a, b, r, "fle.d t0, ft1, ft0", false);
            case IGUAL -> comparacion(a, b, r, "feq.d t0, ft0, ft1", false);
            case DIFERENTE -> comparacion(a, b, r, "feq.d t0, ft0, ft1", true);
            case NOT -> {
                cargar(a, "ft0");
                emitir("feq.d t0, ft0, fs0");
                emitir("fcvt.d.w ft2, t0");
                guardar(r, "ft2");
            }
            case LEER_STACK, LEER_HEAP -> {
                direccion(a, c.operacion() == Operacion.LEER_STACK ? "s2" : "s1");
                emitir("fld ft2, 0(t0)");
                guardar(r, "ft2");
            }
            case ESCRIBIR_STACK, ESCRIBIR_HEAP -> {
                cargar(b, "ft1");
                direccion(a, c.operacion() == Operacion.ESCRIBIR_STACK ? "s2" : "s1");
                emitir("fsd ft1, 0(t0)");
            }
            case ETIQUETA -> texto.append(r).append(":\n");
            case GOTO -> emitir("j " + r);
            case IF_TRUE, IF_FALSE -> {
                cargar(a, "ft0");
                emitir("feq.d t0, ft0, fs0");
                String salto = nuevaEtiqueta();
                emitir((c.operacion() == Operacion.IF_TRUE ? "bnez" : "beqz") + " t0, " + salto);
                emitir("j " + r);
                texto.append(salto).append(":\n");
            }
            case CALL -> emitir("jal ra, " + etiquetaFuncion(a));
            case DIRECCION_FUNCION -> {
                cargar(String.valueOf(numeradas.indexOf(a)), "ft0");
                guardar(r, "ft0");
            }
            case CALL_INDIRECTO -> {
                hayIndirectas = true;
                cargar(a, "ft0");
                emitir("fcvt.w.d t0, ft0, rtz");
                emitir("slli t0, t0, 2");
                emitir("la t1, _funciones");
                emitir("add t0, t0, t1");
                emitir("lw t0, 0(t0)");
                emitir("jalr ra, t0, 0");
            }
            case RETURN -> emitir("j " + etiquetaFuncion(funcionActual) + "_fin");
            case HALT -> {
                emitir("li a7, 10");
                emitir("ecall");
            }
            case IMPRIMIR -> {
                cargar(a, "ft0");
                emitirValor(b);
            }
            case SALTO_LINEA -> {
                emitir("li a0, 10");
                emitir("jal ra, _emitir_car");
            }
            case LEER_CADENA -> {
                emitir("jal ra, _leer_cadena");
                emitir("fcvt.d.w ft2, a0");
                guardar(r, "ft2");
            }
            case CONCATENAR -> {
                cargar(a, "ft0");
                emitir("fcvt.w.d a1, ft0, rtz");
                cargar(b, "ft1");
                emitir("fcvt.w.d a2, ft1, rtz");
                emitir("jal ra, _concatenar");
                emitir("fcvt.d.w ft2, a0");
                guardar(r, "ft2");
            }
            case A_CADENA -> {
                cargar(a, "ft0");
                emitir("mv s4, s11");
                emitir("li s3, 1");
                emitirValor(b);
                emitir("li a0, -1");
                emitir("jal ra, _emitir_car");
                emitir("li s3, 0");
                emitir("fcvt.d.w ft2, s4");
                guardar(r, "ft2");
            }
            case CONVERTIR_CADENA -> {
                cargar(a, "ft0");
                emitir("fcvt.w.d a1, ft0, rtz");
                switch (b) {
                    case "entero" -> {
                        emitir("jal ra, _parsear_numero");
                        emitir("fcvt.w.d t0, fa0, rtz");
                        emitir("fcvt.d.w ft2, t0");
                    }
                    case "flotante" -> {
                        emitir("jal ra, _parsear_numero");
                        emitir("fmv.d ft2, fa0");
                    }
                    case "caracter" -> {
                        emitir("jal ra, _primer_caracter");
                        emitir("fcvt.d.w ft2, a0");
                    }
                    case "bool" -> {
                        emitir("jal ra, _parsear_bool");
                        emitir("fcvt.d.w ft2, a0");
                    }
                    default -> emitir("fmv.d ft2, fs0");
                }
                guardar(r, "ft2");
            }
            case IGUAL_CADENAS -> {
                cargar(a, "ft0");
                emitir("fcvt.w.d a1, ft0, rtz");
                cargar(b, "ft1");
                emitir("fcvt.w.d a2, ft1, rtz");
                emitir("jal ra, _iguales_cadenas");
                emitir("fcvt.d.w ft2, a0");
                guardar(r, "ft2");
            }
            case ERROR_EJECUCION -> {
                cargar(a, "ft0");
                emitir("fcvt.w.d a1, ft0, rtz");
                emitir("j _error_ejecucion");
            }
            default -> throw new IllegalStateException("Cuarteta sin traduccion: " + c);
        }
    }

    private static int finDeBloqueConstante(List<Cuarteta> cuerpo, int desde) {
        int fin = desde;
        while (fin < cuerpo.size()) {
            Cuarteta c = cuerpo.get(fin);
            if (c.operacion() != Operacion.ESCRIBIR_HEAP
                    || !esEnteroPequenio(c.arg1(), 0, Integer.MAX_VALUE / 8)
                    || !esEnteroPequenio(c.arg2(), Integer.MIN_VALUE, Integer.MAX_VALUE)
                    || fin > desde && Double.parseDouble(c.arg1())
                            != Double.parseDouble(cuerpo.get(fin - 1).arg1()) + 1) {
                break;
            }
            fin++;
        }
        return fin;
    }

    private void copiarBloqueConstante(List<Cuarteta> bloque) {
        String etiqueta = "_imagen" + imagenes.size();
        StringBuilder valores = new StringBuilder();
        for (int k = 0; k < bloque.size(); k++) {
            valores.append(k % 16 == 0 ? (k == 0 ? "" : "\n.word ") : ", ")
                    .append((long) Double.parseDouble(bloque.get(k).arg2()));
        }
        imagenes.put(etiqueta, valores.toString());

        long inicio = (long) Double.parseDouble(bloque.get(0).arg1());
        texto.append(SANGRIA).append("# heap[").append(inicio).append("..").append(inicio + bloque.size() - 1)
                .append("] = ").append(etiqueta).append('\n');
        String ciclo = nuevaEtiqueta();
        emitir("la t0, " + etiqueta);
        emitir("li t1, " + inicio * 8);
        emitir("add t1, t1, s1");
        emitir("li t2, " + bloque.size());
        texto.append(ciclo).append(":\n");
        emitir("lw t3, 0(t0)");
        emitir("fcvt.d.w ft0, t3");
        emitir("fsd ft0, 0(t1)");
        emitir("addi t0, t0, 4");
        emitir("addi t1, t1, 8");
        emitir("addi t2, t2, -1");
        emitir("bnez t2, " + ciclo);
    }

    private void emitirValor(String tipo) {
        switch (tipo) {
            case "cadena" -> {
                emitir("fcvt.w.d a1, ft0, rtz");
                emitir("jal ra, _emitir_cadena");
            }
            case "flotante" -> {
                emitir("fmv.d fa0, ft0");
                emitir("jal ra, _emitir_flotante");
            }
            case "caracter" -> {
                emitir("fcvt.w.d a0, ft0, rtz");
                emitir("jal ra, _emitir_car");
            }
            case "bool_y", "bool_z", "bool_pig" -> {
                String sufijo = tipo.substring(5);
                String listo = nuevaEtiqueta();
                emitir("feq.d t0, ft0, fs0");
                emitir("la a1, _txt_verdadero_" + sufijo);
                emitir("beqz t0, " + listo);
                emitir("la a1, _txt_falso_" + sufijo);
                texto.append(listo).append(":\n");
                emitir("jal ra, _emitir_asciz");
            }
            default -> {
                emitir("fmv.d fa0, ft0");
                emitir("jal ra, _emitir_entero");
            }
        }
    }

    private void binaria(String a, String b, String r, String instruccion) {
        cargar(a, "ft0");
        cargar(b, "ft1");
        emitir(instruccion);
        guardar(r, "ft2");
    }

    private void comparacion(String a, String b, String r, String instruccion, boolean negar) {
        cargar(a, "ft0");
        cargar(b, "ft1");
        emitir(instruccion);
        if (negar) {
            emitir("xori t0, t0, 1");
        }
        emitir("fcvt.d.w ft2, t0");
        guardar(r, "ft2");
    }

    private void verificarDivisor() {
        emitir("feq.d t0, ft1, fs0");
        String sigue = nuevaEtiqueta();
        emitir("beqz t0, " + sigue);
        emitir("j _error_division");
        texto.append(sigue).append(":\n");
    }

    private void direccion(String operando, String base) {
        if (esEnteroPequenio(operando, 0, Integer.MAX_VALUE / 8)) {
            emitir("li t0, " + (long) Double.parseDouble(operando) * 8);
        } else if (esRegistroEntero(operando)) {
            emitir("slli t0, " + registroDe(operando) + ", 3");
        } else {
            cargar(operando, "ft0");
            emitir("fcvt.w.d t0, ft0, rtz");
            emitir("slli t0, t0, 3");
        }
        emitir("add t0, t0, " + base);
    }

    private void cargar(String operando, String registro) {
        if (esRegistroEntero(operando)) {
            emitir("fcvt.d.w " + registro + ", " + registroDe(operando));
        } else if (esTemporal(operando)) {
            accesoMarco("fld", registro, temporales.get(operando));
        } else if (esEnteroPequenio(operando, Integer.MIN_VALUE, Integer.MAX_VALUE)) {
            long valor = (long) Double.parseDouble(operando);
            if (valor == 0) {
                emitir("fmv.d " + registro + ", fs0");
            } else {
                emitir("li t0, " + valor);
                emitir("fcvt.d.w " + registro + ", t0");
            }
        } else {
            String etiqueta = constantes.computeIfAbsent(String.valueOf(Double.parseDouble(operando)),
                    v -> "_c" + constantes.size());
            emitir("la t0, " + etiqueta);
            emitir("fld " + registro + ", 0(t0)");
        }
    }

    private void guardar(String destino, String registro) {
        if (esRegistroEntero(destino)) {
            emitir("fcvt.w.d " + registroDe(destino) + ", " + registro + ", rtz");
        } else if (esTemporal(destino)) {
            accesoMarco("fsd", registro, temporales.get(destino));
        } else {
            throw new IllegalStateException("Destino invalido en " + funcionActual + ": " + destino);
        }
    }

    private void accesoMarco(String instruccion, String registro, int desplazamiento) {
        if (desplazamiento < 2048) {
            emitir(instruccion + " " + registro + ", " + desplazamiento + "(s0)");
        } else {
            emitir("li t5, " + desplazamiento);
            emitir("add t5, s0, t5");
            emitir(instruccion + " " + registro + ", 0(t5)");
        }
    }

    private void ajustarSp(int bytes) {
        if (bytes >= -2048 && bytes < 2048) {
            emitir("addi sp, sp, " + bytes);
        } else {
            emitir("li t0, " + bytes);
            emitir("add sp, sp, t0");
        }
    }

    private void emitir(String instruccion) {
        texto.append(SANGRIA).append(instruccion).append('\n');
    }

    private String nuevaEtiqueta() {
        return "_r" + contadorEtiquetas++;
    }

    private static String etiquetaFuncion(String nombre) {
        return "F_" + nombre.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private static boolean esTemporal(String operando) {
        return operando != null && operando.length() > 1 && operando.charAt(0) == 't';
    }

    private static boolean esRegistroEntero(String operando) {
        return "P".equals(operando) || "H".equals(operando);
    }

    private static String registroDe(String operando) {
        return operando.equals("P") ? "s10" : "s11";
    }

    private static boolean esEnteroPequenio(String operando, long minimo, long maximo) {
        if (operando == null || esRegistroEntero(operando) || esTemporal(operando)) {
            return false;
        }
        double valor = Double.parseDouble(operando);
        return valor == Math.rint(valor) && valor >= minimo && valor <= maximo
                && !(valor == 0 && 1 / valor < 0);
    }

    private static final String DATOS = """
            _c_2e52: .double 4503599627370496.0
            _c_1e15: .double 1000000000000000.0
            _c_1e6: .double 1000000.0
            _txt_verdadero_y: .asciz "verdadero"
            _txt_falso_y: .asciz "falso"
            _txt_verdadero_z: .asciz "true"
            _txt_falso_z: .asciz "false"
            _txt_verdadero_pig: .asciz "verum"
            _txt_falso_pig: .asciz "falsus"
            _txt_nan: .asciz "nan"
            _txt_inf: .asciz "inf"
            _txt_punto_cero: .asciz ".0"
            _txt_uno: .asciz "1"
            _txt_division: .asciz "Error de ejecucion: division entre cero\\n"
            _buffer: .space 1024
            """;

    private static final String RUTINAS = """

            # ---------------------------------------------------------------- rutinas de apoyo

            # a0 = caracter. Lo imprime (s3 = 0) o lo agrega en heap[H] (s3 = 1).
            _emitir_car:
                bnez s3, _emitir_car_heap
                li a7, 11
                ecall
                ret
            _emitir_car_heap:
                fcvt.d.w ft10, a0
                slli t6, s11, 3
                add t6, t6, s1
                fsd ft10, 0(t6)
                addi s11, s11, 1
                ret

            # a1 = direccion de un texto terminado en NUL (.asciz)
            _emitir_asciz:
                addi sp, sp, -16
                sw ra, 0(sp)
                mv t0, a1
            _emitir_asciz_ciclo:
                lbu a0, 0(t0)
                beqz a0, _emitir_asciz_fin
                jal ra, _emitir_car
                addi t0, t0, 1
                j _emitir_asciz_ciclo
            _emitir_asciz_fin:
                lw ra, 0(sp)
                addi sp, sp, 16
                ret

            # a1 = celda del heap donde empieza una cadena terminada en -1
            _emitir_cadena:
                addi sp, sp, -16
                sw ra, 0(sp)
                slli t0, a1, 3
                add t0, t0, s1
            _emitir_cadena_ciclo:
                fld ft0, 0(t0)
                feq.d t1, ft0, fs1
                bnez t1, _emitir_cadena_fin
                fcvt.w.d a0, ft0, rtz
                jal ra, _emitir_car
                addi t0, t0, 8
                j _emitir_cadena_ciclo
            _emitir_cadena_fin:
                lw ra, 0(sp)
                addi sp, sp, 16
                ret

            # fa0 >= 0  ->  fa0 = redondeo al entero mas cercano (empates al par). Usa t6, ft8, ft9.
            _redondear:
                la t6, _c_2e52
                fld ft8, 0(t6)
                fle.d t6, ft8, fa0
                bnez t6, _redondear_fin
                fadd.d ft9, fa0, ft8
                fsub.d fa0, ft9, ft8
            _redondear_fin:
                ret

            # fa0 >= 0  ->  fa0 = piso(fa0). Usa t6, ft8, ft9.
            _piso:
                la t6, _c_2e52
                fld ft8, 0(t6)
                fle.d t6, ft8, fa0
                bnez t6, _piso_fin
                fadd.d ft9, fa0, ft8
                fsub.d ft9, ft9, ft8
                fle.d t6, ft9, fa0
                bnez t6, _piso_listo
                fsub.d ft9, ft9, fs2
            _piso_listo:
                fmv.d fa0, ft9
            _piso_fin:
                ret

            # fa0 = entero >= 0 guardado como double. Emite sus digitos decimales.
            _emitir_digitos:
                addi sp, sp, -352
                sw ra, 0(sp)
                addi t3, sp, 8
                mv t2, t3
                addi t4, sp, 340
                li t0, 10
                fcvt.d.w fa2, t0
                fmv.d fa3, fa0
            _emitir_digitos_ciclo:
                fdiv.d fa0, fa3, fa2
                jal ra, _piso
                fmul.d ft0, fa0, fa2
                fsub.d ft0, fa3, ft0
                flt.d t0, ft0, fs0
                beqz t0, _emitir_digitos_alto
                fsub.d fa0, fa0, fs2
                fadd.d ft0, ft0, fa2
            _emitir_digitos_alto:
                fle.d t0, fa2, ft0
                beqz t0, _emitir_digitos_guardar
                fadd.d fa0, fa0, fs2
                fsub.d ft0, ft0, fa2
            _emitir_digitos_guardar:
                fcvt.w.d t0, ft0, rtz
                sb t0, 0(t2)
                addi t2, t2, 1
                fmv.d fa3, fa0
                feq.d t0, fa3, fs0
                bnez t0, _emitir_digitos_salida
                bltu t2, t4, _emitir_digitos_ciclo
            _emitir_digitos_salida:
                addi t2, t2, -1
                lbu a0, 0(t2)
                addi a0, a0, 48
                jal ra, _emitir_car
                bne t2, t3, _emitir_digitos_salida
                lw ra, 0(sp)
                addi sp, sp, 352
                ret

            # fa0 = valor. Emite (long) valor, igual que String.valueOf((long) valor).
            _emitir_entero:
                addi sp, sp, -16
                sw ra, 0(sp)
                feq.d t0, fa0, fa0
                bnez t0, _emitir_entero_numero
                fmv.d fa0, fs0
            _emitir_entero_numero:
                flt.d t0, fa0, fs0
                beqz t0, _emitir_entero_positivo
                fneg.d fa0, fa0
                jal ra, _piso
                feq.d t0, fa0, fs0
                bnez t0, _emitir_entero_digitos
                li a0, 45
                jal ra, _emitir_car
                j _emitir_entero_digitos
            _emitir_entero_positivo:
                jal ra, _piso
            _emitir_entero_digitos:
                jal ra, _emitir_digitos
                lw ra, 0(sp)
                addi sp, sp, 16
                ret

            # fa0 = valor. Mismo formato que InterpreteCuartetas.formatearFlotante:
            # enteros con ".0", el resto redondeado a 6 decimales sin ceros al final.
            _emitir_flotante:
                addi sp, sp, -16
                sw ra, 0(sp)
                fclass.d t0, fa0
                andi t1, t0, 768
                beqz t1, _emitir_flotante_no_nan
                la a1, _txt_nan
                jal ra, _emitir_asciz
                j _emitir_flotante_fin
            _emitir_flotante_no_nan:
                andi t1, t0, 15
                beqz t1, _emitir_flotante_signo
                li a0, 45
                jal ra, _emitir_car
            _emitir_flotante_signo:
                andi t1, t0, 129
                beqz t1, _emitir_flotante_finito
                la a1, _txt_inf
                jal ra, _emitir_asciz
                j _emitir_flotante_fin
            _emitir_flotante_finito:
                fabs.d fa4, fa0
                fmv.d fa0, fa4
                jal ra, _redondear
                feq.d t0, fa0, fa4
                beqz t0, _emitir_flotante_decimales
                la t0, _c_1e15
                fld ft0, 0(t0)
                flt.d t0, fa4, ft0
                beqz t0, _emitir_flotante_decimales
                fmv.d fa0, fa4
                jal ra, _emitir_digitos
                la a1, _txt_punto_cero
                jal ra, _emitir_asciz
                j _emitir_flotante_fin
            _emitir_flotante_decimales:
                la t0, _c_1e6
                fld fa5, 0(t0)
                fmul.d fa0, fa4, fa5
                jal ra, _redondear
                fmv.d fa6, fa0
                fdiv.d fa0, fa6, fa5
                jal ra, _piso
                fmv.d fa7, fa0
                fmul.d ft0, fa7, fa5
                fsub.d fa6, fa6, ft0
                flt.d t0, fa6, fs0
                beqz t0, _emitir_flotante_alto
                fsub.d fa7, fa7, fs2
                fadd.d fa6, fa6, fa5
            _emitir_flotante_alto:
                fle.d t0, fa5, fa6
                beqz t0, _emitir_flotante_entera
                fadd.d fa7, fa7, fs2
                fsub.d fa6, fa6, fa5
            _emitir_flotante_entera:
                fmv.d fa0, fa7
                jal ra, _emitir_digitos
                li a0, 46
                jal ra, _emitir_car
                fcvt.w.d t4, fa6, rtz
                bnez t4, _emitir_flotante_recortar
                li a0, 48
                jal ra, _emitir_car
                j _emitir_flotante_fin
            _emitir_flotante_recortar:
                li t0, 10
                li t5, 6
            _emitir_flotante_ceros:
                rem t1, t4, t0
                bnez t1, _emitir_flotante_potencia
                div t4, t4, t0
                addi t5, t5, -1
                j _emitir_flotante_ceros
            _emitir_flotante_potencia:
                li t1, 1
            _emitir_flotante_potencia_ciclo:
                addi t5, t5, -1
                beqz t5, _emitir_flotante_fraccion
                mul t1, t1, t0
                j _emitir_flotante_potencia_ciclo
            _emitir_flotante_fraccion:
                div a0, t4, t1
                rem t4, t4, t1
                addi a0, a0, 48
                jal ra, _emitir_car
                div t1, t1, t0
                bnez t1, _emitir_flotante_fraccion
            _emitir_flotante_fin:
                lw ra, 0(sp)
                addi sp, sp, 16
                ret

            # a1 = cadena a, a2 = cadena b  ->  a0 = nueva cadena a + b en el heap
            _concatenar:
                addi sp, sp, -16
                sw ra, 0(sp)
                sw a2, 4(sp)
                sw s11, 8(sp)
                li s3, 1
                jal ra, _emitir_cadena
                lw a1, 4(sp)
                jal ra, _emitir_cadena
                li a0, -1
                jal ra, _emitir_car
                li s3, 0
                lw a0, 8(sp)
                lw ra, 0(sp)
                addi sp, sp, 16
                ret

            # a1, a2 = cadenas  ->  a0 = 1 si son iguales
            _iguales_cadenas:
                slli t0, a1, 3
                add t0, t0, s1
                slli t1, a2, 3
                add t1, t1, s1
            _iguales_cadenas_ciclo:
                fld ft0, 0(t0)
                fld ft1, 0(t1)
                feq.d t2, ft0, ft1
                beqz t2, _iguales_cadenas_no
                feq.d t2, ft0, fs1
                bnez t2, _iguales_cadenas_si
                addi t0, t0, 8
                addi t1, t1, 8
                j _iguales_cadenas_ciclo
            _iguales_cadenas_si:
                li a0, 1
                ret
            _iguales_cadenas_no:
                li a0, 0
                ret

            # Lee una linea de la entrada  ->  a0 = nueva cadena en el heap, sin el salto de linea
            _leer_cadena:
                addi sp, sp, -16
                sw ra, 0(sp)
                sw s11, 4(sp)
                la a0, _buffer
                sb zero, 0(a0)
                li a1, 1024
                li a7, 8
                ecall
                li s3, 1
                la t0, _buffer
            _leer_cadena_ciclo:
                lbu a0, 0(t0)
                beqz a0, _leer_cadena_fin
                li t1, 10
                beq a0, t1, _leer_cadena_fin
                li t1, 13
                beq a0, t1, _leer_cadena_fin
                li t1, 192
                bltu a0, t1, _leer_cadena_agregar
                li t1, 224
                bgeu a0, t1, _leer_cadena_agregar
                lbu t1, 1(t0)
                andi a0, a0, 31
                slli a0, a0, 6
                andi t1, t1, 63
                or a0, a0, t1
                addi t0, t0, 1
            _leer_cadena_agregar:
                jal ra, _emitir_car
                addi t0, t0, 1
                j _leer_cadena_ciclo
            _leer_cadena_fin:
                li a0, -1
                jal ra, _emitir_car
                li s3, 0
                lw a0, 4(sp)
                lw ra, 0(sp)
                addi sp, sp, 16
                ret

            # a1 = cadena  ->  t0 = direccion del primer caracter > ' ', t1 = direccion despues del ultimo
            _recortar:
                slli t0, a1, 3
                add t0, t0, s1
                mv t1, t0
            _recortar_fin_cadena:
                fld ft0, 0(t1)
                feq.d t2, ft0, fs1
                bnez t2, _recortar_atras
                addi t1, t1, 8
                j _recortar_fin_cadena
            _recortar_atras:
                li t3, 32
            _recortar_inicio:
                beq t0, t1, _recortar_listo
                fld ft0, 0(t0)
                fcvt.w.d t2, ft0, rtz
                bgt t2, t3, _recortar_final
                addi t0, t0, 8
                j _recortar_inicio
            _recortar_final:
                fld ft0, -8(t1)
                fcvt.w.d t2, ft0, rtz
                bgt t2, t3, _recortar_listo
                addi t1, t1, -8
                j _recortar_final
            _recortar_listo:
                ret

            # a1 = cadena  ->  fa0 = su valor numerico, o 0 si no es un numero valido
            _parsear_numero:
                addi sp, sp, -16
                sw ra, 0(sp)
                jal ra, _recortar
                fmv.d fa0, fs0
                li t2, 0
                li t3, 0
                li t4, 0
                li t5, 0
                li a3, 0
                li t6, 10
                fcvt.d.w fa2, t6
                beq t0, t1, _parsear_numero_falla
                fld ft0, 0(t0)
                fcvt.w.d a0, ft0, rtz
                li t6, 45
                bne a0, t6, _parsear_numero_mas
                li t2, 1
                addi t0, t0, 8
                j _parsear_numero_ciclo
            _parsear_numero_mas:
                li t6, 43
                bne a0, t6, _parsear_numero_ciclo
                addi t0, t0, 8
            _parsear_numero_ciclo:
                beq t0, t1, _parsear_numero_listo
                fld ft0, 0(t0)
                fcvt.w.d a0, ft0, rtz
                addi t0, t0, 8
                li t6, 46
                bne a0, t6, _parsear_numero_digito
                bnez t5, _parsear_numero_falla
                li t5, 1
                j _parsear_numero_ciclo
            _parsear_numero_digito:
                addi a0, a0, -48
                li t6, 10
                bgeu a0, t6, _parsear_numero_exponente
                fmul.d fa0, fa0, fa2
                fcvt.d.w ft1, a0
                fadd.d fa0, fa0, ft1
                addi t3, t3, 1
                beqz t5, _parsear_numero_ciclo
                addi t4, t4, 1
                j _parsear_numero_ciclo
            _parsear_numero_exponente:
                beqz t3, _parsear_numero_falla
                addi a0, a0, 48
                ori a0, a0, 32
                li t6, 101
                bne a0, t6, _parsear_numero_falla
                beq t0, t1, _parsear_numero_falla
                li a4, 0
                fld ft0, 0(t0)
                fcvt.w.d a0, ft0, rtz
                li t6, 45
                bne a0, t6, _parsear_numero_exp_mas
                li a4, 1
                addi t0, t0, 8
                j _parsear_numero_exp_digitos
            _parsear_numero_exp_mas:
                li t6, 43
                bne a0, t6, _parsear_numero_exp_digitos
                addi t0, t0, 8
            _parsear_numero_exp_digitos:
                beq t0, t1, _parsear_numero_falla
            _parsear_numero_exp_ciclo:
                fld ft0, 0(t0)
                fcvt.w.d a0, ft0, rtz
                addi a0, a0, -48
                li t6, 10
                bgeu a0, t6, _parsear_numero_falla
                li t6, 10
                mul a3, a3, t6
                add a3, a3, a0
                addi t0, t0, 8
                bne t0, t1, _parsear_numero_exp_ciclo
                beqz a4, _parsear_numero_listo
                neg a3, a3
            _parsear_numero_listo:
                beqz t3, _parsear_numero_falla
                sub a3, a3, t4
                fmv.d ft1, fs2
                mv t6, a3
                bgez t6, _parsear_numero_potencia
                neg t6, t6
            _parsear_numero_potencia:
                beqz t6, _parsear_numero_escalar
                fmul.d ft1, ft1, fa2
                addi t6, t6, -1
                j _parsear_numero_potencia
            _parsear_numero_escalar:
                bltz a3, _parsear_numero_dividir
                fmul.d fa0, fa0, ft1
                j _parsear_numero_signo
            _parsear_numero_dividir:
                fdiv.d fa0, fa0, ft1
            _parsear_numero_signo:
                beqz t2, _parsear_numero_fin
                fneg.d fa0, fa0
                j _parsear_numero_fin
            _parsear_numero_falla:
                fmv.d fa0, fs0
            _parsear_numero_fin:
                lw ra, 0(sp)
                addi sp, sp, 16
                ret

            # a1 = cadena  ->  a0 = su primer caracter sin espacios, o 0 si esta vacia
            _primer_caracter:
                addi sp, sp, -16
                sw ra, 0(sp)
                jal ra, _recortar
                li a0, 0
                beq t0, t1, _primer_caracter_fin
                fld ft0, 0(t0)
                fcvt.w.d a0, ft0, rtz
            _primer_caracter_fin:
                lw ra, 0(sp)
                addi sp, sp, 16
                ret

            # a1 = cadena  ->  a0 = 1 si es verdadero, true, verum o 1
            _parsear_bool:
                addi sp, sp, -16
                sw ra, 0(sp)
                jal ra, _recortar
                la a2, _txt_verdadero_y
                jal ra, _comparar_rango
                bnez a0, _parsear_bool_fin
                la a2, _txt_verdadero_z
                jal ra, _comparar_rango
                bnez a0, _parsear_bool_fin
                la a2, _txt_verdadero_pig
                jal ra, _comparar_rango
                bnez a0, _parsear_bool_fin
                la a2, _txt_uno
                jal ra, _comparar_rango
            _parsear_bool_fin:
                lw ra, 0(sp)
                addi sp, sp, 16
                ret

            # celdas [t0, t1) contra el texto NUL en a2  ->  a0 = 1 si coinciden. Conserva t0 y t1.
            _comparar_rango:
                mv t2, t0
            _comparar_rango_ciclo:
                lbu t3, 0(a2)
                beq t2, t1, _comparar_rango_final
                beqz t3, _comparar_rango_no
                fld ft0, 0(t2)
                fcvt.w.d t4, ft0, rtz
                bne t3, t4, _comparar_rango_no
                addi t2, t2, 8
                addi a2, a2, 1
                j _comparar_rango_ciclo
            _comparar_rango_final:
                bnez t3, _comparar_rango_no
                li a0, 1
                ret
            _comparar_rango_no:
                li a0, 0
                ret

            # a1 = mensaje (cadena del heap). Lo imprime y termina el programa.
            _error_ejecucion:
                li s3, 0
                jal ra, _emitir_cadena
                li a0, 10
                jal ra, _emitir_car
                li a7, 10
                ecall

            _error_division:
                li s3, 0
                la a1, _txt_division
                jal ra, _emitir_asciz
                li a7, 10
                ecall
            """;
}
