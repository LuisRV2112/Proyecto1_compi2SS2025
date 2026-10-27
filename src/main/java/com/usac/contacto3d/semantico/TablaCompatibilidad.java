package com.usac.contacto3d.semantico;

import com.usac.contacto3d.ast.Tipo;
import com.usac.contacto3d.ast.expresiones.Operador;
import com.usac.contacto3d.simbolos.SimboloEstructura;
import com.usac.contacto3d.simbolos.TablaSimbolos;

import java.util.List;

/**
 * Genera la seccion 5 del manual tecnico (la tabla de compatibilidad de tipos) consultando
 * {@link Compatibilidad}, la misma clase que usa el analizador. Si cambian las reglas, la tabla se
 * regenera con:
 *
 * <pre>java -cp target/contacto-3d-1.0.0.jar com.usac.contacto3d.semantico.TablaCompatibilidad</pre>
 */
public final class TablaCompatibilidad {

    private static final List<Tipo> PRIMITIVOS =
            List.of(Tipo.ENTERO, Tipo.FLOTANTE, Tipo.CADENA, Tipo.CARACTER, Tipo.BOOLEANO);

    private record Lenguaje(String titulo, List<String> nombres, String negacion, boolean tieneModulo) { }

    private static final List<Lenguaje> LENGUAJES = List.of(
            new Lenguaje("5.1 Y?", List.of("entero", "flotante", "cadena", "caracter", "bool"), "!", false),
            new Lenguaje("5.2 Zetariano", List.of("int", "double", "String", "char", "boolean"), "!", true),
            new Lenguaje("5.3 PigLatin", List.of("numerus", "decimalis", "textum", "littera", "bool"), "non", false));

    private TablaCompatibilidad() { }

    public static void main(String[] args) {
        TablaSimbolos tabla = tablaConHerencia();
        StringBuilder sb = new StringBuilder();
        sb.append("""
                ## 5. Tabla de compatibilidad de tipos

                Se genera a partir de `semantico/Compatibilidad`, la misma clase que usa el analizador, así que
                no puede diferir de lo que el compilador hace. Para regenerarla:

                ```bash
                java -cp target/contacto-3d-1.0.0.jar com.usac.contacto3d.semantico.TablaCompatibilidad
                ```

                Las reglas son las mismas en los tres lenguajes; solo cambian los nombres de los tipos.

                **Jerarquía** (al operar gana la más alta): cadena 5 > flotante 4 > entero 3 > carácter 2 > bool 1.
                Asignar solo puede **ampliar** dentro de la familia numérica (carácter → entero → flotante); bool y
                cadena exigen el mismo tipo. Los objetos, la herencia y `null` están en 5.4.

                """);
        for (Lenguaje lenguaje : LENGUAJES) {
            sb.append("### ").append(lenguaje.titulo()).append("\n\n");
            primitivos(sb, lenguaje, tabla);
        }
        objetos(sb, tabla);
        System.out.print(sb);
    }

    private static void primitivos(StringBuilder sb, Lenguaje lenguaje, TablaSimbolos tabla) {
        operacion(sb, lenguaje, tabla, "Suma `+` (con una cadena, concatena)", Operador.SUMA);
        operacion(sb, lenguaje, tabla, "Resta, multiplicacion y division `-` `*` `/`", Operador.RESTA);
        if (lenguaje.tieneModulo()) {
            operacion(sb, lenguaje, tabla, "Modulo `%`", Operador.MODULO);
        }
        operacion(sb, lenguaje, tabla, "Relacionales `<` `>` `<=` `>=`", Operador.MENOR);
        operacion(sb, lenguaje, tabla, "Igualdad `==` `!=`", Operador.IGUAL);
        operacion(sb, lenguaje, tabla,
                "Logicos `&&` `||` (negacion: `" + lenguaje.negacion() + "`)", Operador.AND);

        sb.append("**Asignacion, paso de parametros y retorno** — fila: tipo del destino, columna: tipo del valor\n\n");
        encabezado(sb, "destino \\ valor", lenguaje.nombres());
        for (int i = 0; i < PRIMITIVOS.size(); i++) {
            sb.append("| **").append(lenguaje.nombres().get(i)).append("** |");
            for (Tipo valor : PRIMITIVOS) {
                sb.append(' ').append(asignacion(PRIMITIVOS.get(i), valor, tabla)).append(" |");
            }
            sb.append('\n');
        }
        sb.append('\n');
    }

    private static void operacion(StringBuilder sb, Lenguaje lenguaje, TablaSimbolos tabla,
                                  String titulo, Operador operador) {
        sb.append("**").append(titulo).append("** — fila: operando izquierdo, columna: operando derecho\n\n");
        encabezado(sb, "", lenguaje.nombres());
        for (int i = 0; i < PRIMITIVOS.size(); i++) {
            sb.append("| **").append(lenguaje.nombres().get(i)).append("** |");
            for (Tipo b : PRIMITIVOS) {
                Tipo resultado = Compatibilidad.binaria(operador, PRIMITIVOS.get(i), b, tabla);
                sb.append(' ').append(resultado == null ? "error" : nombre(resultado, lenguaje)).append(" |");
            }
            sb.append('\n');
        }
        sb.append('\n');
    }

    /** Animal, Perro y Gato heredan de Animal; Persona no tiene relacion con ellas. */
    private static TablaSimbolos tablaConHerencia() {
        TablaSimbolos tabla = new TablaSimbolos();
        SimboloEstructura animal = new SimboloEstructura("Animal", true, "Animal.z", 1, 1);
        SimboloEstructura perro = new SimboloEstructura("Perro", true, "Perro.z", 1, 1);
        SimboloEstructura gato = new SimboloEstructura("Gato", true, "Gato.z", 1, 1);
        SimboloEstructura persona = new SimboloEstructura("Persona", true, "Persona.z", 1, 1);
        perro.setPadre(animal);
        gato.setPadre(animal);
        for (SimboloEstructura clase : List.of(animal, perro, gato, persona)) {
            tabla.declararTipo(clase);
        }
        return tabla;
    }

    private static void objetos(StringBuilder sb, TablaSimbolos tabla) {
        List<Tipo> tipos = List.of(Tipo.objeto("Animal"), Tipo.objeto("Perro"), Tipo.objeto("Gato"),
                Tipo.objeto("Persona"), Tipo.arreglo(Tipo.ENTERO), Tipo.CADENA, Tipo.ENTERO, Tipo.NULO);
        List<String> nombres = List.of("Animal", "Perro", "Gato", "Persona", "int[]", "String", "int", "null");

        sb.append("""
                ### 5.4 Objetos, herencia y `null`

                Para las clases de Zetariano usadas desde Zetariano o desde PigLatin. Las clases de ejemplo:
                `Perro extends Animal`, `Gato extends Animal` y `Persona`, sin relacion con las otras.
                `int[]` es cualquier arreglo, `String` la cadena del lenguaje (`textum` en PigLatin) e `int`
                cualquier primitivo.

                """);

        sb.append("**Asignacion, paso de parametros y retorno** — fila: tipo del destino, columna: tipo del valor\n\n");
        encabezado(sb, "destino \\ valor", nombres);
        for (int i = 0; i < tipos.size() - 1; i++) {
            sb.append("| **").append(nombres.get(i)).append("** |");
            for (Tipo valor : tipos) {
                sb.append(' ').append(asignacion(tipos.get(i), valor, tabla)).append(" |");
            }
            sb.append('\n');
        }

        sb.append("\n**Igualdad `==` `!=`** — fila: operando izquierdo, columna: operando derecho\n\n");
        encabezado(sb, "", nombres);
        for (int i = 0; i < tipos.size(); i++) {
            sb.append("| **").append(nombres.get(i)).append("** |");
            for (Tipo b : tipos) {
                Tipo resultado = Compatibilidad.binaria(Operador.IGUAL, tipos.get(i), b, tabla);
                sb.append(' ').append(resultado == null ? "error" : "boolean").append(" |");
            }
            sb.append('\n');
        }

        Tipo suma = Compatibilidad.binaria(Operador.SUMA, Tipo.CADENA, Tipo.objeto("Animal"), tabla);
        Tipo resta = Compatibilidad.binaria(Operador.RESTA, Tipo.objeto("Animal"), Tipo.objeto("Animal"), tabla);
        sb.append("\n**Otras operaciones con objetos:** `String + Animal` da ")
                .append(suma == null ? "error" : suma)
                .append(" y `Animal - Animal` da ").append(resta == null ? "error" : resta)
                .append(". Un objeto solo se usa con `==`, `!=`, `.atributo` y `.metodo()`.\n");
        sb.append("""

                En resumen:
                - Una variable del tipo padre acepta un objeto de cualquier subclase, directa o indirecta. Al
                  revés no: un `Animal` no se puede guardar en un `Perro`.
                - Dos objetos se pueden comparar si uno es de la clase del otro o de una subclase. Dos hermanas
                  (`Perro` y `Gato`) no.
                - `null` se puede asignar y comparar solo con objetos y arreglos; con ningún primitivo, ni
                  siquiera con la cadena.
                - Las mismas reglas valen para los parámetros, el `return` y el tipo de retorno de un método
                  sobrescrito (retorno covariante).
                """);
    }

    private static void encabezado(StringBuilder sb, String esquina, List<String> nombres) {
        sb.append(esquina.isEmpty() ? "| |" : "| " + esquina + " |");
        nombres.forEach(n -> sb.append(" **").append(n).append("** |"));
        sb.append("\n|---|");
        nombres.forEach(n -> sb.append("---|"));
        sb.append('\n');
    }

    private static String asignacion(Tipo destino, Tipo valor, TablaSimbolos tabla) {
        if (Compatibilidad.asignable(destino, valor, tabla)) {
            return "si";
        }
        return Compatibilidad.pierdeInformacion(destino, valor) ? "no (pierde informacion)" : "no";
    }

    private static String nombre(Tipo tipo, Lenguaje lenguaje) {
        int i = PRIMITIVOS.indexOf(tipo);
        return i >= 0 ? lenguaje.nombres().get(i) : tipo.toString();
    }
}
