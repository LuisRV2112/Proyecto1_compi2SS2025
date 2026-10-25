package com.usac.contacto3d.riscv;

/**
 * Instruccion ya decodificada. En saltos y ramas, imm es la direccion destino absoluta.
 * En las conversiones de punto flotante, imm es el modo de redondeo.
 */
public record Instruccion(Op op, int rd, int rs1, int rs2, int imm, int linea, String fuente) {

    public enum Op {
        LI, LUI, AUIPC,
        ADD, SUB, MUL, MULH, MULHU, DIV, DIVU, REM, REMU, AND, OR, XOR, SLL, SRL, SRA, SLT, SLTU,
        ADDI, ANDI, ORI, XORI, SLLI, SRLI, SRAI, SLTI, SLTIU,
        LB, LBU, LH, LHU, LW, SB, SH, SW,
        BEQ, BNE, BLT, BGE, BLTU, BGEU,
        JAL, JALR,
        ECALL,
        FLD, FSD,
        FADD_D, FSUB_D, FMUL_D, FDIV_D, FSQRT_D, FMIN_D, FMAX_D,
        FSGNJ_D, FSGNJN_D, FSGNJX_D,
        FEQ_D, FLT_D, FLE_D,
        FCVT_D_W, FCVT_D_WU, FCVT_W_D, FCVT_WU_D, FCLASS_D
    }

    public static final int RNE = 0;
    public static final int RTZ = 1;
    public static final int RDN = 2;
    public static final int RUP = 3;
    public static final int RMM = 4;
    public static final int DYN = 7;
}
