package com.usac.contacto3d.riscv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Ejecuta un programa ensamblado. Imita la distribucion de memoria y los servicios ecall de RARS,
 * para que la salida coincida con la del simulador externo.
 */
public class Simulador {

    public static final long MAXIMO_INSTRUCCIONES = 1_000_000_000L;

    private static final int BASE_HEAP = 0x1004_0000;
    private static final int MAXIMO_HEAP = 256 * 1024 * 1024;
    private static final int TOPE_PILA = 0x7fff_effc;
    private static final int TAMANIO_PILA = 16 * 1024 * 1024;
    private static final int BASE_PILA = TOPE_PILA + 4 - TAMANIO_PILA;
    private static final int TAMANIO_DATOS = BASE_HEAP - Ensamblador.BASE_DATOS;

    public static class ErrorEjecucion extends RuntimeException {
        public ErrorEjecucion(String mensaje) {
            super(mensaje);
        }
    }

    private static class Fin extends RuntimeException {
        Fin() {
            super(null, null, false, false);
        }
    }

    private final List<Instruccion> codigo;
    private final int[] x = new int[32];
    private final long[] f = new long[32];
    private final byte[] datos = new byte[TAMANIO_DATOS];
    private byte[] heap = new byte[1 << 20];
    private int finHeap = BASE_HEAP;
    private final byte[] pila = new byte[TAMANIO_PILA];
    private int pc = Ensamblador.BASE_TEXTO;

    private final StringBuilder salida = new StringBuilder();
    private final BufferedReader entrada;
    private long instrucciones;
    private volatile boolean detenido;

    public Simulador(Ensamblador.Programa programa, String entrada) {
        this.codigo = programa.instrucciones();
        if (programa.datos().length > TAMANIO_DATOS) {
            throw new ErrorEjecucion("el segmento .data no cabe en memoria");
        }
        System.arraycopy(programa.datos(), 0, datos, 0, programa.datos().length);
        this.entrada = new BufferedReader(new StringReader(entrada == null ? "" : entrada));
        Integer inicio = programa.etiquetas().get("_inicio");
        if (inicio != null) {
            pc = inicio;
        }
        x[2] = TOPE_PILA;
        x[3] = 0x1000_8000;
    }

    /** Ensambla y ejecuta. Devuelve la salida; un error de ejecucion se agrega al final. */
    public static String ejecutar(String ensamblador, String entrada) {
        Ensamblador.Programa programa;
        try {
            programa = Ensamblador.ensamblar(ensamblador);
        } catch (Ensamblador.ErrorEnsamblado e) {
            return "Error de ensamblado: " + e.getMessage() + "\n";
        }
        Simulador simulador = new Simulador(programa, entrada);
        simulador.ejecutar();
        return simulador.getSalida();
    }

    public void ejecutar() {
        try {
            correr();
        } catch (Fin fin) {
        } catch (ErrorEjecucion e) {
            salida.append("\nError de ejecucion (RISC-V): ").append(e.getMessage()).append('\n');
        }
    }

    public void detener() {
        detenido = true;
    }

    public String getSalida() {
        return salida.toString();
    }

    public long getInstruccionesEjecutadas() {
        return instrucciones;
    }

    private void correr() {
        while (true) {
            int indice = (pc - Ensamblador.BASE_TEXTO) >>> 2;
            if (pc < Ensamblador.BASE_TEXTO || (pc & 3) != 0 || indice >= codigo.size()) {
                throw new ErrorEjecucion(String.format("el contador de programa salio del codigo (pc = 0x%08x)", pc));
            }
            if (++instrucciones > MAXIMO_INSTRUCCIONES) {
                throw new ErrorEjecucion("se supero el limite de " + MAXIMO_INSTRUCCIONES
                        + " instrucciones (posible bucle infinito)");
            }
            if (detenido) {
                throw new ErrorEjecucion("ejecucion detenida por el usuario");
            }
            Instruccion i = codigo.get(indice);
            try {
                paso(i);
            } catch (ErrorEjecucion e) {
                throw new ErrorEjecucion(e.getMessage() + " (linea " + i.linea() + ": " + i.fuente() + ")");
            }
            x[0] = 0;
        }
    }

    private void paso(Instruccion i) {
        int siguiente = pc + 4;
        int a = x[i.rs1()];
        int b = x[i.rs2()];
        switch (i.op()) {
            case LI, LUI -> x[i.rd()] = i.imm();
            case AUIPC -> x[i.rd()] = i.imm();
            case ADD -> x[i.rd()] = a + b;
            case SUB -> x[i.rd()] = a - b;
            case MUL -> x[i.rd()] = a * b;
            case MULH -> x[i.rd()] = (int) (((long) a * (long) b) >> 32);
            case MULHU -> x[i.rd()] = (int) (((a & 0xffffffffL) * (b & 0xffffffffL)) >>> 32);
            case DIV -> x[i.rd()] = b == 0 ? -1 : a == Integer.MIN_VALUE && b == -1 ? a : a / b;
            case DIVU -> x[i.rd()] = b == 0 ? -1 : Integer.divideUnsigned(a, b);
            case REM -> x[i.rd()] = b == 0 ? a : a == Integer.MIN_VALUE && b == -1 ? 0 : a % b;
            case REMU -> x[i.rd()] = b == 0 ? a : Integer.remainderUnsigned(a, b);
            case AND -> x[i.rd()] = a & b;
            case OR -> x[i.rd()] = a | b;
            case XOR -> x[i.rd()] = a ^ b;
            case SLL -> x[i.rd()] = a << (b & 31);
            case SRL -> x[i.rd()] = a >>> (b & 31);
            case SRA -> x[i.rd()] = a >> (b & 31);
            case SLT -> x[i.rd()] = a < b ? 1 : 0;
            case SLTU -> x[i.rd()] = Integer.compareUnsigned(a, b) < 0 ? 1 : 0;
            case ADDI -> x[i.rd()] = a + i.imm();
            case ANDI -> x[i.rd()] = a & i.imm();
            case ORI -> x[i.rd()] = a | i.imm();
            case XORI -> x[i.rd()] = a ^ i.imm();
            case SLLI -> x[i.rd()] = a << (i.imm() & 31);
            case SRLI -> x[i.rd()] = a >>> (i.imm() & 31);
            case SRAI -> x[i.rd()] = a >> (i.imm() & 31);
            case SLTI -> x[i.rd()] = a < i.imm() ? 1 : 0;
            case SLTIU -> x[i.rd()] = Integer.compareUnsigned(a, i.imm()) < 0 ? 1 : 0;
            case LB -> x[i.rd()] = leerByte(a + i.imm());
            case LBU -> x[i.rd()] = leerByte(a + i.imm()) & 0xff;
            case LH -> x[i.rd()] = (short) leer(a + i.imm(), 2);
            case LHU -> x[i.rd()] = (int) leer(a + i.imm(), 2) & 0xffff;
            case LW -> x[i.rd()] = (int) leer(a + i.imm(), 4);
            case SB -> escribir(a + i.imm(), b, 1);
            case SH -> escribir(a + i.imm(), b, 2);
            case SW -> escribir(a + i.imm(), b, 4);
            case BEQ -> siguiente = a == b ? i.imm() : siguiente;
            case BNE -> siguiente = a != b ? i.imm() : siguiente;
            case BLT -> siguiente = a < b ? i.imm() : siguiente;
            case BGE -> siguiente = a >= b ? i.imm() : siguiente;
            case BLTU -> siguiente = Integer.compareUnsigned(a, b) < 0 ? i.imm() : siguiente;
            case BGEU -> siguiente = Integer.compareUnsigned(a, b) >= 0 ? i.imm() : siguiente;
            case JAL -> {
                x[i.rd()] = pc + 4;
                siguiente = i.imm();
            }
            case JALR -> {
                siguiente = (a + i.imm()) & ~1;
                x[i.rd()] = pc + 4;
            }
            case ECALL -> ecall();
            case FLD -> f[i.rd()] = leer(a + i.imm(), 8);
            case FSD -> escribir(a + i.imm(), f[i.rs2()], 8);
            case FADD_D -> fd(i, d(i.rs1()) + d(i.rs2()));
            case FSUB_D -> fd(i, d(i.rs1()) - d(i.rs2()));
            case FMUL_D -> fd(i, d(i.rs1()) * d(i.rs2()));
            case FDIV_D -> fd(i, d(i.rs1()) / d(i.rs2()));
            case FSQRT_D -> fd(i, Math.sqrt(d(i.rs1())));
            case FMIN_D -> fd(i, Math.min(d(i.rs1()), d(i.rs2())));
            case FMAX_D -> fd(i, Math.max(d(i.rs1()), d(i.rs2())));
            case FSGNJ_D -> f[i.rd()] = (f[i.rs1()] & Long.MAX_VALUE) | (f[i.rs2()] & Long.MIN_VALUE);
            case FSGNJN_D -> f[i.rd()] = (f[i.rs1()] & Long.MAX_VALUE) | (~f[i.rs2()] & Long.MIN_VALUE);
            case FSGNJX_D -> f[i.rd()] = f[i.rs1()] ^ (f[i.rs2()] & Long.MIN_VALUE);
            case FEQ_D -> x[i.rd()] = d(i.rs1()) == d(i.rs2()) ? 1 : 0;
            case FLT_D -> x[i.rd()] = d(i.rs1()) < d(i.rs2()) ? 1 : 0;
            case FLE_D -> x[i.rd()] = d(i.rs1()) <= d(i.rs2()) ? 1 : 0;
            case FCVT_D_W -> fd(i, a);
            case FCVT_D_WU -> fd(i, a & 0xffffffffL);
            case FCVT_W_D -> x[i.rd()] = aEntero(redondear(d(i.rs1()), i.imm()));
            case FCVT_WU_D -> x[i.rd()] = (int) aSinSigno(redondear(d(i.rs1()), i.imm()));
            case FCLASS_D -> x[i.rd()] = clasificar(f[i.rs1()]);
        }
        pc = siguiente;
    }

    private double d(int registro) {
        return Double.longBitsToDouble(f[registro]);
    }

    private void fd(Instruccion i, double valor) {
        f[i.rd()] = Double.doubleToRawLongBits(valor);
    }

    private static double redondear(double valor, int modo) {
        return switch (modo) {
            case Instruccion.RTZ -> valor < 0 ? Math.ceil(valor) : Math.floor(valor);
            case Instruccion.RDN -> Math.floor(valor);
            case Instruccion.RUP -> Math.ceil(valor);
            case Instruccion.RMM -> valor < 0 ? -Math.floor(-valor + 0.5) : Math.floor(valor + 0.5);
            default -> Math.rint(valor);
        };
    }

    private static int aEntero(double valor) {
        if (Double.isNaN(valor) || valor >= 2147483647.0) {
            return Integer.MAX_VALUE;
        }
        return valor <= -2147483648.0 ? Integer.MIN_VALUE : (int) valor;
    }

    private static long aSinSigno(double valor) {
        if (Double.isNaN(valor) || valor >= 4294967295.0) {
            return 0xffffffffL;
        }
        return valor <= 0 ? 0 : (long) valor;
    }

    private static int clasificar(long bits) {
        double v = Double.longBitsToDouble(bits);
        boolean negativo = bits < 0;
        long exponente = (bits >>> 52) & 0x7ff;
        long mantisa = bits & 0xf_ffff_ffff_ffffL;
        if (Double.isNaN(v)) {
            return (mantisa & (1L << 51)) != 0 ? 1 << 9 : 1 << 8;
        }
        if (Double.isInfinite(v)) {
            return negativo ? 1 : 1 << 7;
        }
        if (v == 0) {
            return negativo ? 1 << 3 : 1 << 4;
        }
        if (exponente == 0) {
            return negativo ? 1 << 2 : 1 << 5;
        }
        return negativo ? 1 << 1 : 1 << 6;
    }

    // ------------------------------------------------------------------ servicios

    private void ecall() {
        int servicio = x[17];
        switch (servicio) {
            case 1 -> salida.append(x[10]);
            case 3 -> salida.append(d(10));
            case 4 -> salida.append(leerAsciz(x[10]));
            case 5 -> x[10] = (int) numero(leerLinea());
            case 7 -> f[10] = Double.doubleToRawLongBits(numero(leerLinea()));
            case 8 -> leerCadena(x[10], x[11]);
            case 9 -> x[10] = reservar(x[10]);
            case 10 -> throw new Fin();
            case 11 -> salida.append((char) (x[10] & 0xff));
            case 17, 93 -> throw new Fin();
            default -> throw new ErrorEjecucion("servicio ecall no soportado: " + servicio);
        }
    }

    private String leerAsciz(int direccion) {
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        for (int b = leerByte(direccion); b != 0; b = leerByte(++direccion)) {
            bytes.write(b);
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private void leerCadena(int buffer, int maximo) {
        String linea = leerLineaONull();
        if (maximo <= 0) {
            return;
        }
        byte[] bytes = (linea == null ? "" : linea + "\n").getBytes(StandardCharsets.UTF_8);
        int n = Math.min(bytes.length, maximo - 1);
        for (int k = 0; k < n; k++) {
            escribir(buffer + k, bytes[k], 1);
        }
        escribir(buffer + n, 0, 1);
    }

    private int reservar(int bytes) {
        int anterior = finHeap;
        int nuevo = finHeap + ((bytes + 3) & ~3);
        if (bytes < 0 || nuevo - BASE_HEAP > MAXIMO_HEAP) {
            throw new ErrorEjecucion("sbrk: no hay memoria para " + bytes + " bytes");
        }
        int necesario = nuevo - BASE_HEAP;
        if (necesario > heap.length) {
            int tamanio = heap.length;
            while (tamanio < necesario) {
                tamanio *= 2;
            }
            heap = java.util.Arrays.copyOf(heap, tamanio);
        }
        finHeap = nuevo;
        return anterior;
    }

    private String leerLineaONull() {
        try {
            return entrada.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    private String leerLinea() {
        String linea = leerLineaONull();
        return linea == null ? "" : linea;
    }

    private static double numero(String texto) {
        try {
            return Double.parseDouble(texto.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ------------------------------------------------------------------ memoria

    private byte[] zona;
    private int desplazamiento;

    /** Ubica la direccion en su segmento: deja el arreglo en zona y el indice en desplazamiento. */
    private void ubicar(int direccion, int bytes) {
        if ((direccion & (Math.min(bytes, 4) - 1)) != 0) {
            throw new ErrorEjecucion(String.format("acceso de %d bytes no alineado (0x%08x)", bytes, direccion));
        }
        long d = direccion & 0xffffffffL;
        if (d >= Ensamblador.BASE_DATOS && d + bytes <= BASE_HEAP) {
            zona = datos;
            desplazamiento = (int) (d - Ensamblador.BASE_DATOS);
        } else if (d >= BASE_HEAP && d + bytes <= finHeap) {
            zona = heap;
            desplazamiento = (int) (d - BASE_HEAP);
        } else if (d >= (BASE_PILA & 0xffffffffL) && d + bytes <= 0x8000_0000L) {
            zona = pila;
            desplazamiento = (int) (d - BASE_PILA);
        } else {
            throw new ErrorEjecucion(String.format("acceso fuera de la memoria (0x%08x)", direccion));
        }
    }

    private int leerByte(int direccion) {
        long d = direccion & 0xffffffffL;
        if (d >= Ensamblador.BASE_DATOS && d < BASE_HEAP) {
            return datos[(int) (d - Ensamblador.BASE_DATOS)];
        }
        if (d >= BASE_HEAP && d < finHeap) {
            return heap[(int) (d - BASE_HEAP)];
        }
        if (d >= (BASE_PILA & 0xffffffffL) && d < 0x8000_0000L) {
            return pila[(int) (d - BASE_PILA)];
        }
        throw new ErrorEjecucion(String.format("acceso fuera de la memoria (0x%08x)", direccion));
    }

    private long leer(int direccion, int bytes) {
        ubicar(direccion, bytes);
        long valor = 0;
        for (int k = bytes - 1; k >= 0; k--) {
            valor = (valor << 8) | (zona[desplazamiento + k] & 0xff);
        }
        return valor;
    }

    private void escribir(int direccion, long valor, int bytes) {
        if (bytes == 1) {
            long d = direccion & 0xffffffffL;
            if (d >= Ensamblador.BASE_DATOS && d < BASE_HEAP) {
                datos[(int) (d - Ensamblador.BASE_DATOS)] = (byte) valor;
            } else if (d >= BASE_HEAP && d < finHeap) {
                heap[(int) (d - BASE_HEAP)] = (byte) valor;
            } else if (d >= (BASE_PILA & 0xffffffffL) && d < 0x8000_0000L) {
                pila[(int) (d - BASE_PILA)] = (byte) valor;
            } else {
                throw new ErrorEjecucion(String.format("acceso fuera de la memoria (0x%08x)", direccion));
            }
            return;
        }
        ubicar(direccion, bytes);
        for (int k = 0; k < bytes; k++) {
            zona[desplazamiento + k] = (byte) (valor >>> (8 * k));
        }
    }
}
