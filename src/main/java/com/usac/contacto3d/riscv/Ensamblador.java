package com.usac.contacto3d.riscv;

import com.usac.contacto3d.riscv.Instruccion.Op;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convierte el texto .s en un programa ejecutable por el Simulador, en dos pasadas:
 * la primera reserva los datos y ubica las etiquetas, la segunda decodifica las instrucciones.
 * Las seudoinstrucciones se expanden aqui, asi el simulador solo conoce instrucciones base.
 */
public class Ensamblador {

    public static final int BASE_TEXTO = 0x0040_0000;
    public static final int BASE_DATOS = 0x1001_0000;

    public record Programa(List<Instruccion> instrucciones, byte[] datos, Map<String, Integer> etiquetas) { }

    public static class ErrorEnsamblado extends RuntimeException {
        public ErrorEnsamblado(int linea, String mensaje) {
            super("linea " + linea + ": " + mensaje);
        }
    }

    private record LineaTexto(int numero, String fuente, String mnemonico, List<String> operandos) { }

    private final Map<String, Integer> etiquetas = new HashMap<>();
    private final ByteArrayOutputStream datos = new ByteArrayOutputStream();
    private final List<LineaTexto> texto = new ArrayList<>();

    public static Programa ensamblar(String fuente) {
        return new Ensamblador().procesar(fuente);
    }

    private Programa procesar(String fuente) {
        String[] lineas = fuente.split("\n", -1);
        boolean enTexto = true;
        for (int i = 0; i < lineas.length; i++) {
            int numero = i + 1;
            String linea = quitarComentario(lineas[i]).trim();
            while (true) {
                int dosPuntos = finDeEtiqueta(linea);
                if (dosPuntos < 0) {
                    break;
                }
                String etiqueta = linea.substring(0, dosPuntos).trim();
                if (etiquetas.containsKey(etiqueta)) {
                    throw new ErrorEnsamblado(numero, "etiqueta repetida '" + etiqueta + "'");
                }
                etiquetas.put(etiqueta, enTexto ? BASE_TEXTO + 4 * texto.size() : BASE_DATOS + datos.size());
                linea = linea.substring(dosPuntos + 1).trim();
            }
            if (linea.isEmpty()) {
                continue;
            }
            String[] partes = linea.split("\\s+", 2);
            String palabra = partes[0].toLowerCase();
            String resto = partes.length > 1 ? partes[1].trim() : "";
            if (palabra.startsWith(".")) {
                switch (palabra) {
                    case ".data" -> enTexto = false;
                    case ".text" -> enTexto = true;
                    case ".globl", ".global", ".section", ".type", ".size", ".file" -> { }
                    default -> {
                        if (enTexto) {
                            throw new ErrorEnsamblado(numero, "directiva de datos en .text: " + palabra);
                        }
                        directiva(numero, palabra, resto);
                    }
                }
            } else {
                if (!enTexto) {
                    throw new ErrorEnsamblado(numero, "instruccion en .data: " + palabra);
                }
                texto.add(new LineaTexto(numero, linea, palabra, separarOperandos(resto)));
            }
        }

        List<Instruccion> instrucciones = new ArrayList<>(texto.size());
        for (int i = 0; i < texto.size(); i++) {
            instrucciones.add(decodificar(texto.get(i), BASE_TEXTO + 4 * i));
        }
        return new Programa(instrucciones, datos.toByteArray(), Map.copyOf(etiquetas));
    }

    // ------------------------------------------------------------------ datos

    private void directiva(int numero, String nombre, String resto) {
        switch (nombre) {
            case ".align" -> alinear(1 << entero(numero, resto));
            case ".space", ".zero" -> {
                int bytes = entero(numero, resto);
                for (int i = 0; i < bytes; i++) {
                    datos.write(0);
                }
            }
            case ".asciz", ".string", ".ascii" -> {
                for (String literal : separarOperandos(resto)) {
                    byte[] bytes = cadena(numero, literal).getBytes(StandardCharsets.UTF_8);
                    datos.write(bytes, 0, bytes.length);
                    if (!nombre.equals(".ascii")) {
                        datos.write(0);
                    }
                }
            }
            case ".byte" -> separarOperandos(resto).forEach(v -> escribir(entero(numero, v), 1));
            case ".half" -> {
                alinear(2);
                separarOperandos(resto).forEach(v -> escribir(entero(numero, v), 2));
            }
            case ".word" -> {
                alinear(4);
                for (String v : separarOperandos(resto)) {
                    escribir(etiquetas.containsKey(v) ? etiquetas.get(v) : entero(numero, v), 4);
                }
            }
            case ".double" -> {
                alinear(8);
                for (String v : separarOperandos(resto)) {
                    long bits = Double.doubleToRawLongBits(decimal(numero, v));
                    escribir((int) bits, 4);
                    escribir((int) (bits >>> 32), 4);
                }
            }
            case ".float" -> {
                alinear(4);
                separarOperandos(resto).forEach(v -> escribir(Float.floatToRawIntBits((float) decimal(numero, v)), 4));
            }
            default -> throw new ErrorEnsamblado(numero, "directiva desconocida " + nombre);
        }
    }

    private void alinear(int bytes) {
        while (datos.size() % bytes != 0) {
            datos.write(0);
        }
    }

    private void escribir(long valor, int bytes) {
        for (int i = 0; i < bytes; i++) {
            datos.write((int) (valor >>> (8 * i)) & 0xff);
        }
    }

    // ------------------------------------------------------------------ instrucciones

    private Instruccion decodificar(LineaTexto l, int direccion) {
        List<String> o = l.operandos();
        String m = l.mnemonico();
        try {
            return switch (m) {
                case "nop" -> i(l, Op.ADDI, 0, 0, 0, 0);
                case "li" -> i(l, Op.LI, xr(l, o, 0), 0, 0, entero(l.numero(), arg(l, o, 1)));
                case "la" -> i(l, Op.LI, xr(l, o, 0), 0, 0, direccionDe(l, arg(l, o, 1)));
                case "mv" -> i(l, Op.ADDI, xr(l, o, 0), xr(l, o, 1), 0, 0);
                case "not" -> i(l, Op.XORI, xr(l, o, 0), xr(l, o, 1), 0, -1);
                case "neg" -> i(l, Op.SUB, xr(l, o, 0), 0, xr(l, o, 1), 0);
                case "seqz" -> i(l, Op.SLTIU, xr(l, o, 0), xr(l, o, 1), 0, 1);
                case "snez" -> i(l, Op.SLTU, xr(l, o, 0), 0, xr(l, o, 1), 0);
                case "sltz" -> i(l, Op.SLT, xr(l, o, 0), xr(l, o, 1), 0, 0);
                case "sgtz" -> i(l, Op.SLT, xr(l, o, 0), 0, xr(l, o, 1), 0);
                case "beqz" -> i(l, Op.BEQ, 0, xr(l, o, 0), 0, direccionDe(l, arg(l, o, 1)));
                case "bnez" -> i(l, Op.BNE, 0, xr(l, o, 0), 0, direccionDe(l, arg(l, o, 1)));
                case "bltz" -> i(l, Op.BLT, 0, xr(l, o, 0), 0, direccionDe(l, arg(l, o, 1)));
                case "bgez" -> i(l, Op.BGE, 0, xr(l, o, 0), 0, direccionDe(l, arg(l, o, 1)));
                case "blez" -> i(l, Op.BGE, 0, 0, xr(l, o, 0), direccionDe(l, arg(l, o, 1)));
                case "bgtz" -> i(l, Op.BLT, 0, 0, xr(l, o, 0), direccionDe(l, arg(l, o, 1)));
                case "bgt" -> i(l, Op.BLT, 0, xr(l, o, 1), xr(l, o, 0), direccionDe(l, arg(l, o, 2)));
                case "ble" -> i(l, Op.BGE, 0, xr(l, o, 1), xr(l, o, 0), direccionDe(l, arg(l, o, 2)));
                case "bgtu" -> i(l, Op.BLTU, 0, xr(l, o, 1), xr(l, o, 0), direccionDe(l, arg(l, o, 2)));
                case "bleu" -> i(l, Op.BGEU, 0, xr(l, o, 1), xr(l, o, 0), direccionDe(l, arg(l, o, 2)));
                case "j" -> i(l, Op.JAL, 0, 0, 0, direccionDe(l, arg(l, o, 0)));
                case "jr" -> i(l, Op.JALR, 0, xr(l, o, 0), 0, 0);
                case "ret" -> i(l, Op.JALR, 0, 1, 0, 0);
                case "call" -> i(l, Op.JAL, 1, 0, 0, direccionDe(l, arg(l, o, 0)));
                case "tail" -> i(l, Op.JAL, 0, 0, 0, direccionDe(l, arg(l, o, 0)));
                case "jal" -> o.size() == 1
                        ? i(l, Op.JAL, 1, 0, 0, direccionDe(l, arg(l, o, 0)))
                        : i(l, Op.JAL, xr(l, o, 0), 0, 0, direccionDe(l, arg(l, o, 1)));
                case "jalr" -> {
                    if (o.size() == 1) {
                        yield i(l, Op.JALR, 1, xr(l, o, 0), 0, 0);
                    }
                    if (o.size() == 2 && o.get(1).contains("(")) {
                        int[] mem = memoria(l, o.get(1));
                        yield i(l, Op.JALR, xr(l, o, 0), mem[1], 0, mem[0]);
                    }
                    yield i(l, Op.JALR, xr(l, o, 0), xr(l, o, 1), 0, o.size() > 2 ? entero(l.numero(), o.get(2)) : 0);
                }
                case "lui" -> i(l, Op.LUI, xr(l, o, 0), 0, 0, entero(l.numero(), arg(l, o, 1)) << 12);
                case "auipc" -> i(l, Op.AUIPC, xr(l, o, 0), 0, 0, direccion + (entero(l.numero(), arg(l, o, 1)) << 12));
                case "ecall" -> i(l, Op.ECALL, 0, 0, 0, 0);
                case "fmv.d" -> i(l, Op.FSGNJ_D, fr(l, o, 0), fr(l, o, 1), fr(l, o, 1), 0);
                case "fneg.d" -> i(l, Op.FSGNJN_D, fr(l, o, 0), fr(l, o, 1), fr(l, o, 1), 0);
                case "fabs.d" -> i(l, Op.FSGNJX_D, fr(l, o, 0), fr(l, o, 1), fr(l, o, 1), 0);
                default -> base(l, o, m);
            };
        } catch (ErrorEnsamblado e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ErrorEnsamblado(l.numero(), "no se pudo interpretar '" + l.fuente() + "'");
        }
    }

    private Instruccion base(LineaTexto l, List<String> o, String m) {
        Op op = switch (m) {
            case "add" -> Op.ADD; case "sub" -> Op.SUB; case "mul" -> Op.MUL; case "mulh" -> Op.MULH;
            case "mulhu" -> Op.MULHU; case "div" -> Op.DIV; case "divu" -> Op.DIVU; case "rem" -> Op.REM;
            case "remu" -> Op.REMU; case "and" -> Op.AND; case "or" -> Op.OR; case "xor" -> Op.XOR;
            case "sll" -> Op.SLL; case "srl" -> Op.SRL; case "sra" -> Op.SRA; case "slt" -> Op.SLT;
            case "sltu" -> Op.SLTU;
            case "addi" -> Op.ADDI; case "andi" -> Op.ANDI; case "ori" -> Op.ORI; case "xori" -> Op.XORI;
            case "slli" -> Op.SLLI; case "srli" -> Op.SRLI; case "srai" -> Op.SRAI; case "slti" -> Op.SLTI;
            case "sltiu" -> Op.SLTIU;
            case "lb" -> Op.LB; case "lbu" -> Op.LBU; case "lh" -> Op.LH; case "lhu" -> Op.LHU; case "lw" -> Op.LW;
            case "sb" -> Op.SB; case "sh" -> Op.SH; case "sw" -> Op.SW;
            case "beq" -> Op.BEQ; case "bne" -> Op.BNE; case "blt" -> Op.BLT; case "bge" -> Op.BGE;
            case "bltu" -> Op.BLTU; case "bgeu" -> Op.BGEU;
            case "fld" -> Op.FLD; case "fsd" -> Op.FSD;
            case "fadd.d" -> Op.FADD_D; case "fsub.d" -> Op.FSUB_D; case "fmul.d" -> Op.FMUL_D;
            case "fdiv.d" -> Op.FDIV_D; case "fsqrt.d" -> Op.FSQRT_D; case "fmin.d" -> Op.FMIN_D;
            case "fmax.d" -> Op.FMAX_D; case "fsgnj.d" -> Op.FSGNJ_D; case "fsgnjn.d" -> Op.FSGNJN_D;
            case "fsgnjx.d" -> Op.FSGNJX_D; case "feq.d" -> Op.FEQ_D; case "flt.d" -> Op.FLT_D;
            case "fle.d" -> Op.FLE_D; case "fcvt.d.w" -> Op.FCVT_D_W; case "fcvt.d.wu" -> Op.FCVT_D_WU;
            case "fcvt.w.d" -> Op.FCVT_W_D; case "fcvt.wu.d" -> Op.FCVT_WU_D; case "fclass.d" -> Op.FCLASS_D;
            default -> throw new ErrorEnsamblado(l.numero(), "instruccion desconocida '" + m + "'");
        };
        return switch (op) {
            case ADD, SUB, MUL, MULH, MULHU, DIV, DIVU, REM, REMU, AND, OR, XOR, SLL, SRL, SRA, SLT, SLTU ->
                    i(l, op, xr(l, o, 0), xr(l, o, 1), xr(l, o, 2), 0);
            case ADDI, ANDI, ORI, XORI, SLLI, SRLI, SRAI, SLTI, SLTIU ->
                    i(l, op, xr(l, o, 0), xr(l, o, 1), 0, inmediato(l, arg(l, o, 2)));
            case LB, LBU, LH, LHU, LW -> {
                int[] mem = memoria(l, arg(l, o, 1));
                yield i(l, op, xr(l, o, 0), mem[1], 0, mem[0]);
            }
            case SB, SH, SW -> {
                int[] mem = memoria(l, arg(l, o, 1));
                yield i(l, op, 0, mem[1], xr(l, o, 0), mem[0]);
            }
            case FLD -> {
                int[] mem = memoria(l, arg(l, o, 1));
                yield i(l, op, fr(l, o, 0), mem[1], 0, mem[0]);
            }
            case FSD -> {
                int[] mem = memoria(l, arg(l, o, 1));
                yield i(l, op, 0, mem[1], fr(l, o, 0), mem[0]);
            }
            case BEQ, BNE, BLT, BGE, BLTU, BGEU ->
                    i(l, op, 0, xr(l, o, 0), xr(l, o, 1), direccionDe(l, arg(l, o, 2)));
            case FADD_D, FSUB_D, FMUL_D, FDIV_D, FMIN_D, FMAX_D, FSGNJ_D, FSGNJN_D, FSGNJX_D ->
                    i(l, op, fr(l, o, 0), fr(l, o, 1), fr(l, o, 2), 0);
            case FSQRT_D -> i(l, op, fr(l, o, 0), fr(l, o, 1), 0, 0);
            case FEQ_D, FLT_D, FLE_D -> i(l, op, xr(l, o, 0), fr(l, o, 1), fr(l, o, 2), 0);
            case FCVT_D_W, FCVT_D_WU -> i(l, op, fr(l, o, 0), xr(l, o, 1), 0, 0);
            case FCVT_W_D, FCVT_WU_D -> i(l, op, xr(l, o, 0), fr(l, o, 1), 0, redondeo(l, o));
            case FCLASS_D -> i(l, op, xr(l, o, 0), fr(l, o, 1), 0, 0);
            default -> throw new IllegalStateException(op.toString());
        };
    }

    private static Instruccion i(LineaTexto l, Op op, int rd, int rs1, int rs2, int imm) {
        return new Instruccion(op, rd, rs1, rs2, imm, l.numero(), l.fuente());
    }

    private static int redondeo(LineaTexto l, List<String> o) {
        if (o.size() < 3) {
            return Instruccion.DYN;
        }
        return switch (o.get(2).toLowerCase()) {
            case "rne" -> Instruccion.RNE;
            case "rtz" -> Instruccion.RTZ;
            case "rdn" -> Instruccion.RDN;
            case "rup" -> Instruccion.RUP;
            case "rmm" -> Instruccion.RMM;
            case "dyn" -> Instruccion.DYN;
            default -> throw new ErrorEnsamblado(l.numero(), "modo de redondeo desconocido " + o.get(2));
        };
    }

    private int direccionDe(LineaTexto l, String etiqueta) {
        Integer direccion = etiquetas.get(etiqueta);
        if (direccion == null) {
            throw new ErrorEnsamblado(l.numero(), "etiqueta no definida '" + etiqueta + "'");
        }
        return direccion;
    }

    private static int inmediato(LineaTexto l, String texto) {
        int valor = entero(l.numero(), texto);
        if (valor < -2048 || valor > 2047) {
            throw new ErrorEnsamblado(l.numero(), "inmediato fuera de rango (12 bits): " + texto);
        }
        return valor;
    }

    /** "off(reg)" o "(reg)" -> {desplazamiento, registro} */
    private static int[] memoria(LineaTexto l, String texto) {
        int abre = texto.indexOf('(');
        int cierra = texto.lastIndexOf(')');
        if (abre < 0 || cierra < abre) {
            throw new ErrorEnsamblado(l.numero(), "se esperaba desplazamiento(registro): " + texto);
        }
        String desplazamiento = texto.substring(0, abre).trim();
        int valor = desplazamiento.isEmpty() ? 0 : inmediato(l, desplazamiento);
        return new int[] {valor, Registros.entero(texto.substring(abre + 1, cierra).trim(), l.numero())};
    }

    private static String arg(LineaTexto l, List<String> o, int indice) {
        if (indice >= o.size()) {
            throw new ErrorEnsamblado(l.numero(), "faltan operandos en '" + l.fuente() + "'");
        }
        return o.get(indice);
    }

    private static int xr(LineaTexto l, List<String> o, int indice) {
        return Registros.entero(arg(l, o, indice), l.numero());
    }

    private static int fr(LineaTexto l, List<String> o, int indice) {
        return Registros.flotante(arg(l, o, indice), l.numero());
    }

    // ------------------------------------------------------------------ lexico

    private static int finDeEtiqueta(String linea) {
        int dosPuntos = linea.indexOf(':');
        if (dosPuntos <= 0 || linea.indexOf('"') >= 0 && linea.indexOf('"') < dosPuntos) {
            return -1;
        }
        String candidata = linea.substring(0, dosPuntos).trim();
        return candidata.matches("[A-Za-z_.$][A-Za-z0-9_.$]*") ? dosPuntos : -1;
    }

    private static String quitarComentario(String linea) {
        boolean enCadena = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"' && (i == 0 || linea.charAt(i - 1) != '\\')) {
                enCadena = !enCadena;
            } else if (c == '#' && !enCadena) {
                return linea.substring(0, i);
            }
        }
        return linea;
    }

    private static List<String> separarOperandos(String texto) {
        List<String> operandos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean enCadena = false;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '"' && (i == 0 || texto.charAt(i - 1) != '\\')) {
                enCadena = !enCadena;
            }
            if (c == ',' && !enCadena) {
                operandos.add(actual.toString().trim());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        if (!actual.toString().isBlank()) {
            operandos.add(actual.toString().trim());
        }
        return operandos;
    }

    private static String cadena(int numero, String literal) {
        if (literal.length() < 2 || !literal.startsWith("\"") || !literal.endsWith("\"")) {
            throw new ErrorEnsamblado(numero, "se esperaba una cadena entre comillas");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < literal.length() - 1; i++) {
            char c = literal.charAt(i);
            if (c == '\\' && i + 1 < literal.length() - 1) {
                char e = literal.charAt(++i);
                sb.append(switch (e) {
                    case 'n' -> '\n';
                    case 't' -> '\t';
                    case 'r' -> '\r';
                    case '0' -> '\0';
                    default -> e;
                });
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static int entero(int numero, String texto) {
        String t = texto.trim().toLowerCase();
        try {
            if (t.length() == 3 && t.startsWith("'") && t.endsWith("'")) {
                return t.charAt(1);
            }
            boolean negativo = t.startsWith("-");
            String digitos = negativo || t.startsWith("+") ? t.substring(1) : t;
            long valor = digitos.startsWith("0x") ? Long.parseLong(digitos.substring(2), 16)
                    : digitos.startsWith("0b") ? Long.parseLong(digitos.substring(2), 2)
                    : Long.parseLong(digitos);
            return (int) (negativo ? -valor : valor);
        } catch (NumberFormatException e) {
            throw new ErrorEnsamblado(numero, "se esperaba un entero: " + texto);
        }
    }

    private static double decimal(int numero, String texto) {
        try {
            return Double.parseDouble(texto.trim());
        } catch (NumberFormatException e) {
            throw new ErrorEnsamblado(numero, "se esperaba un numero: " + texto);
        }
    }
}
