#!/bin/bash
cd "$(dirname "$0")/../.." || exit 1
JAR=target/contacto-3d-1.0.0.jar
mkdir -p salida
fallos=0

comparar() {
    if diferencias=$(diff <(echo "$2") "$3"); then
        echo "OK    $1"
    else
        echo "FALLA $1"
        echo "$diferencias"
        fallos=$((fallos + 1))
    fi
}

for pig in entradas/programas/*.pig entradas/herencia/*.pig entradas/objetos/*.pig \
           entradas/polimorfismo/*.pig entradas/banco/*.pig entradas/respaldo/*.pig; do
    esperado="${pig%.pig}.esperado"
    case "$(basename "$pig")" in errores_*) continue ;; esac
    [ -f "$esperado" ] || continue
    base=$(basename "${pig%.pig}")
    entrada="${pig%.pig}.entrada"
    [ -f "$entrada" ] || entrada=/dev/null

    interprete=$(java -cp "$JAR" com.usac.contacto3d.Compilador --ejecutar "$pig" < "$entrada" \
                 | sed -n '/^-- salida --$/,/^-- fin --$/p' | sed '1d;$d')
    comparar "$pig (interprete)" "$interprete" "$esperado"

    riscv=$(java -cp "$JAR" com.usac.contacto3d.Compilador --riscv "salida/$base.s" --simular "$pig" < "$entrada" \
            | sed -n '/^-- salida --$/,/^-- fin --$/p' | sed '1d;$d')
    comparar "$pig (RISC-V simulado)" "$riscv" "$esperado"

    java -cp "$JAR" com.usac.contacto3d.Compilador --c "salida/$base.c" "$pig" > /dev/null
    if gcc -std=c11 -Wall -Wextra -Werror "salida/$base.c" -o "salida/$base" 2> "salida/$base.gcc"; then
        comparar "$pig (C con gcc)" "$("./salida/$base" < "$entrada")" "$esperado"
    else
        echo "FALLA $pig (gcc no compilo)"
        cat "salida/$base.gcc"
        fallos=$((fallos + 1))
    fi
done
exit $fallos
