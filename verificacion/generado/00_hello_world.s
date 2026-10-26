# Generado por Contacto3D: RV32IMD, ecall con el servicio en a7 (RARS)
.data
.align 3
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
_txt_division: .asciz "Error de ejecucion: division entre cero\n"
_buffer: .space 1024
.align 2
_imagen0: .word -1, 67, 97, 100, 101, 116, 101, -1, 72, 111, 108, 97, 32, 77, 117, 110
.word 100, 111, 32, 100, 101, 115, 100, 101, 32, 80, 105, 103, 32, 76, 97, 116
.word 105, 110, -1, 69, 114, 114, 111, 114, 32, 100, 101, 32, 101, 106, 101, 99
.word 117, 99, 105, 111, 110, 32, 101, 110, 32, 72, 111, 108, 97, 77, 117, 110
.word 100, 111, 46, 112, 105, 103, 32, 108, 105, 110, 101, 97, 32, 49, 53, 58
.word 32, 114, 101, 102, 101, 114, 101, 110, 99, 105, 97, 32, 110, 117, 108, 97
.word 58, 32, 115, 101, 32, 105, 110, 116, 101, 110, 116, 111, 32, 117, 115, 97
.word 114, 32, 101, 108, 32, 109, 101, 116, 111, 100, 111, 32, 39, 100, 101, 99
.word 105, 114, 40, 41, 39, 32, 115, 111, 98, 114, 101, 32, 110, 117, 108, 108
.word -1, 50, 32, 43, 32, 51, 32, 61, 32, -1, 69, 108, 32, 100, 111, 98
.word 108, 101, 32, 100, 101, 32, 50, 49, 32, 101, 115, 32, -1, 69, 114, 114
.word 111, 114, 32, 100, 101, 32, 101, 106, 101, 99, 117, 99, 105, 111, 110, 32
.word 101, 110, 32, 72, 111, 108, 97, 77, 117, 110, 100, 111, 46, 112, 105, 103
.word 32, 108, 105, 110, 101, 97, 32, 49, 55, 58, 32, 114, 101, 102, 101, 114
.word 101, 110, 99, 105, 97, 32, 110, 117, 108, 97, 58, 32, 115, 101, 32, 105
.word 110, 116, 101, 110, 116, 111, 32, 117, 115, 97, 114, 32, 101, 108, 32, 109
.word 101, 116, 111, 100, 111, 32, 39, 100, 111, 98, 108, 101, 40, 41, 39, 32
.word 115, 111, 98, 114, 101, 32, 110, 117, 108, 108, -1, 72, 111, 108, 97, 32
.word 77, 117, 110, 100, 111, 32, 100, 101, 115, 100, 101, 32, 89, 63, -1, 72
.word 111, 108, 97, 32, -1, 72, 111, 108, 97, 32, 77, 117, 110, 100, 111, 32
.word 100, 101, 115, 100, 101, 32, 90, 101, 116, 97, 114, 105, 97, 110, 111, -1

.text
.globl _inicio
_inicio:
    li a0, 8000000
    li a7, 9
    ecall
    mv s1, a0
    li a0, 2000000
    li a7, 9
    ecall
    mv s2, a0
    li s10, 0
    li s11, 0
    li s3, 0
    fcvt.d.w fs0, zero
    li t0, -1
    fcvt.d.w fs1, t0
    li t0, 1
    fcvt.d.w fs2, t0
    jal ra, F_main
    li a7, 10
    ecall

F_main:
    addi sp, sp, -16
    sw ra, 0(sp)
    sw s0, 4(sp)
    mv s0, sp
    # heap[0..335] = _imagen0
    la t0, _imagen0
    li t1, 0
    add t1, t1, s1
    li t2, 336
_r0:
    lw t3, 0(t0)
    fcvt.d.w ft0, t3
    fsd ft0, 0(t1)
    addi t0, t0, 4
    addi t1, t1, 8
    addi t2, t2, -1
    bnez t2, _r0
    # H = 336
    li s11, 336
    # P = 2
    li s10, 2
    # call principal
    jal ra, F_principal
    # halt
    li a7, 10
    ecall
F_main_fin:
    mv sp, s0
    lw ra, 0(sp)
    lw s0, 4(sp)
    addi sp, sp, 16
    ret

F_principal:
    addi sp, sp, -176
    sw ra, 0(sp)
    sw s0, 4(sp)
    mv s0, sp
    addi t0, s0, 8
    li t1, 21
_r1:
    fsd fs0, 0(t0)
    addi t0, t0, 8
    addi t1, t1, -1
    bnez t1, _r1
    # t0 = H
    fcvt.d.w ft0, s11
    fsd ft0, 8(s0)
    # H = H + 1
    addi s11, s11, 1
    # t1 = P + 1
    fcvt.d.w ft0, s10
    li t0, 1
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 16(s0)
    # t2 = t1 + 1
    fld ft0, 16(s0)
    li t0, 1
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 24(s0)
    # stack[t2] = t0
    fld ft1, 8(s0)
    fld ft0, 24(s0)
    fcvt.w.d t0, ft0, rtz
    slli t0, t0, 3
    add t0, t0, s2
    fsd ft1, 0(t0)
    # P = P + 1
    addi s10, s10, 1
    # call Hola_constructor
    jal ra, F_Hola_constructor
    # P = P - 1
    addi s10, s10, -1
    # stack[0] = t0
    fld ft1, 8(s0)
    li t0, 0
    add t0, t0, s2
    fsd ft1, 0(t0)
    # stack[1] = 1
    li t0, 1
    fcvt.d.w ft1, t0
    li t0, 8
    add t0, t0, s2
    fsd ft1, 0(t0)
    # print 8  (cadena)
    li t0, 8
    fcvt.d.w ft0, t0
    fcvt.w.d a1, ft0, rtz
    jal ra, _emitir_cadena
    # print_salto
    li a0, 10
    jal ra, _emitir_car
    # t3 = P + 1
    fcvt.d.w ft0, s10
    li t0, 1
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 32(s0)
    # P = P + 1
    addi s10, s10, 1
    # call saludar
    jal ra, F_saludar
    # P = P - 1
    addi s10, s10, -1
    # t4 = stack[1]
    li t0, 8
    add t0, t0, s2
    fld ft2, 0(t0)
    fsd ft2, 40(s0)
    # t5 = P + 1
    fcvt.d.w ft0, s10
    li t0, 1
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 48(s0)
    # t6 = t5 + 1
    fld ft0, 48(s0)
    li t0, 1
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 56(s0)
    # stack[t6] = t4
    fld ft1, 40(s0)
    fld ft0, 56(s0)
    fcvt.w.d t0, ft0, rtz
    slli t0, t0, 3
    add t0, t0, s2
    fsd ft1, 0(t0)
    # P = P + 1
    addi s10, s10, 1
    # call saludarA
    jal ra, F_saludarA
    # P = P - 1
    addi s10, s10, -1
    # t7 = stack[0]
    li t0, 0
    add t0, t0, s2
    fld ft2, 0(t0)
    fsd ft2, 64(s0)
    # t8 = t7 != 0
    fld ft0, 64(s0)
    fmv.d ft1, fs0
    feq.d t0, ft0, ft1
    xori t0, t0, 1
    fcvt.d.w ft2, t0
    fsd ft2, 72(s0)
    # if t8 goto L0
    fld ft0, 72(s0)
    feq.d t0, ft0, fs0
    bnez t0, _r2
    j L0
_r2:
    # error 35
    li t0, 35
    fcvt.d.w ft0, t0
    fcvt.w.d a1, ft0, rtz
    j _error_ejecucion
    # L0:
L0:
    # t9 = P + 1
    fcvt.d.w ft0, s10
    li t0, 1
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 80(s0)
    # t10 = t9 + 1
    fld ft0, 80(s0)
    li t0, 1
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 88(s0)
    # stack[t10] = t7
    fld ft1, 64(s0)
    fld ft0, 88(s0)
    fcvt.w.d t0, ft0, rtz
    slli t0, t0, 3
    add t0, t0, s2
    fsd ft1, 0(t0)
    # P = P + 1
    addi s10, s10, 1
    # call Hola_decir
    jal ra, F_Hola_decir
    # P = P - 1
    addi s10, s10, -1
    # print 145  (cadena)
    li t0, 145
    fcvt.d.w ft0, t0
    fcvt.w.d a1, ft0, rtz
    jal ra, _emitir_cadena
    # t11 = P + 1
    fcvt.d.w ft0, s10
    li t0, 1
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 96(s0)
    # t12 = t11 + 1
    fld ft0, 96(s0)
    li t0, 1
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 104(s0)
    # stack[t12] = 2
    li t0, 2
    fcvt.d.w ft1, t0
    fld ft0, 104(s0)
    fcvt.w.d t0, ft0, rtz
    slli t0, t0, 3
    add t0, t0, s2
    fsd ft1, 0(t0)
    # t13 = t11 + 2
    fld ft0, 96(s0)
    li t0, 2
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 112(s0)
    # stack[t13] = 3
    li t0, 3
    fcvt.d.w ft1, t0
    fld ft0, 112(s0)
    fcvt.w.d t0, ft0, rtz
    slli t0, t0, 3
    add t0, t0, s2
    fsd ft1, 0(t0)
    # P = P + 1
    addi s10, s10, 1
    # call sumar
    jal ra, F_sumar
    # t14 = stack[P]
    slli t0, s10, 3
    add t0, t0, s2
    fld ft2, 0(t0)
    fsd ft2, 120(s0)
    # P = P - 1
    addi s10, s10, -1
    # print t14  (entero)
    fld ft0, 120(s0)
    fmv.d fa0, ft0
    jal ra, _emitir_entero
    # print_salto
    li a0, 10
    jal ra, _emitir_car
    # print 154  (cadena)
    li t0, 154
    fcvt.d.w ft0, t0
    fcvt.w.d a1, ft0, rtz
    jal ra, _emitir_cadena
    # t15 = stack[0]
    li t0, 0
    add t0, t0, s2
    fld ft2, 0(t0)
    fsd ft2, 128(s0)
    # t16 = t15 != 0
    fld ft0, 128(s0)
    fmv.d ft1, fs0
    feq.d t0, ft0, ft1
    xori t0, t0, 1
    fcvt.d.w ft2, t0
    fsd ft2, 136(s0)
    # if t16 goto L1
    fld ft0, 136(s0)
    feq.d t0, ft0, fs0
    bnez t0, _r3
    j L1
_r3:
    # error 173
    li t0, 173
    fcvt.d.w ft0, t0
    fcvt.w.d a1, ft0, rtz
    j _error_ejecucion
    # L1:
L1:
    # t17 = P + 1
    fcvt.d.w ft0, s10
    li t0, 1
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 144(s0)
    # t18 = t17 + 1
    fld ft0, 144(s0)
    li t0, 1
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 152(s0)
    # stack[t18] = t15
    fld ft1, 128(s0)
    fld ft0, 152(s0)
    fcvt.w.d t0, ft0, rtz
    slli t0, t0, 3
    add t0, t0, s2
    fsd ft1, 0(t0)
    # t19 = t17 + 2
    fld ft0, 144(s0)
    li t0, 2
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 160(s0)
    # stack[t19] = 21
    li t0, 21
    fcvt.d.w ft1, t0
    fld ft0, 160(s0)
    fcvt.w.d t0, ft0, rtz
    slli t0, t0, 3
    add t0, t0, s2
    fsd ft1, 0(t0)
    # P = P + 1
    addi s10, s10, 1
    # call Hola_doble
    jal ra, F_Hola_doble
    # t20 = stack[P]
    slli t0, s10, 3
    add t0, t0, s2
    fld ft2, 0(t0)
    fsd ft2, 168(s0)
    # P = P - 1
    addi s10, s10, -1
    # print t20  (entero)
    fld ft0, 168(s0)
    fmv.d fa0, ft0
    jal ra, _emitir_entero
    # print_salto
    li a0, 10
    jal ra, _emitir_car
F_principal_fin:
    mv sp, s0
    lw ra, 0(sp)
    lw s0, 4(sp)
    addi sp, sp, 176
    ret

F_saludar:
    addi sp, sp, -16
    sw ra, 0(sp)
    sw s0, 4(sp)
    mv s0, sp
    # print 283  (cadena)
    li t0, 283
    fcvt.d.w ft0, t0
    fcvt.w.d a1, ft0, rtz
    jal ra, _emitir_cadena
    # print_salto
    li a0, 10
    jal ra, _emitir_car
F_saludar_fin:
    mv sp, s0
    lw ra, 0(sp)
    lw s0, 4(sp)
    addi sp, sp, 16
    ret

F_saludarA:
    addi sp, sp, -32
    sw ra, 0(sp)
    sw s0, 4(sp)
    mv s0, sp
    addi t0, s0, 8
    li t1, 3
_r4:
    fsd fs0, 0(t0)
    addi t0, t0, 8
    addi t1, t1, -1
    bnez t1, _r4
    # t21 = P + 1
    fcvt.d.w ft0, s10
    li t0, 1
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 8(s0)
    # t22 = stack[t21]
    fld ft0, 8(s0)
    fcvt.w.d t0, ft0, rtz
    slli t0, t0, 3
    add t0, t0, s2
    fld ft2, 0(t0)
    fsd ft2, 16(s0)
    # t23 = concatenar 303, t22
    li t0, 303
    fcvt.d.w ft0, t0
    fcvt.w.d a1, ft0, rtz
    fld ft1, 16(s0)
    fcvt.w.d a2, ft1, rtz
    jal ra, _concatenar
    fcvt.d.w ft2, a0
    fsd ft2, 24(s0)
    # print t23  (cadena)
    fld ft0, 24(s0)
    fcvt.w.d a1, ft0, rtz
    jal ra, _emitir_cadena
    # print_salto
    li a0, 10
    jal ra, _emitir_car
F_saludarA_fin:
    mv sp, s0
    lw ra, 0(sp)
    lw s0, 4(sp)
    addi sp, sp, 32
    ret

F_sumar:
    addi sp, sp, -48
    sw ra, 0(sp)
    sw s0, 4(sp)
    mv s0, sp
    addi t0, s0, 8
    li t1, 5
_r5:
    fsd fs0, 0(t0)
    addi t0, t0, 8
    addi t1, t1, -1
    bnez t1, _r5
    # t24 = P + 1
    fcvt.d.w ft0, s10
    li t0, 1
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 8(s0)
    # t25 = stack[t24]
    fld ft0, 8(s0)
    fcvt.w.d t0, ft0, rtz
    slli t0, t0, 3
    add t0, t0, s2
    fld ft2, 0(t0)
    fsd ft2, 16(s0)
    # t26 = P + 2
    fcvt.d.w ft0, s10
    li t0, 2
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 24(s0)
    # t27 = stack[t26]
    fld ft0, 24(s0)
    fcvt.w.d t0, ft0, rtz
    slli t0, t0, 3
    add t0, t0, s2
    fld ft2, 0(t0)
    fsd ft2, 32(s0)
    # t28 = t25 + t27
    fld ft0, 16(s0)
    fld ft1, 32(s0)
    fadd.d ft2, ft0, ft1
    fsd ft2, 40(s0)
    # stack[P] = t28
    fld ft1, 40(s0)
    slli t0, s10, 3
    add t0, t0, s2
    fsd ft1, 0(t0)
    # return
    j F_sumar_fin
F_sumar_fin:
    mv sp, s0
    lw ra, 0(sp)
    lw s0, 4(sp)
    addi sp, sp, 48
    ret

F_Hola_constructor:
    addi sp, sp, -32
    sw ra, 0(sp)
    sw s0, 4(sp)
    mv s0, sp
    addi t0, s0, 8
    li t1, 2
_r6:
    fsd fs0, 0(t0)
    addi t0, t0, 8
    addi t1, t1, -1
    bnez t1, _r6
    # t29 = P + 1
    fcvt.d.w ft0, s10
    li t0, 1
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 8(s0)
    # t30 = stack[t29]
    fld ft0, 8(s0)
    fcvt.w.d t0, ft0, rtz
    slli t0, t0, 3
    add t0, t0, s2
    fld ft2, 0(t0)
    fsd ft2, 16(s0)
    # heap[t30] = 309
    li t0, 309
    fcvt.d.w ft1, t0
    fld ft0, 16(s0)
    fcvt.w.d t0, ft0, rtz
    slli t0, t0, 3
    add t0, t0, s1
    fsd ft1, 0(t0)
F_Hola_constructor_fin:
    mv sp, s0
    lw ra, 0(sp)
    lw s0, 4(sp)
    addi sp, sp, 32
    ret

F_Hola_decir:
    addi sp, sp, -32
    sw ra, 0(sp)
    sw s0, 4(sp)
    mv s0, sp
    addi t0, s0, 8
    li t1, 3
_r7:
    fsd fs0, 0(t0)
    addi t0, t0, 8
    addi t1, t1, -1
    bnez t1, _r7
    # t31 = P + 1
    fcvt.d.w ft0, s10
    li t0, 1
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 8(s0)
    # t32 = stack[t31]
    fld ft0, 8(s0)
    fcvt.w.d t0, ft0, rtz
    slli t0, t0, 3
    add t0, t0, s2
    fld ft2, 0(t0)
    fsd ft2, 16(s0)
    # t33 = heap[t32]
    fld ft0, 16(s0)
    fcvt.w.d t0, ft0, rtz
    slli t0, t0, 3
    add t0, t0, s1
    fld ft2, 0(t0)
    fsd ft2, 24(s0)
    # print t33  (cadena)
    fld ft0, 24(s0)
    fcvt.w.d a1, ft0, rtz
    jal ra, _emitir_cadena
    # print_salto
    li a0, 10
    jal ra, _emitir_car
F_Hola_decir_fin:
    mv sp, s0
    lw ra, 0(sp)
    lw s0, 4(sp)
    addi sp, sp, 32
    ret

F_Hola_doble:
    addi sp, sp, -32
    sw ra, 0(sp)
    sw s0, 4(sp)
    mv s0, sp
    addi t0, s0, 8
    li t1, 3
_r8:
    fsd fs0, 0(t0)
    addi t0, t0, 8
    addi t1, t1, -1
    bnez t1, _r8
    # t34 = P + 2
    fcvt.d.w ft0, s10
    li t0, 2
    fcvt.d.w ft1, t0
    fadd.d ft2, ft0, ft1
    fsd ft2, 8(s0)
    # t35 = stack[t34]
    fld ft0, 8(s0)
    fcvt.w.d t0, ft0, rtz
    slli t0, t0, 3
    add t0, t0, s2
    fld ft2, 0(t0)
    fsd ft2, 16(s0)
    # t36 = t35 * 2
    fld ft0, 16(s0)
    li t0, 2
    fcvt.d.w ft1, t0
    fmul.d ft2, ft0, ft1
    fsd ft2, 24(s0)
    # stack[P] = t36
    fld ft1, 24(s0)
    slli t0, s10, 3
    add t0, t0, s2
    fsd ft1, 0(t0)
    # return
    j F_Hola_doble_fin
F_Hola_doble_fin:
    mv sp, s0
    lw ra, 0(sp)
    lw s0, 4(sp)
    addi sp, sp, 32
    ret

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
