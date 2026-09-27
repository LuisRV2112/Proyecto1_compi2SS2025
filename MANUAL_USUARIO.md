  # Manual de usuario — Contacto 3xtrat3rr3str3D

Contacto 3xtrat3rr3str3D compila programas escritos en tres lenguajes —**Y?**, **Zetariano**
y **PigLatin**— a código de tres direcciones y a un programa en **C** que se puede compilar y
ejecutar.

---

## 1. Requisitos e instalación

- **Java 21** o superior.
- **Maven**, para construir el programa.
- **gcc** (opcional), para compilar el código C que se genera. Sin gcc igual se puede
  ejecutar el programa con el intérprete integrado.

```bash
mvn clean package
java -jar target/contacto-3d-1.0.0.jar
```

Se puede pasar una carpeta para abrirla de entrada:
`java -jar target/contacto-3d-1.0.0.jar mi_proyecto/`. Sin argumentos se reabre la última
carpeta usada.

---

## 2. La ventana

| Zona | Para qué |
|---|---|
| Barra superior | Abrir carpeta, guardar, y las cuatro acciones de compilación |
| Izquierda: **árbol de trabajo** | Los archivos de la carpeta abierta. El ícono indica el lenguaje: **Y** (Y?), **Z** (Zetariano), **P** (PigLatin) |
| Centro: **editor** | Un archivo por pestaña, con números de línea y coloreado del lenguaje |
| Abajo: **resultados** | Errores, Cuartetas, Tabla de símbolos, Código C y Consola |
| Barra de estado | El resultado de la última acción |

---

## 3. Trabajar con archivos y carpetas

| Acción | Cómo |
|---|---|
| Abrir un proyecto | **Archivo > Abrir carpeta** (`Ctrl+O`) |
| Abrir un archivo suelto | **Archivo > Abrir archivo** (`Ctrl+Shift+O`) |
| Abrir un archivo del árbol | Doble clic sobre él |
| Crear un archivo o una carpeta | Clic derecho en el árbol > **Nuevo archivo** / **Nueva carpeta** (o **Archivo > Nuevo archivo**, `Ctrl+N`). Se crea dentro de la carpeta seleccionada. El nombre debe llevar la extensión: `.y`, `.z` o `.pig` |
| Renombrar o eliminar | Clic derecho sobre el archivo o la carpeta. Eliminar una carpeta borra todo su contenido (pide confirmación) |
| Actualizar el árbol | Clic derecho > **Actualizar** (si se cambiaron archivos fuera del programa) |
| Guardar | `Ctrl+S` el archivo actual, `Ctrl+Shift+S` todos, **Guardar como...** con otro nombre |
| Cerrar una pestaña | La `x` de la pestaña, o `Ctrl+W`. Si tiene cambios, pregunta si guardarlos |
| Descargar el proyecto | **Archivo > Descargar proyecto (.zip)**: guarda la carpeta completa en un `.zip` |

Una pestaña con cambios sin guardar muestra un `*` antes del nombre. Antes de compilar se
guardan automáticamente todos los archivos abiertos.

---

## 4. Compilar

Se compila **el archivo de la pestaña activa**:

- un **`.pig`** es un programa completo: se compilan también los `.y` y `.z` que importa;
- un **`.y`** o **`.z`** se analiza solo (útil para revisar una biblioteca o una clase).

| Acción | Tecla | Qué hace |
|---|---|---|
| **Analizar** | `F5` | Valida el programa y muestra errores, cuartetas, tabla de símbolos y código C |
| **Generar C** | `F6` | Además guarda el programa en C en la carpeta `salida/` del proyecto |
| **Ejecutar** | `F7` | Corre el programa con el intérprete de cuartetas (no necesita gcc) |
| **gcc y ejecutar** | `F8` | Compila el C con gcc y corre el programa |

### 4.1 Errores

La pestaña **Errores** lista cada error con su tipo (léxico en rojo, sintáctico en naranja,
semántico en amarillo), archivo, línea, columna, lexema y una descripción que dice qué se
esperaba. Los errores también se **subrayan en el editor**; al pasar el mouse sobre el
subrayado aparece la descripción. **Doble clic** en un error abre el archivo (aunque sea un
import) y lleva a la línea.

Si hay errores léxicos o sintácticos no se hace el análisis semántico, y si hay cualquier
error no se generan cuartetas ni C.

### 4.2 Cuartetas y tabla de símbolos

**Cuartetas** muestra el código de tres direcciones: cada función empieza con
`funcion nombre:`, los saltos usan etiquetas `L0`, `L1`… y los valores intermedios
temporales `t0`, `t1`…

**Tabla de símbolos** lista cada variable, parámetro, función, estructura y clase, con su
ámbito y su lugar en memoria: `STACK` (relativo al marco de la función), `GLOBAL` (variables
de `VARIABILES>`) o, para campos y atributos, su desplazamiento dentro de la estructura u
objeto. Se puede ordenar haciendo clic en una columna.

### 4.3 Código C

**Código C** muestra el programa traducido. Con **Generar C** (`F6`) queda guardado en
`salida/<nombre>.c`; con **Compilar > Guardar código C como...** se elige dónde. Para
compilarlo a mano:

```bash
gcc salida/Principal.c -o programa
./programa
```

### 4.4 Ejecutar y la consola

La pestaña **Consola** tiene dos partes. A la izquierda se escribe **antes de ejecutar** lo
que el programa va a leer, una línea por cada lectura (`<<`, `leer()`, `readln()`); a la
derecha aparece lo que el programa imprime. Si el programa tarda más de 10 segundos con gcc
(por ejemplo, un ciclo infinito), se detiene.

Los errores de ejecución detienen el programa con un mensaje que dice archivo y línea: índice
fuera de rango, acceso a un objeto `null` y división entre cero.

> **Para ejecutar, la pestaña activa tiene que ser el `.pig`.** Solo un `.pig` tiene programa
> principal (`MAIOR>`); un `.y` o un `.z` solo define funciones, estructuras o clases, y al
> ejecutarlo la consola avisa que no hay nada que ejecutar. Si el programa tiene errores,
> tampoco se ejecuta: la consola lo dice y queda seleccionada la pestaña **Errores**.

---

## 5. Los tres lenguajes en breve

### Y? (`.y`) — estructuras y funciones

La **indentación define los bloques** (un tab equivale a 4 espacios). No hay variables
globales.

```
%estructuras
estructura Punto:
    entero x
    entero y

%funciones
definir distancia({} Punto p) -> entero:
    retornar p.x * p.x + p.y * p.y

definir factorial(entero n) -> entero:
    si (n <= 1) entonces
        retornar 1
    retornar n * factorial(n - 1)
```

Tipos: `entero`, `flotante`, `cadena`, `caracter`, `bool` (`verdadero`/`falso`). Arreglos y
estructuras se pasan por referencia con `[] entero a` y `{} Punto p`.

### Zetariano (`.z`) — una clase por archivo

El archivo debe llamarse como la clase (`Persona.z` → `class Persona`).

```java
public class Persona {
    String nombre;
    int edad = 0;

    public Persona(String n) { nombre = n; }

    public String saludo() {
        return edad >= 18 ? "Hola " + nombre : "Hola, joven " + nombre;
    }
}
```

Tipos: `int`, `double`, `char`, `boolean`, `String`. Tiene `%`, el ternario `? :`,
`+= -= *=`, `null`, `new`, matrices (`new int[3][3]`) y sobrecarga de métodos y constructores.

### PigLatin (`.pig`) — el programa principal

```
import carpeta.Persona.z
import carpeta.Funciones.y

VARIABILES>
esto nombre : textum;
esto p : novus Persona("Ana");

MAIOR>
>> "Tu nombre:";
nombre <<
>> p.saludo() >> " - factorial(5) = " >> factorial(5);
FINIS;
```

Tipos: `numerus`, `decimalis`, `textum`, `littera`, `bool` (`verum`/`falsus`). `>>`
imprime, `<<` lee. Las estructuras vienen de los `.y` y se inicializan en orden:
`esto p : Punto {3, 4};`. Los imports se buscan junto al `.pig` o en sus subcarpetas, así que
no importa en qué carpeta estén.

---

## 6. Uso desde consola

Sin abrir la ventana:

```bash
java -cp target/contacto-3d-1.0.0.jar com.usac.contacto3d.Compilador --c salida/programa.c programa.pig
```

| Opción | Muestra |
|---|---|
| (ninguna) | Solo los errores |
| `--tabla` | Tabla de símbolos con la memoria asignada |
| `--c3d` | Cuartetas |
| `--ejecutar` | Corre el programa con el intérprete (lee la entrada estándar) |
| `--c RUTA` | Guarda el programa en C |
| `--tokens`, `--arbol`, `--ast` | Tokens, árbol de ANTLR y árbol sintáctico abstracto |

---

## 7. Preguntas frecuentes

**"No se encontró el archivo importado".** El import se busca junto al `.pig`, junto a su
carpeta padre y en todas las subcarpetas de la carpeta del `.pig`. Revisá el nombre y la
extensión (`import carpeta.Archivo.z`).

**"La clase 'X' está en 'Y.z'".** En Zetariano el archivo debe llamarse exactamente como la
clase.

**"Indentación inconsistente" en Y?.** Las líneas de un mismo bloque deben tener la misma
sangría. Mezclar tabs y espacios suele causarlo: un tab cuenta como 4 espacios.

**"gcc y ejecutar" dice que no encuentra gcc.** Instalalo (`sudo dnf install gcc` o
`sudo apt install gcc`) o usá **Ejecutar** (`F7`), que no lo necesita.
