# Verificación cruzada del ensamblador RISC-V

El ensamblador generado usa **RV32IMD** (enteros, multiplicación y doble precisión) y la
convención de `ecall` de **RARS**: el número de servicio va en `a7`. El Venus de CS61C no sirve
como referencia, porque no implementa la extensión D y espera el número de servicio en `a0`.

## Contenido

| Ruta | Qué es |
|---|---|
| `verificar.sh` | Genera el `.s` de cada caso de calificación, lo ejecuta y compara las salidas |
| `casos/` | Copia de los casos de calificación del Proyecto 1, con `public` en los atributos sin modificador |
| `entradas/` | La entrada de teclado de cada caso, una línea por lectura |
| `generado/*.s` | El ensamblador generado |
| `generado/*.esperado` | Salida del intérprete de cuartetas (la referencia) |
| `generado/*.simulador` | Salida del simulador RISC-V propio |
| `generado/*.rars` | Salida de RARS, si se ejecutó con `RARS=...` |

## Uso

```bash
mvn clean package
verificacion/verificar.sh ruta/a/los/casos              # simulador propio contra el intérprete
RARS=ruta/rars1_6.jar verificacion/verificar.sh ruta/a/los/casos   # además, RARS
```

Sin argumento, el script usa `verificacion/casos/`.

### Por qué una copia de los casos

En el Proyecto 2 un miembro sin modificador se trata como `private`: solo es accesible dentro de
su propia clase (lo confirmó el auxiliar). Los casos de calificación del Proyecto 1 leen atributos
sin modificador desde otras clases y desde el `.pig`, así que los originales ya no pasan el
semántico: `02_lista_doble` da 60 errores de acceso, `03_laberinto` 28 y `04_ahorcado` 7.

La copia agrega `public` a esos atributos, y es el único cambio: 34 declaraciones en los `.z`. Los
`.pig` y los `.y` son idénticos. La salida esperada es la misma que la de los originales con el
compilador del Proyecto 1.

## A mano, en RARS

1. Generar el `.s`: en la aplicación, **Compilar > Generar ensamblador RISC-V (F6)**. Se guarda en
   `salida/<programa>.s`.
2. Abrir RARS, **File > Open** el `.s`, **Run > Assemble** (F3) y **Run > Go** (F5).
3. Escribir la entrada en la consola de RARS cuando el programa la pida.
4. Comparar con la salida de **Ejecutar ensamblador RISC-V (F7)** dentro de la aplicación.

## Cuando no coinciden

| Síntoma | Causa probable |
|---|---|
| RARS no ensambla | Una instrucción o directiva que RARS no acepta |
| RARS da otra salida | El simulador propio es más permisivo: revisar `x0`, desbordamientos o alineación |
| Solo el propio da otra salida | Una seudoinstrucción mal expandida en `Ensamblador` |
| RARS se queda sin memoria | El `sbrk` inicial: ver `CELDAS_HEAP` y `CELDAS_PILA` en `GeneradorRiscV` |
