# Contacto 3xtrat3rr3str3D

Proyecto 1 — Organización de Lenguajes y Compiladores 2
Universidad de San Carlos de Guatemala · Centro Universitario de Occidente
Segundo semestre 2026

Compilador de **tres lenguajes de alto nivel** (Y?, Zetariano y PigLatin) que genera
**código de tres direcciones mediante cuartetas** y lo traduce a un archivo **C compilable**.

---

## Avance

| Fase | Qué cubre | Estado |
|---|---|---|
| 1 | Las tres gramáticas ANTLR4 (incluye el INDENT/DEDENT de Y?) | Lista |
| 2 | El AST común y los tres constructores | Lista |
| 3 | Imports, tabla de símbolos y análisis semántico | Lista |
| 4 | Generación de cuartetas (C3D) | Lista |
| 5 | De cuartetas a un `.c` que compile con gcc | Lista |
| 6 | Árbol de trabajo, coloreado y documentación | Pendiente |

```
src/main/antlr4/.../parser/   LenguajeY.g4, Zetariano.g4, PigLatin.g4
src/main/java/com/usac/contacto3d/
├── Main.java                 stub (la ventana es de la fase 6)
├── Compilador.java           compila un archivo (y sus imports) hasta el .c
├── parser/IndentacionY.java  INDENT/DEDENT de Y?
├── errores/                  modelo y listeners, con soporte multi-archivo
├── ast/                      AST común: 37 nodos y el Visitante
├── constructores/            parse tree de cada lenguaje → AST común
├── semantico/                imports, validaciones de tipos y asignación de memoria
├── simbolos/                 ámbitos, símbolos, offsets de stack y heap
├── ui/                       tema, panel de errores y base de los coloreadores
├── c3d/                      cuartetas: generador e intérprete de referencia
└── generador/                TraductorC: cuartetas → C
```

---

## Compilar y ejecutar

```bash
mvn clean package
java -jar target/contacto-3d-1.0.0.jar
```

Para compilar un programa desde consola y correrlo:

```bash
java -cp target/contacto-3d-1.0.0.jar com.usac.contacto3d.Compilador --c salida/programa.c entradas/ejemplo/Principal.pig
gcc salida/programa.c -o salida/programa
./salida/programa
```

Otras opciones: `--tokens`, `--arbol`, `--ast`, `--tabla` (tabla de símbolos con la memoria),
`--c3d` (cuartetas) y `--ejecutar` (corre las cuartetas sin pasar por C).

`entradas/ejemplo/` tiene un programa completo (`Principal.pig` importa `Funciones.y` y
`Persona.z`) que debe compilar sin errores; `entradas/errores/` tiene errores léxicos y
sintácticos a propósito, y `entradas/errores_semanticos/` un error semántico de cada tipo.
`entradas/programas/` tiene programas con su salida esperada; `entradas/programas/probar.sh`
los ejecuta con el intérprete y compilados con gcc, y compara ambos contra lo esperado.

---

## Requisitos del enunciado

- Java + ANTLR4 obligatorios
- Proyecto obligatorio para tener derecho al Proyecto 2
- El código de tres direcciones se traduce a **C**, no C++
