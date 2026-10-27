#!/bin/bash
cd "$(dirname "$0")/.." || exit 1
JAR=target/contacto-3d-1.0.0.jar
CASOS=${1:-verificacion/casos}
DESTINO=verificacion/generado
mkdir -p "$DESTINO"
fallos=0

if [ -z "$CASOS" ] || [ ! -d "$CASOS" ]; then
    echo "No se encontro la carpeta de casos. Uso: $0 carpeta_de_casos"
    exit 2
fi

salida_de() {
    sed -n '/^-- salida --$/,/^-- fin --$/p' | sed '1d;$d'
}

for caso in "$CASOS"/0*/; do
    nombre=$(basename "$caso")
    pig=$(ls "$caso"*.pig | head -1)
    entrada="verificacion/entradas/$nombre.entrada"
    [ -f "$entrada" ] || entrada=/dev/null

    java -cp "$JAR" com.usac.contacto3d.Compilador --riscv "$DESTINO/$nombre.s" "$pig" > /dev/null
    java -cp "$JAR" com.usac.contacto3d.Compilador --ejecutar "$pig" < "$entrada" | salida_de > "$DESTINO/$nombre.esperado"
    java -cp "$JAR" com.usac.contacto3d.Compilador --simular "$pig" < "$entrada" | salida_de > "$DESTINO/$nombre.simulador"

    if diff -q "$DESTINO/$nombre.esperado" "$DESTINO/$nombre.simulador" > /dev/null; then
        echo "OK    $nombre: simulador propio = interprete ($(wc -l < "$DESTINO/$nombre.esperado") lineas)"
    else
        echo "FALLA $nombre: el simulador propio difiere del interprete"
        diff "$DESTINO/$nombre.esperado" "$DESTINO/$nombre.simulador" | head -10
        fallos=$((fallos + 1))
    fi

    if [ -n "$RARS" ]; then
        java -jar "$RARS" nc "$DESTINO/$nombre.s" < "$entrada" > "$DESTINO/$nombre.rars"
        if diff -q <(sed -e '$a\' "$DESTINO/$nombre.esperado") <(sed -e '$a\' "$DESTINO/$nombre.rars") > /dev/null; then
            echo "OK    $nombre: RARS = interprete"
        else
            echo "FALLA $nombre: RARS difiere del interprete"
            diff "$DESTINO/$nombre.esperado" "$DESTINO/$nombre.rars" | head -10
            fallos=$((fallos + 1))
        fi
    fi
done
exit $fallos
