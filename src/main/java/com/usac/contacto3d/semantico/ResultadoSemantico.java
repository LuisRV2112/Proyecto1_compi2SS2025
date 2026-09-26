package com.usac.contacto3d.semantico;

import com.usac.contacto3d.ast.Programa;
import com.usac.contacto3d.simbolos.SimboloFuncion;
import com.usac.contacto3d.simbolos.TablaSimbolos;

import java.util.List;

/**
 * Lo que el semantico le deja al generador de cuartetas: los AST ya anotados,
 * la tabla con la memoria asignada, y lo que no cuelga de ningun nodo.
 *
 * @param principal       el MAIOR> de PigLatin como funcion (su marco); null si no hay .pig
 * @param tamanioGlobales celdas de la zona de globales (VARIABILES> de PigLatin)
 */
public record ResultadoSemantico(List<Programa> modulos, TablaSimbolos tabla,
                                 SimboloFuncion principal, int tamanioGlobales) {
}
