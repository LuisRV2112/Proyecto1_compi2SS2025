#!/bin/bash
# Corre cada programa de esta carpeta con el interprete de cuartetas y compara
# su salida con el .esperado. Uso (desde la raiz, despues de mvn package):
#     entradas/programas/probar.sh
cd "$(dirname "$0")/../.." || exit 1
fallos=0
for pig in entradas/programas/*.pig; do
    esperado="${pig%.pig}.esperado"
    obtenido=$(java -cp target/contacto-3d-1.0.0.jar com.usac.contacto3d.Compilador --ejecutar "$pig" < /dev/null \
               | sed -n '/^-- salida --$/,/^-- fin --$/p' | sed '1d;$d')
    if diferencias=$(diff <(echo "$obtenido") "$esperado"); then
        echo "OK    $pig"
    else
        echo "FALLA $pig"
        echo "$diferencias"
        fallos=$((fallos + 1))
    fi
done
exit $fallos
