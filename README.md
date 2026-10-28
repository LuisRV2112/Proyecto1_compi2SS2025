# Contacto 3xtrat3rr3str3D

Proyecto 2 "St4ck Over-Pig" — Organización de Lenguajes y Compiladores 2
Universidad de San Carlos de Guatemala · Centro Universitario de Occidente
Segundo semestre 2026

Compilador de **tres lenguajes de alto nivel** (Y?, Zetariano y PigLatin) que genera
**código de tres direcciones mediante cuartetas** y lo traduce a **ensamblador RISC-V**
(RV32IMD, compatible con RARS). La aplicación trae su propio ensamblador y simulador RISC-V
para ejecutar el `.s` sin salir de la interfaz.

- **[Manual de usuario](MANUAL_USUARIO.md)** — cómo abrir un proyecto, escribir en cada
  lenguaje, compilar, ver las cuartetas y el ensamblador, ejecutar y detener.
- **[Manual técnico](MANUAL_TECNICO.md)** — tecnologías, diagramas de clases, palabras
  reservadas, símbolos y gramáticas, tabla de compatibilidad de tipos de los tres lenguajes,
  modelo de memoria, cuartetas, traducción a RISC-V y el simulador.

---

## Qué agrega el Proyecto 2

- **Zetariano orientado a objetos:** herencia (`extends`), `protected`, `@Override`, `super(...)`
  explícito e implícito, y despacho dinámico por vtable para los métodos sobrescritos.
- **PigLatin:** `null` y la declaración con tipo explícito.
- **Backend RISC-V:** las cuartetas se traducen a un `.s` RV32IMD (cada celda es un `double` de
  8 bytes) con la convención de `ecall` de RARS (servicio en `a7`).
- **Simulador propio:** un ensamblador y un simulador escritos desde cero (paquete `riscv`),
  integrados a la interfaz con ejecución en segundo plano y botón de detener.

---

## Estado

| Fase | Qué cubre | Estado |
|---|---|---|
| 0 | Reparaciones del Proyecto 1 | Lista |
| 1 | `extends`, `protected`, `super` y `@Override` en la gramática de Zetariano | Lista |
| 2 | Semántico de herencia, sobrescritura, `super` y encapsulamiento | Lista |
| 3 | `null` y tipo explícito en PigLatin | Lista |
| 4 | Vtable en la celda 0 del objeto y llamadas indirectas en los tres backends | Lista |
| 5 | Generador de RISC-V | Lista |
| 6 | Ensamblador y simulador RISC-V integrados a la interfaz | Lista |
| 7 | Interfaz: menú Compilar, pestaña RISC-V, consola y detener | Lista |
| 8 | Manual técnico, manual de usuario y este README | Lista |

**Verificado** (con `entradas/programas/probar.sh` y `verificacion/verificar.sh`):
- Las 11 pruebas con salida esperada (programas, herencia, objetos, polimorfismo, el banco del
  enunciado y la prueba de respaldo) dan la misma salida en los tres backends: intérprete de
  cuartetas, `.s` en el simulador propio y `.c` compilado con gcc. 33 de 33.
- **Prueba de respaldo** (`entradas/respaldo/Respaldo.pig`): ejecuta, sección por sección, cada
  punto de la hoja de calificación en los tres lenguajes, incluido lo que el banco no usa: la
  lectura (`<<`, `leer()`, `readln()`), los ciclos de PigLatin, `mientras`/`hacer` y `elegir` en Y?,
  y `while`/`do`/`switch` en Zetariano. La entrada de teclado está en `Respaldo.entrada`: en la
  interfaz se copia al cuadro de entrada de la consola antes de pulsar F7.
- Los cinco casos de calificación (`00_hello_world` a `04_ahorcado`) dan en el simulador RISC-V
  la misma salida que el intérprete de cuartetas. Los `.s` generados y sus salidas están en
  `verificacion/generado/`.

**Limitaciones conocidas:**
- **Sin modificador es `private`**, como lo confirmó el auxiliar. Por eso los casos originales
  `02_lista_doble`, `03_laberinto` y `04_ahorcado` del Proyecto 1 ya no pasan el semántico: leen
  atributos sin modificador desde otras clases. `verificacion/casos/` es una copia con `public`
  en esos atributos (ver `verificacion/README.md`).
- **La verificación fue contra el simulador propio.** El `.s` usa solo instrucciones y servicios
  de RARS, pero los casos no se corrieron en RARS. `verificar.sh` lo hace si se le pasa
  `RARS=ruta/rars1_6.jar`.
- **El heap no se libera.** Las cadenas que produce la concatenación se quedan en el heap. En un
  programa que concatena dentro de un ciclo sin fin (por ejemplo, el ahorcado si la entrada se
  acaba), el heap se agota y la ejecución termina con un error.
- El banco del enunciado está en `entradas/banco/` con `protected` en un atributo de `Cuenta`
  para que las subclases lo usen (ver `entradas/banco/CAMBIOS.md`).

---

## Compilar y ejecutar

Requiere Java 25 y Maven 3.9.

```bash
mvn clean package
java -jar target/contacto-3d-1.0.0.jar
```

En la interfaz: abrir una carpeta, seleccionar el `.pig` principal y usar **Analizar (F5)**,
**Generar RISC-V (F6)**, **Ejecutar RISC-V (F7)** y **Detener (F9)**. El `.s` se guarda en
`salida/<programa>.s`.

Desde consola, sin interfaz:

```bash
# generar el .s
java -cp target/contacto-3d-1.0.0.jar com.usac.contacto3d.Compilador --riscv salida/banco.s entradas/banco/Banco.pig

# ejecutarlo en el simulador propio (la entrada de teclado va por stdin)
java -cp target/contacto-3d-1.0.0.jar com.usac.contacto3d.Compilador --simular entradas/banco/Banco.pig < /dev/null
```

Otras opciones: `--tokens`, `--arbol`, `--ast`, `--tabla` (tabla de símbolos con la memoria),
`--c3d` (cuartetas), `--ejecutar` (intérprete de cuartetas) y `--c salida.c` (el backend C del
Proyecto 1).

Para correr las pruebas:

```bash
entradas/programas/probar.sh     # los tres backends contra la salida esperada
verificacion/verificar.sh        # los cinco casos de calificación
```

---

## Los tres lenguajes juntos

`Principal.pig` importa una clase de Zetariano y funciones de Y?:

```
entradas/ejemplo/
├── Principal.pig    programa principal (PigLatin)
├── Persona.z        clase (Zetariano)
└── Funciones.y      funciones (Y?)
```

`entradas/polimorfismo/` muestra lo nuevo: `Impresora.z` y tres subclases que sobrescriben
`imprimir`, llamadas desde un `.pig` por variable del tipo padre, por `this` y desde un arreglo.

---

## Estructura

```
src/main/antlr4/.../parser/   LenguajeY.g4, Zetariano.g4, PigLatin.g4
src/main/java/com/usac/contacto3d/
├── Main.java                 abre la ventana
├── Compilador.java           compila un archivo (y sus imports) hasta el .s
├── parser/IndentacionY.java  INDENT/DEDENT de Y?
├── errores/                  modelo y listeners, con soporte multi-archivo
├── ast/                      AST común y el Visitante
├── constructores/            parse tree de cada lenguaje → AST común
├── semantico/                imports, tipos, herencia y TablaCompatibilidad (genera la tabla del manual)
├── simbolos/                 ámbitos, símbolos, offsets, clase padre y vtable
├── c3d/                      cuartetas: generador e intérprete de referencia
├── generador/                GeneradorRiscV (cuartetas → .s) y TraductorC (cuartetas → .c)
├── riscv/                    Ensamblador y Simulador propios
└── ui/                       ventana, árbol de trabajo, editor, coloreadores propios y paneles
entradas/                     programas de prueba con su salida esperada
verificacion/                 casos de calificación, sus .s y la verificación cruzada
```

---

## Requisitos del enunciado

- Java + ANTLR4 obligatorios.
- El coloreado de sintaxis es propio: no usa el de RSyntaxTextArea ni otra librería.
- El ensamblador generado es RISC-V y se ejecuta dentro de la aplicación.
